package com.cinema.controller;

import com.cinema.dto.ReportDTO;
import com.cinema.dto.SessionCreateRequest;
import com.cinema.dto.SessionDTO;
import com.cinema.dto.SessionPurchaseRequest;
import com.cinema.service.SessionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@Slf4j
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @GetMapping
    public ResponseEntity<List<SessionDTO>> getAllSessions() {
        log.info("GET /api/sessions - запрос на получение списка всех сеансов");
        List<SessionDTO> sessions = sessionService.getAllSessions();
        log.info("GET /api/sessions - возвращено {} сеансов", sessions.size());

        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionDTO> getSessionById(@PathVariable Long id) {
        log.info("GET /api/sessions/{} - запрос сеанса по id", id);
        SessionDTO session = sessionService.getSessionById(id);
        log.info("GET /api/sessions/{} - найден сеанс: фильм='{}', зал='{}'",
                id, session.getMovieTitle(), session.getHallName());

        return ResponseEntity.ok(session);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<SessionDTO>> getUpcomingSessions() {
        log.info("GET /api/sessions/upcoming - запрос предстоящих сеансов");
        List<SessionDTO> sessions = sessionService.getUpcomingSessions();
        log.info("GET /api/sessions/upcoming - возвращено {} сеансов", sessions.size());

        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<SessionDTO>> getSessionsByMovie(@PathVariable Long movieId) {
        log.info("GET /api/sessions/movie/{} - запрос сеансов фильма", movieId);
        List<SessionDTO> sessions = sessionService.getSessionsByMovie(movieId);
        log.info("GET /api/sessions/movie/{} - возвращено {} сеансов", movieId, sessions.size());

        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/hall/{hallId}")
    public ResponseEntity<List<SessionDTO>> getSessionsByHall(@PathVariable Long hallId) {
        log.info("GET /api/sessions/hall/{} - запрос сеансов зала", hallId);
        List<SessionDTO> sessions = sessionService.getSessionsByHall(hallId);
        log.info("GET /api/sessions/hall/{} - возвращено {} сеансов", hallId, sessions.size());

        return ResponseEntity.ok(sessions);
    }

    @PostMapping
    public ResponseEntity<SessionDTO> createSession(@Valid @RequestBody SessionCreateRequest request) {
        log.info("POST /api/sessions - запрос на создание сеанса: movieId={}, hallId={}, {} - {}",
                request.getMovieId(), request.getHallId(),
                request.getStartTime(), request.getEndTime());
        SessionDTO created = sessionService.createSession(request);
        log.info("POST /api/sessions - сеанс успешно создан с id={}", created.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SessionDTO> updateSessionStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("PATCH /api/sessions/{}/status - смена статуса на '{}'", id, status);
        SessionDTO updated = sessionService.updateSessionStatus(id, status);
        log.info("PATCH /api/sessions/{}/status - статус изменён на '{}'", id, updated.getStatus());

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        log.info("DELETE /api/sessions/{} - запрос на удаление сеанса", id);
        sessionService.deleteSession(id);
        log.info("DELETE /api/sessions/{} - сеанс успешно удалён", id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/purchase")
    public ResponseEntity<SessionDTO> purchaseTickets(@RequestBody SessionPurchaseRequest request) {
        log.info("POST /api/sessions/purchase - покупка билетов: sessionId={}, места={}",
                request.getSessionId(), request.getSeats());
        SessionDTO session = sessionService.purchaseTickets(request);
        log.info("POST /api/sessions/purchase - билеты проданы, продано мест: {}",
                session.getSoldCount());

        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @GetMapping("/{sessionId}/occupied")
    public ResponseEntity<List<String>> getOccupiedSeats(@PathVariable Long sessionId) {
        log.info("GET /api/sessions/{}/occupied - занятые места", sessionId);
        List<String> seats = sessionService.getOccupiedSeats(sessionId);
        log.info("GET /api/sessions/{}/occupied - занято {} мест", sessionId, seats.size());

        return ResponseEntity.ok(seats);
    }

    @GetMapping("/{sessionId}/available")
    public ResponseEntity<List<String>> getAvailableSeats(@PathVariable Long sessionId) {
        log.info("GET /api/sessions/{}/available - свободные места", sessionId);
        List<String> seats = sessionService.getAvailableSeats(sessionId);
        log.info("GET /api/sessions/{}/available - свободно {} мест", sessionId, seats.size());

        return ResponseEntity.ok(seats);
    }

    @GetMapping("/revenue")
    public ResponseEntity<BigDecimal> getRevenue(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        log.info("GET /api/sessions/revenue - период: {} - {}", start, end);
        BigDecimal revenue = sessionService.getTotalRevenue(start, end);
        log.info("GET /api/sessions/revenue - выручка: {}", revenue);

        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/reports/movies")
    public ResponseEntity<ReportDTO> getMovieReport(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        log.info("GET /api/sessions/reports/movies - период: {} - {}", start, end);

        return ResponseEntity.ok(sessionService.getMovieReport(start, end));
    }

    @GetMapping("/reports/halls")
    public ResponseEntity<ReportDTO> getHallReport(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        log.info("GET /api/sessions/reports/halls - период: {} - {}", start, end);

        return ResponseEntity.ok(sessionService.getHallReport(start, end));
    }

    @GetMapping("/reports/daily")
    public ResponseEntity<ReportDTO> getDailyReport(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        log.info("GET /api/sessions/reports/daily - период: {} - {}", start, end);

        return ResponseEntity.ok(sessionService.getDailyReport(start, end));
    }

    @GetMapping("/reports/summary")
    public ResponseEntity<ReportDTO> getSummaryReport(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        log.info("GET /api/sessions/reports/summary - период: {} - {}", start, end);

        return ResponseEntity.ok(sessionService.getSummaryReport(start, end));
    }
}