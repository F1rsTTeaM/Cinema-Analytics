package com.cinema.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {

    private Long id;

    @NotBlank(message = "Название фильма обязательно")
    @Size(max = 256, message = "Название не должно превышать 256 символов")
    private String title;

    @NotBlank(message = "Жанр обязателен")
    @Size(max = 256, message = "Жанр не должен превышать 256 символов")
    private String genre;

    @NotNull(message = "Длительность обязательна")
    @Min(value = 1, message = "Длительность должна быть больше 0")
    @Max(value = 240, message = "Длительность не должна превышать 240 минут")
    private Integer durationMinutes;

    @NotNull(message = "Дата релиза обязательна")
    @PastOrPresent(message = "Дата релиза не может быть в будущем")
    private LocalDateTime releaseDate;

    @AssertTrue(message = "Дата релиза не может быть раньше 1895 года")
    public boolean isValidReleaseDate() {
        return releaseDate == null || !releaseDate.isBefore(LocalDateTime.of(1895, 1, 1, 0, 0));
    }
}
