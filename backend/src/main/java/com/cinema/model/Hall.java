package com.cinema.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "halls")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "rows_count", nullable = false)
    private Integer rowsCount;

    @Column(name = "seats_per_row", nullable = false)
    private Integer seatsPerRow;

    @OneToMany(mappedBy = "hall", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Session> sessions = new ArrayList<>();

    public Hall(String name, Integer rowsCount, Integer seatsPerRow) {
        this.name = name;
        this.rowsCount = rowsCount;
        this.seatsPerRow = seatsPerRow;
        this.capacity = rowsCount * seatsPerRow;
    }
}