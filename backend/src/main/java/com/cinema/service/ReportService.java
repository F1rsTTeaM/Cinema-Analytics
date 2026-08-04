package com.cinema.service;

import com.cinema.dto.ReportDTO;
import com.cinema.model.Session;
import com.cinema.model.ProductSale;
import com.cinema.repository.SessionRepository;
import com.cinema.repository.ProductSaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private ProductSaleRepository productSaleRepository;

    public ReportDTO getTicketSummaryReport(LocalDateTime start, LocalDateTime end) {
        List<Session> sessions = sessionRepository.findCompletedSessionsInPeriod(start, end);
        
        Map<String, Object> data = new HashMap<>();
        
        BigDecimal totalRevenue = sessions.stream()
                .map(Session::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Long totalTickets = sessions.stream()
                .mapToLong(Session::getSoldCount)
                .sum();
        
        Long totalSessions = (long) sessions.size();
        
        Map<String, Long> movieTickets = sessions.stream()
                .collect(Collectors.groupingBy(
                    s -> s.getMovie().getTitle(),
                    Collectors.summingLong(Session::getSoldCount)
                ));
        
        String topMovie = movieTickets.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Нет данных");
        
        Long topMovieTickets = movieTickets.getOrDefault(topMovie, 0L);
        
        Map<String, Long> hallTickets = sessions.stream()
                .collect(Collectors.groupingBy(
                    s -> s.getHall().getName(),
                    Collectors.summingLong(Session::getSoldCount)
                ));
        
        String topHall = hallTickets.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Нет данных");
        
        Long topHallTickets = hallTickets.getOrDefault(topHall, 0L);
        
        data.put("totalRevenue", totalRevenue);
        data.put("totalTickets", totalTickets);
        data.put("totalSessions", totalSessions);
        data.put("topMovie", topMovie);
        data.put("topMovieTickets", topMovieTickets);
        data.put("topHall", topHall);
        data.put("topHallTickets", topHallTickets);
        data.put("period", start + " - " + end);

        Map<String, BigDecimal> dailyRevenue = sessions.stream()
                .collect(Collectors.groupingBy(
                    s -> s.getStartTime().toLocalDate().toString(),
                    Collectors.reducing(BigDecimal.ZERO, Session::getTotalAmount, BigDecimal::add)
                ));
        
        List<String> dates = new ArrayList<>(dailyRevenue.keySet());
        Collections.sort(dates);
        
        List<Number> revenueValues = dates.stream()
                .map(dailyRevenue::get)
                .map(BigDecimal::doubleValue)
                .collect(Collectors.toList());

        ReportDTO.ChartData chart = new ReportDTO.ChartData();
        chart.setType("line");
        chart.setTitle("Динамика выручки по дням");
        chart.setLabels(dates);
        chart.setValues(revenueValues);

        ReportDTO report = new ReportDTO();
        report.setReportName("Общая статистика продаж билетов");
        report.setReportType("TICKET_SUMMARY");
        report.setGeneratedAt(LocalDateTime.now());
        report.setData(data);
        report.setCharts(List.of(chart));

        return report;
    }

    public ReportDTO getMovieReport(LocalDateTime start, LocalDateTime end) {
        List<Session> sessions = sessionRepository.findCompletedSessionsInPeriod(start, end);
        
        Map<String, Map<String, Object>> movieMap = new LinkedHashMap<>();
        
        for (Session s : sessions) {
            String title = s.getMovie().getTitle();
            movieMap.putIfAbsent(title, new LinkedHashMap<>());
            Map<String, Object> stats = movieMap.get(title);
            
            stats.put("movieTitle", title);
            stats.put("genre", s.getMovie().getGenre());
            stats.put("sessionsCount", ((Long) stats.getOrDefault("sessionsCount", 0L)) + 1);
            stats.put("ticketsSold", ((Long) stats.getOrDefault("ticketsSold", 0L)) + s.getSoldCount());
            stats.put("totalRevenue", ((BigDecimal) stats.getOrDefault("totalRevenue", BigDecimal.ZERO))
                    .add(s.getTotalAmount()));
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("movieStats", new ArrayList<>(movieMap.values()));
        data.put("totalMovies", movieMap.size());
        data.put("period", start + " - " + end);

        List<Map<String, Object>> sortedMovies = new ArrayList<>(movieMap.values());
        sortedMovies.sort((a, b) -> ((BigDecimal) b.get("totalRevenue"))
                .compareTo((BigDecimal) a.get("totalRevenue")));
        
        List<String> labels = sortedMovies.stream()
                .limit(10)
                .map(m -> (String) m.get("movieTitle"))
                .collect(Collectors.toList());
        List<Number> values = sortedMovies.stream()
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
        report.setReportType("TICKET_MOVIES");
        report.setGeneratedAt(LocalDateTime.now());
        report.setData(data);
        report.setCharts(List.of(chart));

        return report;
    }

    public ReportDTO getHallReport(LocalDateTime start, LocalDateTime end) {
        List<Session> sessions = sessionRepository.findCompletedSessionsInPeriod(start, end);
        
        Map<String, Map<String, Object>> hallMap = new LinkedHashMap<>();
        
        for (Session s : sessions) {
            String name = s.getHall().getName();
            hallMap.putIfAbsent(name, new LinkedHashMap<>());
            Map<String, Object> stats = hallMap.get(name);
            
            stats.put("hallName", name);
            stats.put("capacity", s.getHall().getCapacity());
            stats.put("sessionsCount", ((Long) stats.getOrDefault("sessionsCount", 0L)) + 1);
            stats.put("ticketsSold", ((Long) stats.getOrDefault("ticketsSold", 0L)) + s.getSoldCount());
            stats.put("totalRevenue", ((BigDecimal) stats.getOrDefault("totalRevenue", BigDecimal.ZERO))
                    .add(s.getTotalAmount()));
            
            double occupancy = (double) s.getSoldCount() / s.getHall().getCapacity() * 100;
            stats.put("occupancyPercent", Math.round(occupancy * 10.0) / 10.0);
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("hallStats", new ArrayList<>(hallMap.values()));
        data.put("totalHalls", hallMap.size());
        data.put("period", start + " - " + end);

        List<Map<String, Object>> sortedHalls = new ArrayList<>(hallMap.values());
        sortedHalls.sort((a, b) -> ((BigDecimal) b.get("totalRevenue"))
                .compareTo((BigDecimal) a.get("totalRevenue")));
        
        List<String> labels = sortedHalls.stream()
                .map(h -> (String) h.get("hallName"))
                .collect(Collectors.toList());
        List<Number> values = sortedHalls.stream()
                .map(h -> ((BigDecimal) h.get("totalRevenue")).doubleValue())
                .collect(Collectors.toList());

        ReportDTO.ChartData chart = new ReportDTO.ChartData();
        chart.setType("bar");
        chart.setTitle("Выручка по залам");
        chart.setLabels(labels);
        chart.setValues(values);

        ReportDTO report = new ReportDTO();
        report.setReportName("Отчет по продажам билетов по залам");
        report.setReportType("TICKET_HALLS");
        report.setGeneratedAt(LocalDateTime.now());
        report.setData(data);
        report.setCharts(List.of(chart));

        return report;
    }

    public ReportDTO getDailyReport(LocalDateTime start, LocalDateTime end) {
        List<Session> sessions = sessionRepository.findCompletedSessionsInPeriod(start, end);
        
        Map<String, Map<String, Object>> dailyMap = new LinkedHashMap<>();
        
        for (Session s : sessions) {
            String date = s.getStartTime().toLocalDate().toString();
            dailyMap.putIfAbsent(date, new LinkedHashMap<>());
            Map<String, Object> stats = dailyMap.get(date);
            
            stats.put("date", date);
            stats.put("sessionsCount", ((Long) stats.getOrDefault("sessionsCount", 0L)) + 1);
            stats.put("ticketsSold", ((Long) stats.getOrDefault("ticketsSold", 0L)) + s.getSoldCount());
            stats.put("revenue", ((BigDecimal) stats.getOrDefault("revenue", BigDecimal.ZERO))
                    .add(s.getTotalAmount()));
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("dailyStats", new ArrayList<>(dailyMap.values()));
        data.put("daysCount", dailyMap.size());
        data.put("period", start + " - " + end);

        List<String> dates = new ArrayList<>(dailyMap.keySet());
        Collections.sort(dates);
        
        List<Number> ticketValues = dates.stream()
                .map(d -> ((Long) dailyMap.get(d).get("ticketsSold")))
                .collect(Collectors.toList());
        
        List<Number> revenueValues = dates.stream()
                .map(d -> ((BigDecimal) dailyMap.get(d).get("revenue")).doubleValue())
                .collect(Collectors.toList());

        ReportDTO.ChartData chart1 = new ReportDTO.ChartData();
        chart1.setType("line");
        chart1.setTitle("Количество проданных билетов по дням");
        chart1.setLabels(dates);
        chart1.setValues(ticketValues);

        ReportDTO.ChartData chart2 = new ReportDTO.ChartData();
        chart2.setType("line");
        chart2.setTitle("Выручка по дням (₽)");
        chart2.setLabels(dates);
        chart2.setValues(revenueValues);

        ReportDTO report = new ReportDTO();
        report.setReportName("Отчет по продажам билетов по дням");
        report.setReportType("TICKET_DAILY");
        report.setGeneratedAt(LocalDateTime.now());
        report.setData(data);
        report.setCharts(List.of(chart1, chart2));

        return report;
    }

    public ReportDTO getProductSummaryReport(LocalDateTime start, LocalDateTime end) {
        List<ProductSale> sales = productSaleRepository.findBySaleDateBetween(start, end);
        
        Map<String, Object> data = new HashMap<>();
        
        BigDecimal totalRevenue = sales.stream()
                .map(ps -> ps.getProduct().getPrice().multiply(BigDecimal.valueOf(ps.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Long totalItems = sales.stream()
                .mapToLong(ProductSale::getCount)
                .sum();
        
        Map<String, Long> productCounts = sales.stream()
                .collect(Collectors.groupingBy(
                    ps -> ps.getProduct().getName(),
                    Collectors.summingLong(ProductSale::getCount)
                ));
        
        String topProduct = productCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Нет данных");
        
        Long topProductCount = productCounts.getOrDefault(topProduct, 0L);
        
        data.put("totalRevenue", totalRevenue);
        data.put("totalItems", totalItems);
        data.put("topProduct", topProduct);
        data.put("topProductCount", topProductCount);
        data.put("period", start + " - " + end);

        Map<String, BigDecimal> dailyRevenue = sales.stream()
                .collect(Collectors.groupingBy(
                    ps -> ps.getSaleDate().toLocalDate().toString(),
                    Collectors.reducing(
                        BigDecimal.ZERO,
                        ps -> ps.getProduct().getPrice().multiply(BigDecimal.valueOf(ps.getCount())),
                        BigDecimal::add
                    )
                ));
        
        List<String> dates = new ArrayList<>(dailyRevenue.keySet());
        Collections.sort(dates);
        
        List<Number> revenueValues = dates.stream()
                .map(dailyRevenue::get)
                .map(BigDecimal::doubleValue)
                .collect(Collectors.toList());

        ReportDTO.ChartData chart = new ReportDTO.ChartData();
        chart.setType("line");
        chart.setTitle("Динамика выручки от товаров");
        chart.setLabels(dates);
        chart.setValues(revenueValues);

        ReportDTO report = new ReportDTO();
        report.setReportName("Общая статистика продаж товаров");
        report.setReportType("PRODUCT_SUMMARY");
        report.setGeneratedAt(LocalDateTime.now());
        report.setData(data);
        report.setCharts(List.of(chart));

        return report;
    }

    public ReportDTO getProductListReport(LocalDateTime start, LocalDateTime end) {
        List<ProductSale> sales = productSaleRepository.findBySaleDateBetween(start, end);
        
        Map<String, Map<String, Object>> productMap = new LinkedHashMap<>();
        
        for (ProductSale ps : sales) {
            String name = ps.getProduct().getName();
            productMap.putIfAbsent(name, new LinkedHashMap<>());
            Map<String, Object> stats = productMap.get(name);
            
            stats.put("productName", name);
            stats.put("totalSold", ((Long) stats.getOrDefault("totalSold", 0L)) + ps.getCount());
            stats.put("totalRevenue", ((BigDecimal) stats.getOrDefault("totalRevenue", BigDecimal.ZERO))
                    .add(ps.getProduct().getPrice().multiply(BigDecimal.valueOf(ps.getCount()))));
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("productStats", new ArrayList<>(productMap.values()));
        data.put("totalProducts", productMap.size());
        data.put("period", start + " - " + end);

        List<Map<String, Object>> sortedProducts = new ArrayList<>(productMap.values());
        sortedProducts.sort((a, b) -> ((BigDecimal) b.get("totalRevenue"))
                .compareTo((BigDecimal) a.get("totalRevenue")));
        
        List<String> labels = sortedProducts.stream()
                .limit(10)
                .map(p -> (String) p.get("productName"))
                .collect(Collectors.toList());
        List<Number> values = sortedProducts.stream()
                .limit(10)
                .map(p -> ((BigDecimal) p.get("totalRevenue")).doubleValue())
                .collect(Collectors.toList());

        ReportDTO.ChartData chart = new ReportDTO.ChartData();
        chart.setType("bar");
        chart.setTitle("Выручка по товарам");
        chart.setLabels(labels);
        chart.setValues(values);

        ReportDTO report = new ReportDTO();
        report.setReportName("Отчет по продажам товаров");
        report.setReportType("PRODUCT_LIST");
        report.setGeneratedAt(LocalDateTime.now());
        report.setData(data);
        report.setCharts(List.of(chart));

        return report;
    }

    public ReportDTO getProductDailyReport(LocalDateTime start, LocalDateTime end) {
        List<ProductSale> sales = productSaleRepository.findBySaleDateBetween(start, end);
        
        Map<String, Map<String, Object>> dailyMap = new LinkedHashMap<>();
        
        for (ProductSale ps : sales) {
            String date = ps.getSaleDate().toLocalDate().toString();
            dailyMap.putIfAbsent(date, new LinkedHashMap<>());
            Map<String, Object> stats = dailyMap.get(date);
            
            stats.put("date", date);
            stats.put("itemsSold", ((Long) stats.getOrDefault("itemsSold", 0L)) + ps.getCount());
            stats.put("revenue", ((BigDecimal) stats.getOrDefault("revenue", BigDecimal.ZERO))
                    .add(ps.getProduct().getPrice().multiply(BigDecimal.valueOf(ps.getCount()))));
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("dailyStats", new ArrayList<>(dailyMap.values()));
        data.put("daysCount", dailyMap.size());
        data.put("period", start + " - " + end);

        List<String> dates = new ArrayList<>(dailyMap.keySet());
        Collections.sort(dates);
        
        List<Number> itemValues = dates.stream()
                .map(d -> ((Long) dailyMap.get(d).get("itemsSold")))
                .collect(Collectors.toList());
        
        List<Number> revenueValues = dates.stream()
                .map(d -> ((BigDecimal) dailyMap.get(d).get("revenue")).doubleValue())
                .collect(Collectors.toList());

        ReportDTO.ChartData chart1 = new ReportDTO.ChartData();
        chart1.setType("line");
        chart1.setTitle("Количество проданных товаров по дням");
        chart1.setLabels(dates);
        chart1.setValues(itemValues);

        ReportDTO.ChartData chart2 = new ReportDTO.ChartData();
        chart2.setType("line");
        chart2.setTitle("Выручка от товаров по дням");
        chart2.setLabels(dates);
        chart2.setValues(revenueValues);

        ReportDTO report = new ReportDTO();
        report.setReportName("Отчет по продажам товаров по дням");
        report.setReportType("PRODUCT_DAILY");
        report.setGeneratedAt(LocalDateTime.now());
        report.setData(data);
        report.setCharts(List.of(chart1, chart2));

        return report;
    }
}