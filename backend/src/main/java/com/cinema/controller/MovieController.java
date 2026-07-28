package com.cinema.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cinema.dto.MovieDTO;
import com.cinema.service.MovieService;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    @Autowired
    private MovieService movieService;

    @GetMapping
    public ResponseEntity<List<MovieDTO>> getAllMovies() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieDTO> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieDTO>> searchMovies(@RequestParam String query) {
        return ResponseEntity.ok(movieService.searchMovies(query));
    }

    @PostMapping
    public ResponseEntity<MovieDTO> createMovie(@RequestBody MovieDTO movieDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movieService.createMovie(movieDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieDTO> updateMovie(@PathVariable Long id, @RequestBody MovieDTO movieDTO) {
        return ResponseEntity.ok(movieService.updateMovie(id, movieDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}
