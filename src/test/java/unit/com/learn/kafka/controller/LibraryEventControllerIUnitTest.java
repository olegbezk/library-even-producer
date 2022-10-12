package com.learn.kafka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.kafka.domain.Book;
import com.learn.kafka.domain.LibraryEvent;
import com.learn.kafka.producer.LibraryEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryEventController.class)
@AutoConfigureMockMvc
class LibraryEventControllerIUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LibraryEventProducer libraryEventProducer;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void postEventData() throws Exception {
        Book book = Book.builder()
                .bookId(123)
                .bookAuthor("Me")
                .bookName("First")
                .build();
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .book(book)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);

        mockMvc.perform(post("/v1/library/event")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void putEventData() throws Exception {
        Book book = Book.builder()
                .bookId(123)
                .bookAuthor("Me")
                .bookName("First")
                .build();
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(456)
                .book(book)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);

        mockMvc.perform(put("/v1/library/event")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void putEventDataFail() throws Exception {
        Book book = Book.builder()
                .bookAuthor("Me")
                .bookId(123)
                .bookName("First")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .book(book)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);

        mockMvc.perform(put("/v1/library/event")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please pass the LibraryEventId"));
    }

    @Test
    void postEventDataFail() throws Exception {
        Book book = Book.builder()
                .bookId(123)
                .bookName("First")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .book(book)
                .build();

        String json = objectMapper.writeValueAsString(libraryEvent);

        mockMvc.perform(post("/v1/library/event")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("book.bookAuthor - must not be blank"));
    }
}
