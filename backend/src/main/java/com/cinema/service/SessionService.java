package com.cinema.service;

import com.cinema.dto.ReportDTO;
import com.cinema.dto.SessionCreateRequest;
import com.cinema.dto.SessionDTO;
import com.cinema.dto.SessionPurchaseRequest;
import com.cinema.exception.HallExceptions;
import com.cinema.exception.MovieExceptions;
import com.cinema.exception.SessionExceptions;
import com.cinema.model.Hall;
import com.cinema.model.Movie;
import com.cinema.model.Session;
import com.cinema.model.SessionStatus;
import com.cinema.repository.HallRepository;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.SessionRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class SessionService {
    private static final long MIN_SESSION_DURATION_MINUTES = 60;
    private static final long MAX_MONTHS_AHEAD = 3;
    private static final BigDecimal MIN_TICKET_PRICE = new BigDecimal("100.00");
    private static final BigDecimal MAX_TICKET_PRICE = new BigDecimal("10000.00");

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private HallRepository hallRepository;

    public List<SessionDTO> getAllSessions() {
        log.debug("Получение списка всех сеансов");
        List<Session> sessions = sessionRepository.findAll();
        log.debug("Из БД получено {} сеансов", sessions.size());

        return sessions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public SessionDTO getSessionById(Long id) {
        log.debug("Поиск сеанса по id={}", id);
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Сеанс с id={} не найден", id);
                    return new SessionExceptions.NotFound(id);
                });
        log.debug("Сеанс с id={} найден: фильм='{}', зал='{}'",
                id, session.getMovie().getTitle(), session.getHall().getName());

        return convertToDTO(session);
    }

    public List<SessionDTO> getUpcomingSessions() {
        log.debug("Получение списка предстоящих сеансов");
        List<Session> upcoming = sessionRepository.findUpcomingSessions(LocalDateTime.now());
        log.debug("Найдено {} предстоящих сеансов", upcoming.size());

        return upcoming.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<SessionDTO> getSessionsByMovie(Long movieId) {
        log.debug("Получение сеансов для фильма id={}", movieId);
        List<Session> sessions = sessionRepository.findByMovieId(movieId);
        log.debug("Для фильма id={} найдено {} сеансов", movieId, sessions.size());

        return sessions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<SessionDTO> getSessionsByHall(Long hallId) {
        log.debug("Получение сеансов для зала id={}", hallId);
        List<Session> sessions = sessionRepository.findByHallId(hallId);
        log.debug("Для зала id={} найдено {} сеансов", hallId, sessions.size());

        return sessions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public SessionDTO createSession(SessionCreateRequest request) {
        log.debug("Создание сеанса: movieId={}, hallId={}, {} - {}",
                request.getMovieId(), request.getHallId(),
                request.getStartTime(), request.getEndTime());

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> {
                    log.warn("Фильм id={} не найден при создании сеанса", request.getMovieId());
                    return new MovieExceptions.NotFound(request.getMovieId());
                });

        Hall hall = hallRepository.findById(request.getHallId())
                .orElseThrow(() -> {
                    log.warn("Зал id={} не найден при создании сеанса", request.getHallId());
                    return new HallExceptions.NotFound(request.getHallId());
                });

        if (request.getEndTime().isBefore(request.getStartTime())
                || request.getEndTime().equals(request.getStartTime())) {
            log.warn("Некорректный диапазон времени сеанса: {} - {}",
                    request.getStartTime(), request.getEndTime());
            throw new SessionExceptions.InvalidTimeRange();
        }

        long duration = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        if (duration < MIN_SESSION_DURATION_MINUTES) {
            log.warn("Слишком короткий сеанс: {} мин (мин. {})",
                    duration, MIN_SESSION_DURATION_MINUTES);
            throw new SessionExceptions.DurationTooShort(duration, MIN_SESSION_DURATION_MINUTES);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime maxDate = now.plusMonths(MAX_MONTHS_AHEAD);

        if (request.getStartTime().isBefore(todayStart)) {
            log.warn("Начало сеанса раньше сегодня: {}", request.getStartTime());
            throw new SessionExceptions.InvalidData(
                    "Начало сеанса не может быть раньше сегодняшнего дня");
        }
        if (request.getStartTime().isAfter(maxDate)) {
            log.warn("Начало сеанса позже {} мес.: {}", MAX_MONTHS_AHEAD, request.getStartTime());
            throw new SessionExceptions.InvalidData(
                    "Начало сеанса не может быть позже " + MAX_MONTHS_AHEAD + " месяцев от текущей даты");
        }
        if (request.getEndTime().isBefore(todayStart)) {
            log.warn("Окончание сеанса раньше сегодня: {}", request.getEndTime());
            throw new SessionExceptions.InvalidData(
                    "Окончание сеанса не может быть раньше сегодняшнего дня");
        }
        if (request.getEndTime().isAfter(maxDate)) {
            log.warn("Окончание сеанса позже {} мес.: {}", MAX_MONTHS_AHEAD, request.getEndTime());
            throw new SessionExceptions.InvalidData(
                    "Окончание сеанса не может быть позже " + MAX_MONTHS_AHEAD + " месяцев от текущей даты");
        }

        if (request.getTicketPrice() == null
                || request.getTicketPrice().compareTo(MIN_TICKET_PRICE) < 0
                || request.getTicketPrice().compareTo(MAX_TICKET_PRICE) > 0) {
            log.warn("Некорректная цена билета: {}", request.getTicketPrice());
            throw new SessionExceptions.InvalidData(
                    "Цена билета должна быть в диапазоне от "
                            + MIN_TICKET_PRICE + " до " + MAX_TICKET_PRICE + " ₽");
        }

        List<Session> overlapping = sessionRepository.findOverlappingSessions(
                request.getHallId(), request.getStartTime(), request.getEndTime());
        if (!overlapping.isEmpty()) {
            log.warn("Пересечение сеансов в зале id={} на {} - {}; конфликтов: {}",
                    request.getHallId(), request.getStartTime(), request.getEndTime(),
                    overlapping.size());
            throw new SessionExceptions.TimeOverlap(request.getHallId());
        }

        Session session = new Session(
                movie, hall,
                request.getStartTime(), request.getEndTime(),
                request.getTicketPrice());

        Session saved = sessionRepository.save(session);
        log.info("Сеанс успешно создан: id={}, фильм='{}', зал='{}', {} - {}",
                saved.getId(), movie.getTitle(), hall.getName(),
                saved.getStartTime(), saved.getEndTime());

        return convertToDTO(saved);
    }

    public SessionDTO updateSessionStatus(Long id, String status) {
        log.debug("Обновление статуса сеанса id={} на '{}'", id, status);

        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Сеанс с id={} не найден", id);
                    return new SessionExceptions.NotFound(id);
                });

        SessionStatus newStatus;
        try {
            newStatus = SessionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Некорректный статус сеанса: '{}'", status);
            throw new SessionExceptions.InvalidStatus(status);
        }

        SessionStatus oldStatus = session.getStatus();
        session.setStatus(newStatus);
        Session saved = sessionRepository.save(session);

        log.info("Статус сеанса id={} изменён: {} -> {}", id, oldStatus, newStatus);

        return convertToDTO(saved);
    }

    public void deleteSession(Long id) {
        log.debug("Удаление сеанса id={}", id);
        if (!sessionRepository.existsById(id)) {
            log.warn("Невозможно удалить: сеанс id={} не найден", id);
            throw new SessionExceptions.NotFound(id);
        }
        sessionRepository.deleteById(id);
        log.info("Сеанс id={} успешно удалён", id);
    }

    public SessionDTO purchaseTickets(SessionPurchaseRequest request) {
        log.debug("Покупка билетов: sessionId={}, места={}",
                request.getSessionId(), request.getSeats());

        if (request.getSeats() == null || request.getSeats().isEmpty()) {
            log.warn("Не выбрано ни одного места для покупки");
            throw new SessionExceptions.NoSeatsSelected();
        }

        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> {
                    log.warn("Сеанс id={} не найден", request.getSessionId());
                    return new SessionExceptions.NotFound(request.getSessionId());
                });

        if (session.getStatus() == SessionStatus.CANCELLED) {
            log.warn("Покупка на отменённый сеанс id={}", session.getId());
            throw new SessionExceptions.Cancelled(session.getId());
        }
        if (session.getStatus() == SessionStatus.COMPLETED) {
            log.warn("Покупка на завершённый сеанс id={}", session.getId());
            throw new SessionExceptions.Completed(session.getId());
        }

        for (String seat : request.getSeats()) {
            if (!session.isSeatAvailable(seat)) {
                log.warn("Место '{}' уже занято на сеансе id={}", seat, session.getId());
                throw new SessionExceptions.SeatOccupied(seat);
            }
        }

        for (String seat : request.getSeats()) {
            session.addSoldSeat(seat);
        }

        if (session.getSoldCount() >= session.getHall().getCapacity()) {
            session.setStatus(SessionStatus.SOLD_OUT);
            log.info("Сеанс id={} полностью распродан", session.getId());
        }

        Session saved = sessionRepository.save(session);
        log.info("Билеты проданы: sessionId={}, места={}, всего продано={}",
                saved.getId(), request.getSeats(), saved.getSoldCount());

        return convertToDTO(saved);
    }

    public List<String> getOccupiedSeats(Long sessionId) {
        log.debug("Получение занятых мест для сеанса id={}", sessionId);
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    log.warn("Сеанс id={} не найден", sessionId);
                    return new SessionExceptions.NotFound(sessionId);
                });
        List<String> occupied = session.getSoldSeats();
        log.debug("Сеанс id={}: занято {} мест", sessionId, occupied.size());

        return occupied;
    }

    public List<String> getAvailableSeats(Long sessionId) {
        log.debug("Получение свободных мест для сеанса id={}", sessionId);
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    log.warn("Сеанс id={} не найден", sessionId);
                    return new SessionExceptions.NotFound(sessionId);
                });
        List<String> available = session.getAvailableSeats();
        log.debug("Сеанс id={}: свободно {} мест", sessionId, available.size());

        return available;
    }

    public BigDecimal getTotalRevenue(LocalDateTime start, LocalDateTime end) {
        validatePeriod(start, end);
        log.debug("Расчёт выручки за период {} - {}", start, end);
        BigDecimal revenue = sessionRepository.getTotalRevenue(start, end);
        log.info("Выручка за период {} - {}: {}", start, end, revenue);

        return revenue;
    }

    public ReportDTO getMovieReport(LocalDateTime start, LocalDateTime end) {
        validatePeriod(start, end);
        log.debug("Формирование отчёта MOVIE_REPORT за период {} - {}", start, end);

        List<Object[]> stats = sessionRepository.getMovieStats(start, end);

        log.debug("Получено {} строк статистики по фильмам", stats.size());

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

        log.info("Отчёт MOVIE_REPORT сформирован");

        return report;
    }

    public ReportDTO getHallReport(LocalDateTime start, LocalDateTime end) {
        validatePeriod(start, end);
        log.debug("Формирование отчёта HALL_REPORT за период {} - {}", start, end);

        List<Object[]> stats = sessionRepository.getHallStats(start, end);

        log.debug("Получено {} строк статистики по залам", stats.size());

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

        log.info("Отчёт HALL_REPORT сформирован");

        return report;
    }

    public ReportDTO getDailyReport(LocalDateTime start, LocalDateTime end) {
        validatePeriod(start, end);
        log.debug("Формирование отчёта DAILY_REPORT за период {} - {}", start, end);

        List<Object[]> stats = sessionRepository.getDailyTrends(start, end);

        log.debug("Получено {} строк статистики", stats.size());

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

        log.info("Отчёт DAILY_REPORT сформирован");

        return report;
    }

    public ReportDTO getSummaryReport(LocalDateTime start, LocalDateTime end) {
        validatePeriod(start, end);
        log.debug("Формирование отчёта SUMMARY_REPORT за период {} - {}", start, end);

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

        log.info("Отчёт SUMMARY_REPORT сформирован");

        return report;
    }

    private void validatePeriod(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            log.warn("Некорректный период: start={}, end={}", start, end);
            throw new SessionExceptions.InvalidPeriod(
                    "Дата начала и конца периода обязательны");
        }
        if (start.isAfter(end)) {
            log.warn("Некорректный период: start={} позже end={}", start, end);
            throw new SessionExceptions.InvalidPeriod(
                    "Дата начала не может быть позже даты конца");
        }
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