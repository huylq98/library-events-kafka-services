package vn.com.huylq.libraryeventsproducer.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.huylq.libraryeventsproducer.constant.LibraryEventType;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LibraryEvent {

  private Integer id;
  private LibraryEventType libraryEventType;
  private Book book;
}
