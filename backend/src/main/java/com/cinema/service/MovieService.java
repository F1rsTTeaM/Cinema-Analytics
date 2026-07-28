package com.cinema.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cinema.repository.MovieRepository;

import com.cinema.dto.MovieDTO;
import com.cinema.model.Movie;

@Service
@Transactional
public class MovieService {
    @Autowired
    private MovieRepository movieRepository;

    public List<MovieDTO> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public MovieDTO getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Фильм не найден"));
        return convertToDTO(movie);
    }

    public MovieDTO createMovie(MovieDTO movieDTO) {
        if (movieRepository.existsByTitle(movieDTO.getTitle())) {
            throw new RuntimeException("Фильм с таким названием уже существует");
        }

        Movie movie = new Movie(
            movieDTO.getTitle(),
            movieDTO.getGenre(),
            movieDTO.getDurationMinutes(),
            movieDTO.getReleaseDate()
        );

        Movie saved = movieRepository.save(movie);
        return convertToDTO(saved);
    }

    public MovieDTO updateMovie(Long id, MovieDTO movieDTO) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Фильм не найден"));

        movie.setTitle(movieDTO.getTitle());
        movie.setGenre(movieDTO.getGenre());
        movie.setDurationMinutes(movieDTO.getDurationMinutes());
        movie.setReleaseDate(movieDTO.getReleaseDate());

        return convertToDTO(movieRepository.save(movie));
    }

    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new RuntimeException("Фильм не найден");
        }
        movieRepository.deleteById(id);
    }

    public List<MovieDTO> searchMovies(String query) {
        return movieRepository.findByTitleContainingIgnoreCase(query).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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
