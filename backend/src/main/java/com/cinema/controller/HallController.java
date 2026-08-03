package com.cinema.controller;

import com.cinema.dto.HallCreateRequest;
import com.cinema.dto.HallDTO;
import com.cinema.service.HallService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/halls")
public class HallController {

    @Autowired
    private HallService hallService;

    @GetMapping
    public ResponseEntity<List<HallDTO>> getAllHalls() {
        return ResponseEntity.ok(hallService.getAllHalls());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HallDTO> getHallById(@PathVariable Long id) {
        return ResponseEntity.ok(hallService.getHallById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<HallDTO>> searchHalls(@RequestParam String query) {
        return ResponseEntity.ok(hallService.searchHalls(query));
    }

    @PostMapping
    public ResponseEntity<HallDTO> createHall(@Valid @RequestBody HallCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hallService.createHall(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHall(@PathVariable Long id) {
        hallService.deleteHall(id);
        return ResponseEntity.noContent().build();
    }
}