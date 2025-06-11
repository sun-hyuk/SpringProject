package com.dita.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.dita.dto.EventDTO;
import com.dita.service.EventService;

@Controller
public class EventController {
	
	@Autowired
	private EventService eventService;
	
	@GetMapping("/event")
	public String showEventPage(Model model) {
	    eventService.updateEventStatuses();  // endDate 기반 status 갱신

	    List<EventDTO> ongoingEvents = eventService.getEventsByStatus("진행중");
	    List<EventDTO> endedEvents = eventService.getEventsByStatus("종료");

	    model.addAttribute("ongoingEvents", ongoingEvents);
	    model.addAttribute("endedEvents", endedEvents);

	    return "event/event"; // templates/event/event.html
	}
	
	@GetMapping("/event/{eventId}")
	public String showEventDetail(@PathVariable Integer eventId, Model model) {
	    try {
	        EventDTO event = eventService.getEventById(eventId);
	        model.addAttribute("event", event);
	        return "event/eventDetail";
	    } catch (NoSuchElementException e) {
	        return "error/404";  // templates/error/404.html 필요
	    }
	}
}
