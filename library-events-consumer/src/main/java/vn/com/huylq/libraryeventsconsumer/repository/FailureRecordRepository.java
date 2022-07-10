package vn.com.huylq.libraryeventsconsumer.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.huylq.libraryeventsconsumer.constant.FailureRecordStatus;
import vn.com.huylq.libraryeventsconsumer.entity.FailureRecord;

@Repository
public interface FailureRecordRepository extends JpaRepository<FailureRecord, Integer> {

  List<FailureRecord> findAllByStatus(FailureRecordStatus status);
}
