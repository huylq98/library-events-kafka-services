package vn.com.huylq.libraryeventsconsumer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.huylq.libraryeventsconsumer.entity.LibraryEvent;

@Repository
public interface LibraryEventsRepository extends JpaRepository<LibraryEvent, Integer> {

}
