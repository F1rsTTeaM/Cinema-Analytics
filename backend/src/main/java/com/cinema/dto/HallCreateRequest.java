package com.cinema.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HallCreateRequest {

    @NotBlank(message = "Название зала обязательно")
    private String name;

    @NotNull(message = "Количество рядов обязательно")
    @Min(value = 1, message = "Минимум 1 ряд")
    @Max(value = 20, message = "Максимум 20 рядов")
    private Integer rowsCount;

    @NotNull(message = "Количество мест в ряду обязательно")
    @Min(value = 1, message = "Минимум 1 место")
    @Max(value = 30, message = "Максимум 30 мест в ряду")
    private Integer seatsPerRow;
}