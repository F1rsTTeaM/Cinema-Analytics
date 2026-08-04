package com.cinema.repository;

import com.cinema.model.Session;
import com.cinema.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByStatus(SessionStatus status);

    List<Session> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Session> findByMovieId(Long movieId);

    List<Session> findByHallId(Long hallId);

    @Query("SELECT s FROM Session s WHERE s.startTime >= :now AND s.status = 'SCHEDULED' ORDER BY s.startTime ASC")
    List<Session> findUpcomingSessions(@Param("now") LocalDateTime now);

    @Query("SELECT s FROM Session s WHERE s.startTime BETWEEN :start AND :end AND s.status = 'COMPLETED'")
    List<Session> findCompletedSessionsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT s FROM Session s WHERE s.hall.id = :hallId AND " +
           "((s.startTime < :endTime AND s.endTime > :startTime))")
    List<Session> findOverlappingSessions(
            @Param("hallId") Long hallId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT SUM(s.totalAmount) FROM Session s WHERE s.status = 'COMPLETED' AND s.startTime BETWEEN :start AND :end")
    BigDecimal getTotalRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT s.movie.id, s.movie.title, COUNT(s), SUM(s.totalAmount), SUM(SIZE(s.soldSeats)) " +
       "FROM Session s " +
       "WHERE s.status = 'COMPLETED' AND s.startTime BETWEEN :start AND :end " +
       "GROUP BY s.movie.id, s.movie.title")
List<Object[]> getMovieStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT s.hall.id, s.hall.name, COUNT(s), SUM(s.totalAmount), SUM(SIZE(s.soldSeats)) " +
           "FROM Session s " +
           "WHERE s.status = 'COMPLETED' AND s.startTime BETWEEN :start AND :end " +
           "GROUP BY s.hall.id, s.hall.name")
    List<Object[]> getHallStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT CAST(s.start_time AS DATE), COUNT(s.id), SUM(s.total_amount) " +
                   "FROM sessions s " +
                   "WHERE s.status = 'COMPLETED' AND s.start_time BETWEEN :start AND :end " +
                   "GROUP BY CAST(s.start_time AS DATE) " +
                   "ORDER BY CAST(s.start_time AS DATE)",
           nativeQuery = true)
    List<Object[]> getDailyTrends(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}