package com.cinema.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cinema.model.Session;
import com.cinema.model.SessionStatus;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long>{
    List<Session> findByStatus(SessionStatus status);
    List<Session> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Session> findByMovieId(Long movieId);
    List<Session> findByHallId(Long hallId);
    
    @Query("SELECT s FROM Session s WHERE s.startTime >= :now AND s.status = 'SCHEDULED' ORDER BY s.startTime ASC")
    List<Session> findUpcomingSessions(@Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM Session s WHERE s.startTime BETWEEN :start AND :end AND s.status = 'COMPLETED'")
    List<Session> findCompletedSessionsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
