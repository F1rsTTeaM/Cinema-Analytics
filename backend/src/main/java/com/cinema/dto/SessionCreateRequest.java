package com.cinema.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionCreateRequest {

    @NotNull(message = "ID фильма обязательно")
    private Long movieId;

    @NotNull(message = "ID зала обязателен")
    private Long hallId;

    @NotNull(message = "Время начала обязательно")
    private LocalDateTime startTime;

    @NotNull(message = "Время окончания обязательно")
    private LocalDateTime endTime;

    @NotNull(message = "Цена билета обязательна")
    @Positive(message = "Цена билета должна быть больше 0")
    private BigDecimal ticketPrice;
}