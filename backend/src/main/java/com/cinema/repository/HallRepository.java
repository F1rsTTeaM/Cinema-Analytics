package com.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cinema.model.Hall;

@Repository
public interface HallRepository extends JpaRepository<Hall, Long> {
    boolean existsByName(String name);
}
