package vn.com.huylq.libraryeventsproducer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import vn.com.huylq.libraryeventsproducer.constant.LibraryEventType;
import vn.com.huylq.libraryeventsproducer.domain.LibraryEvent;
import vn.com.huylq.libraryeventsproducer.producer.LibraryEventsProducer;

@RestController
@RequestMapping("/v1/library-event")
@RequiredArgsConstructor
@SuppressWarnings("java:S1452")
public class LibraryEventsController {

  private final LibraryEventsProducer producer;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public LibraryEvent scanNewBook(@Valid @RequestBody LibraryEvent libraryEvent)
      throws JsonProcessingException {

    libraryEvent.setLibraryEventType(LibraryEventType.NEW);
    producer.sendLibraryEvents(libraryEvent);

    return libraryEvent;
  }

  @PutMapping
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<?> updateBook(@RequestBody LibraryEvent libraryEvent)
      throws JsonProcessingException {

    if (libraryEvent.getId() == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("libraryEventId is required");
    }
    libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
    producer.sendLibraryEvents(libraryEvent);

    return ResponseEntity.ok(libraryEvent);
  }
}
