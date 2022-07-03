package vn.com.huylq.libraryeventsconsumer.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class Book {

  @Id
  private Integer id;

  private String name;

  private String author;

  @OneToOne
  @JoinColumn(name = "libraryEventId")
  private LibraryEvent libraryEvent;
}
