package com.cinema.controller;

import com.cinema.dto.ReportDTO;
import com.cinema.dto.SessionCreateRequest;
import com.cinema.dto.SessionDTO;
import com.cinema.dto.SessionPurchaseRequest;
import com.cinema.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @GetMapping
    public ResponseEntity<List<SessionDTO>> getAllSessions() {
        return ResponseEntity.ok(sessionService.getAllSessions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionDTO> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getSessionById(id));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<SessionDTO>> getUpcomingSessions() {
        return ResponseEntity.ok(sessionService.getUpcomingSessions());
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<SessionDTO>> getSessionsByMovie(@PathVariable Long movieId) {
        return ResponseEntity.ok(sessionService.getSessionsByMovie(movieId));
    }

    @GetMapping("/hall/{hallId}")
    public ResponseEntity<List<SessionDTO>> getSessionsByHall(@PathVariable Long hallId) {
        return ResponseEntity.ok(sessionService.getSessionsByHall(hallId));
    }

    @PostMapping
    public ResponseEntity<SessionDTO> createSession(@Valid @RequestBody SessionCreateRequest request) {
        SessionDTO created = sessionService.createSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SessionDTO> updateSessionStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(sessionService.updateSessionStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/purchase")
    public ResponseEntity<SessionDTO> purchaseTickets(@RequestBody SessionPurchaseRequest request) {
        SessionDTO session = sessionService.purchaseTickets(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @GetMapping("/{sessionId}/occupied")
    public ResponseEntity<List<String>> getOccupiedSeats(@PathVariable Long sessionId) {
        return ResponseEntity.ok(sessionService.getOccupiedSeats(sessionId));
    }

    @GetMapping("/{sessionId}/available")
    public ResponseEntity<List<String>> getAvailableSeats(@PathVariable Long sessionId) {
        return ResponseEntity.ok(sessionService.getAvailableSeats(sessionId));
    }

    @GetMapping("/revenue")
    public ResponseEntity<BigDecimal> getRevenue(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return ResponseEntity.ok(sessionService.getTotalRevenue(start, end));
    }

    @GetMapping("/reports/movies")
    public ResponseEntity<ReportDTO> getMovieReport(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return ResponseEntity.ok(sessionService.getMovieReport(start, end));
    }

    @GetMapping("/reports/halls")
    public ResponseEntity<ReportDTO> getHallReport(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return ResponseEntity.ok(sessionService.getHallReport(start, end));
    }

    @GetMapping("/reports/daily")
    public ResponseEntity<ReportDTO> getDailyReport(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return ResponseEntity.ok(sessionService.getDailyReport(start, end));
    }

    @GetMapping("/reports/summary")
    public ResponseEntity<ReportDTO> getSummaryReport(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return ResponseEntity.ok(sessionService.getSummaryReport(start, end));
    }
}