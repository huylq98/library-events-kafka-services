package vn.com.huylq.libraryeventsproducer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.com.huylq.libraryeventsproducer.constant.LibraryEventType;
import vn.com.huylq.libraryeventsproducer.domain.LibraryEvent;
import vn.com.huylq.libraryeventsproducer.producer.LibraryEventsProducer;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/library-event")
@RequiredArgsConstructor
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

  @PutMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public LibraryEvent updateBook(@PathVariable Integer id, @RequestBody LibraryEvent libraryEvent) {

    libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
    // TODO: invoke kafka

    return libraryEvent;
  }
}
