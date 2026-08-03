package com.cinema.repository;

import com.cinema.model.Hall;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HallRepository extends JpaRepository<Hall, Long> {
    boolean existsByName(String name);
    List<Hall> findByNameContainingIgnoreCase(String name);
}