package com.learn.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.kafka.domain.Book;
import com.learn.kafka.domain.LibraryEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryEventProducerTest {

    @Mock
    private KafkaTemplate<Integer, String> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private LibraryEventProducer libraryEventProducer;

    @Test
    void sendEvent_Approach2_Failure() {
        Book book = Book.builder()
                .bookId(123)
                .bookAuthor("Me")
                .bookName("First")
                .build();
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        SettableListenableFuture<Object> listenableFuture = new SettableListenableFuture<>();
        listenableFuture.setException(new RuntimeException("Exception of calling Kafka"));

        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(listenableFuture);

        assertThatThrownBy(() -> libraryEventProducer.sendEventApproach2(libraryEvent).get())
                .hasMessageContaining("Exception of calling Kafka");
    }

    @Test
    void sendEvent_Approach2_Success() throws JsonProcessingException, ExecutionException, InterruptedException {
        Book book = Book.builder()
                .bookId(123)
                .bookAuthor("Me")
                .bookName("First")
                .build();
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();

        String record = objectMapper.writeValueAsString(libraryEvent);

        ProducerRecord<Integer, String> producerRecord = new ProducerRecord("library-events", libraryEvent.getLibraryEventId(), record);
        RecordMetadata recordMetadata = new RecordMetadata(
                new TopicPartition("library-events", 1), 1, 1, 342, System.currentTimeMillis(), 1, 2);
        SendResult<Integer, String> sendResult = new SendResult<>(producerRecord, recordMetadata);
        SettableListenableFuture<Object> listenableFuture = new SettableListenableFuture<>();
        listenableFuture.set(sendResult);

        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(listenableFuture);

        ListenableFuture<SendResult<Integer, String>> future = libraryEventProducer.sendEventApproach2(libraryEvent);
        SendResult<Integer, String> result = future.get();

        assertThat(result.getRecordMetadata().partition()).isEqualTo(1);
    }
}
