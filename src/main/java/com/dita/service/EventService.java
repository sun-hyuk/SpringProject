package com.dita.service;

import com.dita.domain.Event;
import com.dita.dto.EventDTO;
import com.dita.persistence.EventRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    public void saveEvent(Event event) {
        eventRepository.save(event);
    }

    @Transactional
    public void updateEventStatuses() {
        eventRepository.updateStatusToEnded();
    }
    
    public List<EventDTO> getEventsByStatus(String status) {
        List<Event> events = eventRepository.findByStatusOrderByCreatedAtDesc(status);  // 🔄 변경된 부분
        return events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public EventDTO getEventById(Integer eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new NoSuchElementException("이벤트를 찾을 수 없습니다."));
        return convertToDTO(event);
    }
    
    private EventDTO convertToDTO(Event event) {
        EventDTO dto = new EventDTO();
        dto.setEventId(event.getEventId());
        dto.setAdminId(event.getAdminId());
        dto.setContent(event.getContent());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setEndDate(event.getEndDate());
        dto.setImageUrl(event.getImageUrl());
        dto.setStartDate(event.getStartDate());
        dto.setStatus(event.getStatus());
        dto.setTitle(event.getTitle());
        dto.setUpdatedAt(event.getUpdatedAt());
        dto.setViews(event.getViews());
        return dto;
    }
}
