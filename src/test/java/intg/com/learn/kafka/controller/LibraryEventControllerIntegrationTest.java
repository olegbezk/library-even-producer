package com.learn.kafka.controller;

import com.learn.kafka.domain.Book;
import com.learn.kafka.domain.LibraryEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = { "library-events" }, partitions = 3)
@TestPropertySource(properties = {
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"
})
class LibraryEventControllerIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<Integer, String> consumer;

    @BeforeEach
    void setUp() {
        HashMap<String, Object> configs = new HashMap<>(
                KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));
        consumer = new DefaultKafkaConsumerFactory<>(
                configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void postLibraryEvent() {
            final String expectedValue = "{\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":123,\"bookName\":\"First\",\"bookAuthor\":\"Me\"}}";
        final LibraryEvent libraryEvent = getLibraryEvent(null);

        HttpHeaders header = new HttpHeaders();
        header.add("content-type", MediaType.APPLICATION_JSON.toString());
        HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent, header);

        ResponseEntity<LibraryEvent> response = testRestTemplate.exchange(
                "/v1/library/event", HttpMethod.POST, request, LibraryEvent.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "library-events");

        assertThat(consumerRecord.key()).isNull();
        assertThat(consumerRecord.value()).isEqualTo(expectedValue);
    }

    @Test
    void putLibraryEvent() throws InterruptedException {
        final String expectedValue = "{\"libraryEventId\":123,\"libraryEventType\":\"UPDATE\",\"book\":{\"bookId\":123,\"bookName\":\"First\",\"bookAuthor\":\"Me\"}}";
        final LibraryEvent libraryEvent = getLibraryEvent(123);

        HttpHeaders header = new HttpHeaders();
        header.add("content-type", MediaType.APPLICATION_JSON.toString());
        HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent, header);

        ResponseEntity<LibraryEvent> response = testRestTemplate.exchange(
                "/v1/library/event", HttpMethod.PUT, request, LibraryEvent.class);

        Thread.sleep(3000);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ConsumerRecords<Integer, String> records = KafkaTestUtils.getRecords(consumer);

        for (ConsumerRecord<Integer, String> consumerRecord : records) {
            if (consumerRecord.key() != null) {
                assertThat(consumerRecord.key()).isEqualTo(123);
                assertThat(consumerRecord.value()).isEqualTo(expectedValue);
            }
        }
    }

    private LibraryEvent getLibraryEvent(final Integer libraryEventId) {
        Book book = Book.builder()
                .bookId(123)
                .bookAuthor("Me")
                .bookName("First")
                .build();
        return LibraryEvent.builder()
                .libraryEventId(libraryEventId)
                .book(book)
                .build();
    }
}
