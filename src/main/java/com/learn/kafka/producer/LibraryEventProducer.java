package com.learn.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.kafka.domain.LibraryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LibraryEventProducer {

    private static final String TOPIC = "library-events";
    private final KafkaTemplate<Integer, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendEvent(final LibraryEvent libraryEvent) throws JsonProcessingException {
        final Integer key = libraryEvent.getLibraryEventId();
        final String value = objectMapper.writeValueAsString(libraryEvent);

        final ListenableFuture<SendResult<Integer, String>> listenableFuture =
                kafkaTemplate.sendDefault(key, value);

        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleResult(key, value, result);
            }
        });
    }

    public ListenableFuture<SendResult<Integer, String>> sendEventApproach2(final LibraryEvent libraryEvent) throws JsonProcessingException {
        final Integer key = libraryEvent.getLibraryEventId();
        final String value = objectMapper.writeValueAsString(libraryEvent);

        final ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, value, TOPIC);

        final ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(producerRecord);

        listenableFuture.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleResult(key, value, result);
            }
        });

        return listenableFuture;
    }

    private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value, String topic) {
        final List<Header> headers = List.of(new RecordHeader("event-source", "scanner".getBytes()));
        return new ProducerRecord<>(topic, null, key, value, headers);
    }

    public SendResult<Integer, String> sendEventSynchronously(final LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException {
        final Integer key = libraryEvent.getLibraryEventId();
        final String value = objectMapper.writeValueAsString(libraryEvent);
        SendResult<Integer, String> sendResult;
        try {
            sendResult = kafkaTemplate.sendDefault(key, value).get();
        } catch (ExecutionException | InterruptedException e) {
            log.error("ExecutionException / InterruptedException sending the message for key '{}, value '{}', exception is '{}'", key, value, e.getMessage());
            throw e;
        } catch (Exception ex) {
            log.error("ExecutionException / InterruptedException sending the message for key '{}, value '{}', exception is '{}'", key, value, ex.getMessage());
            throw ex;
        }
        return sendResult;
    }

    private void handleFailure(Integer key, String value, Throwable ex) {
        log.error("Error of sending the message for key '{}, value '{}', exception is '{}'", key, value, ex.getMessage());
        try {
            throw ex;
        } catch (Throwable throwable) {
            log.error("Error in OnFailure '{}'", throwable.getMessage());
        }
    }

    private void handleResult(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Message sent successfully for the key '{}', and the value is '{}, partition is '{}'", key, value, result.getRecordMetadata().partition());
    }
}
