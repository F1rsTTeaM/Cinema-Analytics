package com.cinema.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cinema.repository.MovieRepository;

import lombok.extern.slf4j.Slf4j;

import com.cinema.dto.MovieDTO;
import com.cinema.exception.MovieExceptions;
import com.cinema.model.Movie;

@Service
@Transactional
@Slf4j 
public class MovieService {
    @Autowired
    private MovieRepository movieRepository;

    public List<MovieDTO> getAllMovies() {
        log.debug("Получение списка всех фильмов из БД");
        List<Movie> movies = movieRepository.findAll();
        log.debug("Из БД получено {} фильмов", movies.size());
        
        return movies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public MovieDTO getMovieById(Long id) {
        log.debug("Поиск фильма по id={}", id);
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с id={} не найден", id);
                    return new MovieExceptions.NotFound(id);
                });
        log.debug("Фильм с id={} найден: '{}'", id, movie.getTitle());
        
        return convertToDTO(movie);
    }

    public MovieDTO createMovie(MovieDTO movieDTO) {
        log.debug("Создание фильма с названием '{}'", movieDTO.getTitle());

        validateMovie(movieDTO);

        if (movieRepository.existsByTitle(movieDTO.getTitle())) {
            log.warn("Попытка создать фильм с уже существующим названием: '{}'", movieDTO.getTitle());
            throw new MovieExceptions.DuplicateTitle(movieDTO.getTitle());
        }

        Movie movie = new Movie(
                movieDTO.getTitle(),
                movieDTO.getGenre(),
                movieDTO.getDurationMinutes(),
                movieDTO.getReleaseDate()
        );

        Movie saved = movieRepository.save(movie);
        log.info("Фильм '{}' успешно создан с id={}", saved.getTitle(), saved.getId());
        
        return convertToDTO(saved);
    }

    public MovieDTO updateMovie(Long id, MovieDTO movieDTO) {
        log.debug("Обновление фильма с id={}", id);

        validateMovie(movieDTO);

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Невозможно обновить: фильм с id={} не найден", id);
                    return new MovieExceptions.NotFound(id);
                });

        if (!movie.getTitle().equals(movieDTO.getTitle())
                && movieRepository.existsByTitle(movieDTO.getTitle())) {
            log.warn("Попытка переименовать фильм id={} в уже занятое название '{}'",
                    id, movieDTO.getTitle());
            throw new MovieExceptions.DuplicateTitle(movieDTO.getTitle());
        }

        log.debug("Обновление полей фильма id={}: '{}' -> '{}'",
                id, movie.getTitle(), movieDTO.getTitle());

        movie.setTitle(movieDTO.getTitle());
        movie.setGenre(movieDTO.getGenre());
        movie.setDurationMinutes(movieDTO.getDurationMinutes());
        movie.setReleaseDate(movieDTO.getReleaseDate());

        Movie updated = movieRepository.save(movie);
        log.info("Фильм с id={} успешно обновлён", id);
        return convertToDTO(updated);
    }

    public void deleteMovie(Long id) {
        log.debug("Удаление фильма с id={}", id);

        if (!movieRepository.existsById(id)) {
            log.warn("Невозможно удалить: фильм с id={} не найден", id);
            throw new MovieExceptions.NotFound(id);
        }

        movieRepository.deleteById(id);
        log.info("Фильм с id={} успешно удалён", id);
    }

    public List<MovieDTO> searchMovies(String query) {
        log.debug("Поиск фильмов по подстроке: '{}'", query);
        List<Movie> found = movieRepository.findByTitleContainingIgnoreCase(query);
        log.debug("По запросу '{}' найдено {} фильмов", query, found.size());
        
        return found.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private void validateMovie(MovieDTO movieDTO) {
        if (movieDTO.getDurationMinutes() == null || movieDTO.getDurationMinutes() <= 0) {
            log.warn("Некорректная длительность фильма: {}", movieDTO.getDurationMinutes());
            throw new MovieExceptions.InvalidDuration(movieDTO.getDurationMinutes());
        }
    }

    private MovieDTO convertToDTO(Movie movie) {
        return new MovieDTO(
            movie.getId(),
            movie.getTitle(),
            movie.getGenre(),
            movie.getDurationMinutes(),
            movie.getReleaseDate()
        );
    }
}
