package vn.com.huylq.libraryeventsconsumer.entity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.huylq.libraryeventsconsumer.constant.FailureRecordStatus;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class FailureRecord {

  @Id
  @GeneratedValue
  private Integer id;
  private String topic;
  private Integer partition;
  private Integer key;
  private String errorRecord;
  private Long offset;
  @Enumerated(EnumType.STRING)
  private FailureRecordStatus status;
  private Long timestamp;
}
