package com.badminton.service;

import com.badminton.entity.Event;
import com.badminton.exception.EventNotFoundException;
import com.badminton.form.EventForm;
import com.badminton.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> findUpcomingEvents() {
        return eventRepository.findByEventDateGreaterThanEqualOrderByEventDateAscStartTimeAsc(LocalDate.now());
    }

    public List<Event> findAllEvents() {
        return eventRepository.findAllByOrderByEventDateAscStartTimeAsc();
    }

    public Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }

    @Transactional
    public Event createEvent(EventForm form) {
        Event event = new Event();
        apply(event, form);
        return eventRepository.save(event);
    }

    @Transactional
    public Event updateEvent(Long eventId, EventForm form) {
        Event event = getEvent(eventId);
        apply(event, form);
        return event;
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = getEvent(eventId);
        eventRepository.delete(event);
    }

    private void apply(Event event, EventForm form) {
        event.setEventDate(form.getEventDate());
        event.setStartTime(form.getStartTime());
        event.setEndTime(form.getEndTime());
        event.setPlace(form.getPlace());
        event.setCapacity(form.getCapacity());
        event.setDescription(form.getDescription());
    }
}
