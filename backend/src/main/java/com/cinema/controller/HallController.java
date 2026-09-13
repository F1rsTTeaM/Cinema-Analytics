package com.cinema.controller;

import com.cinema.dto.HallCreateRequest;
import com.cinema.dto.HallDTO;
import com.cinema.service.HallService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/halls")
@Slf4j 
public class HallController {

    @Autowired
    private HallService hallService;

    @GetMapping
    public ResponseEntity<List<HallDTO>> getAllHalls() {
        log.info("GET /api/halls - запрос на получение списка всех залов");
        List<HallDTO> halls = hallService.getAllHalls();
        log.info("GET /api/halls - возвращено {} залов", halls.size());
        
        return ResponseEntity.ok(halls);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HallDTO> getHallById(@PathVariable Long id) {
        log.info("GET /api/halls/{} - запрос на получение зала по id", id);
        HallDTO hall = hallService.getHallById(id);
        log.info("GET /api/halls/{} - зал найден: '{}'", id, hall.getName());
        
        return ResponseEntity.ok(hall);
    }

    @GetMapping("/search")
    public ResponseEntity<List<HallDTO>> searchHalls(@RequestParam String query) {
        log.info("GET /api/halls/search - поиск залов по запросу: '{}'", query);
        List<HallDTO> result = hallService.searchHalls(query);
        log.info("GET /api/halls/search - по запросу '{}' найдено {} залов", query, result.size());
        
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<HallDTO> createHall(@Valid @RequestBody HallCreateRequest request) {
        log.info("POST /api/halls - запрос на создание зала: '{}' (рядов: {}, мест в ряду: {})",
                request.getName(), request.getRowsCount(), request.getSeatsPerRow());
        HallDTO created = hallService.createHall(request);
        log.info("POST /api/halls - зал успешно создан с id={}", created.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHall(@PathVariable Long id) {
        log.info("DELETE /api/halls/{} - запрос на удаление зала", id);
        hallService.deleteHall(id);
        log.info("DELETE /api/halls/{} - зал успешно удалён", id);
        
        return ResponseEntity.noContent().build();
    }
}