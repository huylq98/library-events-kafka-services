package vn.com.huylq.libraryeventsconsumer.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import vn.com.huylq.libraryeventsconsumer.constant.LibraryEventType;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class LibraryEvent {

  @Id
  @GeneratedValue
  private Integer id;

  @Enumerated(EnumType.STRING)
  private LibraryEventType libraryEventType;

  @OneToOne(mappedBy = "libraryEvent", cascade = CascadeType.ALL)
  @ToString.Exclude
  private Book book;
}
