package com.cinema.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
    @DecimalMin(value = "100.00", message = "Цена билета должна быть не менее 100 ₽")
    @DecimalMax(value = "10000.00", message = "Цена билета не должна превышать 10 000 ₽")
    private BigDecimal ticketPrice;
}