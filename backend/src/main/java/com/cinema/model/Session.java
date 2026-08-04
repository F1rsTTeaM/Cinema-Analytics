package com.cinema.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "ticket_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal ticketPrice;

    @Enumerated(EnumType.STRING)
    private SessionStatus status = SessionStatus.SCHEDULED;

    @ElementCollection
    @CollectionTable(name = "session_sold_seats", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "seat")
    private List<String> soldSeats = new ArrayList<>();

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Session(Movie movie, Hall hall, LocalDateTime startTime, LocalDateTime endTime, BigDecimal ticketPrice) {
        this.movie = movie;
        this.hall = hall;
        this.startTime = startTime;
        this.endTime = endTime;
        this.ticketPrice = ticketPrice;
        this.status = SessionStatus.SCHEDULED;
        this.soldSeats = new ArrayList<>();
        this.totalAmount = BigDecimal.ZERO;
    }

    public void addSoldSeat(String seat) {
        if (this.soldSeats == null) {
            this.soldSeats = new ArrayList<>();
        }
        this.soldSeats.add(seat);
        this.totalAmount = this.totalAmount.add(this.ticketPrice);
    }

    public int getSoldCount() {
        return this.soldSeats != null ? this.soldSeats.size() : 0;
    }

    public boolean isSeatAvailable(String seat) {
        return this.soldSeats == null || !this.soldSeats.contains(seat);
    }

    public List<String> getAvailableSeats() {
        List<String> allSeats = new ArrayList<>();
        for (int row = 1; row <= hall.getRowsCount(); row++) {
            for (int seat = 1; seat <= hall.getSeatsPerRow(); seat++) {
                String seatKey = row + "-" + seat;
                if (isSeatAvailable(seatKey)) {
                    allSeats.add(seatKey);
                }
            }
        }
        return allSeats;
    }

    public int getAvailableCount() {
        return getAvailableSeats().size();
    }
}