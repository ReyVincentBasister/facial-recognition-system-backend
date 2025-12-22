package com.example.attendance.event;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// ===========================
// 📌 ENTITIES
// ===========================
@Entity
@Table(name = "event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class Event {
    @Id
    private String id;
    private String name;
    private LocalDateTime startTime;
    private String description;
}

@Entity
@Table(name = "system_state")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class SystemState {
    @Id
    // Changed column name to "key" to match your Drizzle schema. 
    // Note: "key" is a reserved word in SQL. If you see SQL syntax errors, 
    // you might need to use @Column(name = "\"key\"") to quote it.
    @Column(name = "key") 
    private String key; 
    private String value;
}

// ===========================
// 📌 DTOs
// ===========================
@Data
class CreateEventDTO {
    private String name;
    private LocalDateTime startTime;
    private String description;
}

@Data
class UpdateEventDTO {
    private String name;
    private LocalDateTime startTime;
    private String description;
}

// ===========================
// 📌 REPOSITORIES
// ===========================
interface EventRepository extends JpaRepository<Event, String> {}
interface SystemStateRepository extends JpaRepository<SystemState, String> {}

// ===========================
// 📌 SERVICE
// ===========================
@Service
@RequiredArgsConstructor
class EventService {
    private final EventRepository eventRepository;
    private final SystemStateRepository systemStateRepository;

    public List<Event> getEvents() {
        return eventRepository.findAll();
    }

    public Event createEvent(CreateEventDTO data) {
        Event event = Event.builder()
                .id(UUID.randomUUID().toString())
                .name(data.getName())
                .startTime(data.getStartTime())
                .description(data.getDescription())
                .build();
        return eventRepository.save(event);
    }

    public Event updateEvent(String id, UpdateEventDTO updates) {
        return eventRepository.findById(id).map(event -> {
            if (updates.getName() != null) event.setName(updates.getName());
            if (updates.getStartTime() != null) event.setStartTime(updates.getStartTime());
            if (updates.getDescription() != null) event.setDescription(updates.getDescription());
            return eventRepository.save(event);
        }).orElse(null);
    }

    @Transactional
    public boolean deleteEvent(String id) {
        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
            // Also clear if it was the active event
            clearActiveEvent(id);
            return true;
        }
        return false;
    }

    public Event getEventById(String id) {
        return eventRepository.findById(id).orElse(null);
    }

    // ========================================================
    // 📌 ACTIVE EVENT MANAGEMENT (Matches Drizzle Logic)
    // ========================================================
    @Transactional
    public void setActiveEvent(String eventId) {
        // Match Drizzle logic: if null, delete the entry
        if (eventId == null) {
            systemStateRepository.deleteById("active_event");
            return;
        }
        
        // Match Drizzle logic: insert or update (upsert)
        // JPA's save() method acts as an upsert:
        // 1. Checks if "active_event" exists.
        // 2. If yes, updates it. If no, inserts it.
        SystemState state = new SystemState("active_event", eventId);
        systemStateRepository.save(state);
    }

    public Event getActiveEvent() {
        return systemStateRepository.findById("active_event")
                .map(state -> getEventById(state.getValue()))
                .orElse(null);
    }

    public void clearActiveEvent(String eventId) {
        systemStateRepository.findById("active_event").ifPresent(state -> {
            if (state.getValue().equals(eventId)) {
                systemStateRepository.deleteById("active_event");
            }
        });
    }
}

// ===========================
// 📌 CONTROLLER
// ===========================
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
class EventController {
    private final EventService service;

    @GetMapping
    public List<Event> getAll() {
        return service.getEvents();
    }

    @PostMapping
    public ResponseEntity<Event> create(@RequestBody CreateEventDTO data) {
        return ResponseEntity.ok(service.createEvent(data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getById(@PathVariable String id) {
        Event event = service.getEventById(id);
        return event != null ? ResponseEntity.ok(event) : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Event> update(@PathVariable String id, @RequestBody UpdateEventDTO updates) {
        Event event = service.updateEvent(id, updates);
        return event != null ? ResponseEntity.ok(event) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        return service.deleteEvent(id) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    // Active Event Endpoints
    @GetMapping("/active")
    public ResponseEntity<Event> getActive() {
        Event event = service.getActiveEvent();
        // Return 200 with Event, or 204 No Content if none active
        return event != null ? ResponseEntity.ok(event) : ResponseEntity.noContent().build();
    }

    @PostMapping("/active/{eventId}")
    public ResponseEntity<Void> setActive(@PathVariable String eventId) {
        service.setActiveEvent(eventId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/active")
    public ResponseEntity<Void> clearActive() {
        service.setActiveEvent(null);
        return ResponseEntity.ok().build();
    }
}