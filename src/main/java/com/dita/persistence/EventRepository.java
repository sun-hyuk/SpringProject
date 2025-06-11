package com.dita.persistence;

import com.dita.domain.Event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface EventRepository extends JpaRepository<Event, Integer> {
	
	@Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Event e SET e.status = '종료' WHERE e.endDate < CURRENT_DATE AND (e.status != '종료' OR e.status IS NULL)")
    void updateStatusToEnded();

    List<Event> findByStatusOrderByCreatedAtDesc(String status);
}
