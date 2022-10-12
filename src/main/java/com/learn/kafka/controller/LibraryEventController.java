package com.learn.kafka.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.learn.kafka.domain.LibraryEvent;
import com.learn.kafka.domain.LibraryEventType;
import com.learn.kafka.producer.LibraryEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LibraryEventController {

    private final LibraryEventProducer libraryEventProducer;

    @PostMapping("v1/library/event")
    public ResponseEntity<LibraryEvent> postLibraryEvent(@Valid @RequestBody final LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException {
        log.info("Before SendLibraryEvent");
        libraryEvent.setLibraryEventType(LibraryEventType.NEW);
        libraryEventProducer.sendEventApproach2(libraryEvent);
        log.info("After SendLibraryEvent");
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PutMapping("v1/library/event")
    public ResponseEntity<?> putLibraryEvent(@Valid @RequestBody final LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException {
        if (libraryEvent.getLibraryEventId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please pass the LibraryEventId");
        }
        libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
        libraryEventProducer.sendEventApproach2(libraryEvent);
        return ResponseEntity.status(HttpStatus.OK).body(libraryEvent);
    }
}
