package com.cinema.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cinema.dto.MovieDTO;
import com.cinema.service.MovieService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/movies")
@Slf4j 
public class MovieController {
    @Autowired
    private MovieService movieService;

    @GetMapping
    public ResponseEntity<List<MovieDTO>> getAllMovies() {
        log.info("GET /api/movies - запрос на получение списка всех фильмов");
        List<MovieDTO> movies = movieService.getAllMovies();
        log.info("GET /api/movies - возвращено {} фильмов", movies.size());
        
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieDTO> getMovieById(@PathVariable Long id) {
        log.info("GET /api/movies/{} - запрос на получение фильма по id", id);
        MovieDTO movie = movieService.getMovieById(id);
        log.info("GET /api/movies/{} - фильм найден: '{}'", id, movie.getTitle());
        
        return ResponseEntity.ok(movie);
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieDTO>> searchMovies(@RequestParam String query) {
        log.info("GET /api/movies/search - поиск фильмов по запросу: '{}'", query);
        List<MovieDTO> result = movieService.searchMovies(query);
        log.info("GET /api/movies/search - по запросу '{}' найдено {} фильмов", query, result.size());
        
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<MovieDTO> createMovie(@RequestBody MovieDTO movieDTO) {
        log.info("POST /api/movies - запрос на создание фильма: '{}' (жанр: {}, длительность: {} мин)",
                movieDTO.getTitle(), movieDTO.getGenre(), movieDTO.getDurationMinutes());
        MovieDTO created = movieService.createMovie(movieDTO);
        log.info("POST /api/movies - фильм успешно создан с id={}", created.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieDTO> updateMovie(@PathVariable Long id, @RequestBody MovieDTO movieDTO) {
        log.info("PUT /api/movies/{} - запрос на обновление фильма: '{}'", id, movieDTO.getTitle());
        MovieDTO updated = movieService.updateMovie(id, movieDTO);
        log.info("PUT /api/movies/{} - фильм успешно обновлён", id);
        
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        log.info("DELETE /api/movies/{} - запрос на удаление фильма", id);
        movieService.deleteMovie(id);
        log.info("DELETE /api/movies/{} - фильм успешно удалён", id);
        
        return ResponseEntity.noContent().build();
    }
}
