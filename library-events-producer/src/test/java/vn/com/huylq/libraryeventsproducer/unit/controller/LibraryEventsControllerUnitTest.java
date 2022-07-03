package vn.com.huylq.libraryeventsproducer.unit.controller;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vn.com.huylq.libraryeventsproducer.controller.LibraryEventsController;
import vn.com.huylq.libraryeventsproducer.domain.Book;
import vn.com.huylq.libraryeventsproducer.domain.LibraryEvent;
import vn.com.huylq.libraryeventsproducer.producer.LibraryEventsProducer;

@WebMvcTest(LibraryEventsController.class)
@AutoConfigureMockMvc
@SuppressWarnings("all")
class LibraryEventsControllerUnitTest {

  public static final String URL_TEMPLATE = "/v1/library-event";
  @Autowired
  MockMvc mockMvc;

  @MockBean
  LibraryEventsProducer libraryEventsProducer;

  ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void postLibraryEvent() throws Exception {
    // given
    Book book = Book.builder()
        .id(1)
        .name("Black Phone")
        .author("Stephen")
        .build();

    LibraryEvent libraryEvent = LibraryEvent.builder()
        .id(null)
        .book(book)
        .build();
    // when
    when(libraryEventsProducer.sendLibraryEvents(isA(LibraryEvent.class))).thenReturn(null);

    // then
    mockMvc.perform(post(URL_TEMPLATE)
            .content(objectMapper.writeValueAsString(libraryEvent))
            .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isCreated());

  }

  @Test
  void postLibraryEvent_4xx() throws Exception {
    // given
    Book book = Book.builder()
        .id(null)
        .name(null)
        .author("Stephen")
        .build();

    LibraryEvent libraryEvent = LibraryEvent.builder()
        .id(null)
        .book(book)
        .build();
    // when
    when(libraryEventsProducer.sendLibraryEvents(isA(LibraryEvent.class))).thenReturn(null);
    // then
    String expectedErrorMessage = "book.id - must not be null, book.name - must not be blank";
    mockMvc.perform(post(URL_TEMPLATE)
            .content(objectMapper.writeValueAsString(libraryEvent))
            .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is4xxClientError())
        .andExpect(content().string(expectedErrorMessage));

  }

  @Test
  void putLibaryEvent_success() throws Exception {
    // given
    Book book = Book.builder()
        .id(1)
        .name("The Black Phone")
        .author("Joe Hill")
        .build();

    LibraryEvent libraryEvent = LibraryEvent.builder()
        .id(1)
        .book(book)
        .build();

    // when
    when(libraryEventsProducer.sendLibraryEvents(isA(LibraryEvent.class))).thenReturn(null);

    // then
    mockMvc.perform(
            put(URL_TEMPLATE)
                .content(objectMapper.writeValueAsString(libraryEvent))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  void putLibraryEvent_4xx() throws Exception {
    // given
    Book book = Book.builder()
        .id(1)
        .name("The Black Phone")
        .author("Joe Hill")
        .build();

    LibraryEvent libraryEvent = LibraryEvent.builder()
        .id(null)
        .book(book)
        .build();

    // when
    when(libraryEventsProducer.sendLibraryEvents(isA(LibraryEvent.class))).thenReturn(null);

    // then
    mockMvc.perform(
            put(URL_TEMPLATE)
                .content(objectMapper.writeValueAsString(libraryEvent))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is4xxClientError());
  }
}
