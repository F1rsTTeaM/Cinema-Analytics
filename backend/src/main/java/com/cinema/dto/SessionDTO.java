package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDTO {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private String movieGenre;
    private Long hallId;
    private String hallName;
    private Integer hallCapacity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal ticketPrice;
    private String status;
    private List<String> soldSeats;
    private BigDecimal totalAmount;
    private Integer soldCount;
    private Integer availableCount;
    private Integer capacity;
    private List<String> availableSeats;
}