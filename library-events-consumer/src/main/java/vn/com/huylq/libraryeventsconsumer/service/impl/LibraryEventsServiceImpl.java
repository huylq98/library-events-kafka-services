package vn.com.huylq.libraryeventsconsumer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import vn.com.huylq.libraryeventsconsumer.entity.LibraryEvent;
import vn.com.huylq.libraryeventsconsumer.repository.LibraryEventsRepository;
import vn.com.huylq.libraryeventsconsumer.service.LibraryEventsService;

@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryEventsServiceImpl implements LibraryEventsService {

  private final ObjectMapper objectMapper;
  private final LibraryEventsRepository libraryEventsRepository;

  @Override
  public void process(ConsumerRecord<Integer, String> consumerRecord)
      throws JsonProcessingException {
    LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
    log.info("libraryEvent: {}", libraryEvent);
    switch (libraryEvent.getLibraryEventType()) {
      case NEW:
        save(libraryEvent);
        break;
      case UPDATE:
        validate(libraryEvent);
        save(libraryEvent);
        break;
      default:
        log.info("Invalid library event type");
    }
  }

  private void validate(LibraryEvent libraryEvent) {
    if (libraryEvent.getId() == null) {
      throw new IllegalArgumentException("Library event ID is missing");
    }
    Optional<LibraryEvent> libraryEventOptional = libraryEventsRepository.findById(
        libraryEvent.getId());
    if (!libraryEventOptional.isPresent()) {
      throw new IllegalArgumentException("Library event not found");
    }
  }

  private void save(LibraryEvent libraryEvent) {
    libraryEvent.getBook().setLibraryEvent(libraryEvent);
    libraryEvent = libraryEventsRepository.save(libraryEvent);
    log.info("libraryEvent saved successfully: {}", libraryEvent);
  }
}
