package com.cinema.service;

import com.cinema.dto.ReportDTO;
import com.cinema.dto.SessionCreateRequest;
import com.cinema.dto.SessionDTO;
import com.cinema.dto.SessionPurchaseRequest;
import com.cinema.model.Hall;
import com.cinema.model.Movie;
import com.cinema.model.Session;
import com.cinema.model.SessionStatus;
import com.cinema.repository.HallRepository;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private HallRepository hallRepository;

    public List<SessionDTO> getAllSessions() {
        return sessionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public SessionDTO getSessionById(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Сеанс не найден"));
        return convertToDTO(session);
    }

    public List<SessionDTO> getUpcomingSessions() {
        return sessionRepository.findUpcomingSessions(LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SessionDTO> getSessionsByMovie(Long movieId) {
        return sessionRepository.findByMovieId(movieId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SessionDTO> getSessionsByHall(Long hallId) {
        return sessionRepository.findByHallId(hallId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public SessionDTO createSession(SessionCreateRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new RuntimeException("Фильм не найден"));

        Hall hall = hallRepository.findById(request.getHallId())
                .orElseThrow(() -> new RuntimeException("Зал не найден"));

        if (request.getEndTime().isBefore(request.getStartTime()) ||
                request.getEndTime().equals(request.getStartTime())) {
            throw new RuntimeException("Время окончания должно быть позже времени начала");
        }

        long durationMinutes = java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        if (durationMinutes < 30) {
            throw new RuntimeException("Минимальная длительность сеанса — 30 минут");
        }

        List<Session> overlapping = sessionRepository.findOverlappingSessions(
                request.getHallId(),
                request.getStartTime(),
                request.getEndTime());

        if (!overlapping.isEmpty()) {
            throw new RuntimeException("В этом зале уже есть сеанс в указанное время");
        }

        Session session = new Session(
                movie,
                hall,
                request.getStartTime(),
                request.getEndTime(),
                request.getTicketPrice());

        Session saved = sessionRepository.save(session);
        return convertToDTO(saved);
    }

    public SessionDTO updateSessionStatus(Long id, String status) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Сеанс не найден"));

        try {
            SessionStatus newStatus = SessionStatus.valueOf(status.toUpperCase());
            session.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Неверный статус: " + status);
        }

        return convertToDTO(sessionRepository.save(session));
    }

    public void deleteSession(Long id) {
        if (!sessionRepository.existsById(id)) {
            throw new RuntimeException("Сеанс не найден");
        }
        sessionRepository.deleteById(id);
    }

    public SessionDTO purchaseTickets(SessionPurchaseRequest request) {
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Сеанс не найден"));

        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new RuntimeException("Сеанс отменён");
        }

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new RuntimeException("Сеанс уже завершён");
        }

        for (String seat : request.getSeats()) {
            if (!session.isSeatAvailable(seat)) {
                throw new RuntimeException("Место " + seat + " уже занято");
            }
        }

        for (String seat : request.getSeats()) {
            session.addSoldSeat(seat);
        }

        if (session.getSoldCount() >= session.getHall().getCapacity()) {
            session.setStatus(SessionStatus.SOLD_OUT);
        }

        Session saved = sessionRepository.save(session);
        return convertToDTO(saved);
    }

    public List<String> getOccupiedSeats(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Сеанс не найден"));
        return session.getSoldSeats();
    }

    public List<String> getAvailableSeats(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Сеанс не найден"));
        return session.getAvailableSeats();
    }

    public BigDecimal getTotalRevenue(LocalDateTime start, LocalDateTime end) {
        return sessionRepository.getTotalRevenue(start, end);
    }

    public ReportDTO getMovieReport(LocalDateTime start, LocalDateTime end) {
        List<Object[]> stats = sessionRepository.getMovieStats(start, end);

        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> movieStats = new ArrayList<>();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        Long totalTickets = 0L;

        for (Object[] row : stats) {
            Map<String, Object> item = new HashMap<>();
            item.put("movieId", row[0]);
            item.put("movieTitle", row[1]);
            item.put("sessionsCount", row[2]);
            item.put("totalRevenue", row[3]);
            item.put("ticketsSold", row[4]);

            totalRevenue = totalRevenue.add((BigDecimal) row[3]);
            totalTickets += ((Number) row[4]).longValue();
            movieStats.add(item);
        }

        movieStats.sort((a, b) -> ((BigDecimal) b.get("totalRevenue")).compareTo((BigDecimal) a.get("totalRevenue")));

        data.put("movieStats", movieStats);
        data.put("totalRevenue", totalRevenue);
        data.put("totalTickets", totalTickets);
        data.put("totalMovies", movieStats.size());

        List<String> labels = movieStats.stream()
                .limit(10)
                .map(m -> (String) m.get("movieTitle"))
                .collect(Collectors.toList());
        List<Number> values = movieStats.stream()
                .limit(10)
                .map(m -> ((BigDecimal) m.get("totalRevenue")).doubleValue())
                .collect(Collectors.toList());

        ReportDTO.ChartData chart = new ReportDTO.ChartData();
        chart.setType("bar");
        chart.setTitle("Выручка по фильмам");
        chart.setLabels(labels);
        chart.setValues(values);

        ReportDTO report = new ReportDTO();
        report.setReportName("Отчет по продажам билетов по фильмам");
        report.setReportType("MOVIE_REPORT");
        report.setGeneratedAt(LocalDateTime.now());
        report.setData(data);
        report.setCharts(List.of(chart));

        return report;
    }

    public ReportDTO getHallReport(LocalDateTime start, LocalDateTime end) {
        List<Object[]> stats = sessionRepository.getHallStats(start, end);

        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> hallStats = new ArrayList<>();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        Long totalTickets = 0L;

        for (Object[] row : stats) {
            Map<String, Object> item = new HashMap<>();
            item.put("hallId", row[0]);
            item.put("hallName", row[1]);
            item.put("sessionsCount", row[2]);
            item.put("totalRevenue", row[3]);
            item.put("ticketsSold", row[4]);

            totalRevenue = totalRevenue.add((BigDecimal) row[3]);
            totalTickets += ((Number) row[4]).longValue();
            hallStats.add(item);
        }

        hallStats.sort((a, b) -> ((BigDecimal) b.get("totalRevenue")).compareTo((BigDecimal) a.get("totalRevenue")));

        data.put("hallStats", hallStats);
        data.put("totalRevenue", totalRevenue);
        data.put("totalTickets", totalTickets);
        data.put("totalHalls", hallStats.size());

        List<String> labels = hallStats.stream()
                .map(h -> (String) h.get("hallName"))
                .collect(Collectors.toList());
        List<Number> values = hallStats.stream()
                .map(h -> ((BigDecimal) h.get("totalRevenue")).doubleValue())
                .collect(Collectors.toList());

        ReportDTO.ChartData chart = new ReportDTO.ChartData();
        chart.setType("bar");
        chart.setTitle("Выручка по залам");
        chart.setLabels(labels);
        chart.setValues(values);

        ReportDTO report = new ReportDTO();
        report.setReportName("Отчет по продажам билетов по залам");
        report.setReportType("HALL_REPORT");
        report.setGeneratedAt(LocalDateTime.now());
        report.setData(data);
        report.setCharts(List.of(chart));

        return report;
    }

    public ReportDTO getDailyReport(LocalDateTime start, LocalDateTime end) {
        List<Object[]> stats = sessionRepository.getDailyTrends(start, end);

        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> dailyStats = new ArrayList<>();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        Long totalTickets = 0L;

        List<String> dates = new ArrayList<>();
        List<Number> ticketCounts = new ArrayList<>();
        List<Number> revenueValues = new ArrayList<>();

        for (Object[] row : stats) {
            Map<String, Object> item = new HashMap<>();
            String dateStr = row[0].toString();
            Long tickets = (Long) row[1];
            BigDecimal revenue = (BigDecimal) row[2];

            item.put("date", dateStr);
            item.put("ticketsSold", tickets);
            item.put("revenue", revenue);

            totalRevenue = totalRevenue.add(revenue);
            totalTickets += tickets;

            dates.add(dateStr);
            ticketCounts.add(tickets);
            revenueValues.add(revenue.doubleValue());

            dailyStats.add(item);
        }

        data.put("dailyStats", dailyStats);
        data.put("totalRevenue", totalRevenue);
        data.put("totalTickets", totalTickets);
        data.put("daysCount", dailyStats.size());

        ReportDTO.ChartData chart1 = new ReportDTO.ChartData();
        chart1.setType("line");
        chart1.setTitle("Количество проданных билетов по дням");
        chart1.setLabels(dates);
        chart1.setValues(ticketCounts);

        ReportDTO.ChartData chart2 = new ReportDTO.ChartData();
        chart2.setType("line");
        chart2.setTitle("Выручка по дням");
        chart2.setLabels(dates);
        chart2.setValues(revenueValues);

        ReportDTO report = new ReportDTO();
        report.setReportName("Отчет по продажам билетов по дням");
        report.setReportType("DAILY_REPORT");
        report.setGeneratedAt(LocalDateTime.now());
        report.setData(data);
        report.setCharts(List.of(chart1, chart2));

        return report;
    }

    public ReportDTO getSummaryReport(LocalDateTime start, LocalDateTime end) {
        List<Object[]> movieStats = sessionRepository.getMovieStats(start, end);
        List<Object[]> hallStats = sessionRepository.getHallStats(start, end);
        List<Object[]> dailyStats = sessionRepository.getDailyTrends(start, end);

        Map<String, Object> data = new HashMap<>();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        Long totalTickets = 0L;
        Long totalSessions = 0L;

        for (Object[] row : movieStats) {
            totalRevenue = totalRevenue.add((BigDecimal) row[3]);
            totalTickets += ((Number) row[4]).longValue();
            totalSessions += ((Number) row[2]).longValue();
        }

        String topMovie = "";
        Long topMovieTickets = 0L;
        for (Object[] row : movieStats) {
            Long tickets = ((Number) row[4]).longValue();
            if (tickets > topMovieTickets) {
                topMovieTickets = tickets;
                topMovie = (String) row[1];
            }
        }

        String topHall = "";
        Long topHallTickets = 0L;
        for (Object[] row : hallStats) {
            Long tickets = ((Number) row[4]).longValue();
            if (tickets > topHallTickets) {
                topHallTickets = tickets;
                topHall = (String) row[1];
            }
        }

        data.put("totalRevenue", totalRevenue);
        data.put("totalTickets", totalTickets);
        data.put("totalSessions", totalSessions);
        data.put("topMovie", topMovie);
        data.put("topMovieTickets", topMovieTickets);
        data.put("topHall", topHall);
        data.put("topHallTickets", topHallTickets);
        data.put("period", start + " - " + end);

        List<String> labels = dailyStats.stream()
                .map(row -> row[0].toString())
                .collect(Collectors.toList());
        List<Number> values = dailyStats.stream()
                .map(row -> ((BigDecimal) row[2]).doubleValue())
                .collect(Collectors.toList());

        ReportDTO.ChartData chart = new ReportDTO.ChartData();
        chart.setType("line");
        chart.setTitle("Динамика выручки");
        chart.setLabels(labels);
        chart.setValues(values);

        ReportDTO report = new ReportDTO();
        report.setReportName("Общая статистика продаж");
        report.setReportType("SUMMARY_REPORT");
        report.setGeneratedAt(LocalDateTime.now());
        report.setData(data);
        report.setCharts(List.of(chart));

        return report;
    }

    private SessionDTO convertToDTO(Session session) {
        return new SessionDTO(
                session.getId(),
                session.getMovie().getId(),
                session.getMovie().getTitle(),
                session.getMovie().getGenre(),
                session.getHall().getId(),
                session.getHall().getName(),
                session.getHall().getCapacity(),
                session.getStartTime(),
                session.getEndTime(),
                session.getTicketPrice(),
                session.getStatus().name(),
                session.getSoldSeats(),
                session.getTotalAmount(),
                session.getSoldCount(),
                session.getAvailableCount(),
                session.getHall().getCapacity(),
                session.getAvailableSeats());
    }
}