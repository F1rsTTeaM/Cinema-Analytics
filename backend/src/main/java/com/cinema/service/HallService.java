package com.cinema.service;

import com.cinema.dto.HallCreateRequest;
import com.cinema.dto.HallDTO;
import com.cinema.exception.HallExceptions;
import com.cinema.model.Hall;
import com.cinema.repository.HallRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j 
public class HallService {

    @Autowired
    private HallRepository hallRepository;

    public List<HallDTO> getAllHalls() {
        log.debug("Получение списка всех залов из БД");
        List<Hall> halls = hallRepository.findAll();
        log.debug("Из БД получено {} залов", halls.size());
        
        return halls.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public HallDTO getHallById(Long id) {
        log.debug("Поиск зала по id={}", id);
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Зал с id={} не найден", id);
                    return new HallExceptions.NotFound(id);
                });
        log.debug("Зал с id={} найден: '{}'", id, hall.getName());
        
        return convertToDTO(hall);
    }

    public HallDTO createHall(HallCreateRequest request) {
        log.debug("Создание зала с названием '{}'", request.getName());

        validateHall(request);

        if (hallRepository.existsByName(request.getName())) {
            log.warn("Попытка создать зал с уже существующим названием: '{}'", request.getName());
            throw new HallExceptions.DuplicateName(request.getName());
        }

        Hall hall = new Hall(
                request.getName(),
                request.getRowsCount(),
                request.getSeatsPerRow()
        );

        Hall saved = hallRepository.save(hall);
        log.info("Зал '{}' успешно создан с id={} (вместимость: {} мест)",
                saved.getName(), saved.getId(), saved.getCapacity());
        
        return convertToDTO(saved);
    }

    public void deleteHall(Long id) {
        log.debug("Удаление зала с id={}", id);

        if (!hallRepository.existsById(id)) {
            log.warn("Невозможно удалить: зал с id={} не найден", id);
            throw new HallExceptions.NotFound(id);
        }

        hallRepository.deleteById(id);
        log.info("Зал с id={} успешно удалён", id);
    }

    public List<HallDTO> searchHalls(String query) {
        log.debug("Поиск залов по подстроке: '{}'", query);
        List<Hall> found = hallRepository.findByNameContainingIgnoreCase(query);
        log.debug("По запросу '{}' найдено {} залов", query, found.size());
        
        return found.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private void validateHall(HallCreateRequest request) {
        if (request.getRowsCount() == null || request.getRowsCount() <= 0) {
            log.warn("Некорректное количество рядов: {}", request.getRowsCount());
            throw new HallExceptions.InvalidRowsCount(request.getRowsCount());
        }
        if (request.getSeatsPerRow() == null || request.getSeatsPerRow() <= 0) {
            log.warn("Некорректное количество мест в ряду: {}", request.getSeatsPerRow());
            throw new HallExceptions.InvalidSeatsPerRow(request.getSeatsPerRow());
        }
    }

    private HallDTO convertToDTO(Hall hall) {
        return new HallDTO(
            hall.getId(),
            hall.getName(),
            hall.getCapacity(),
            hall.getRowsCount(),
            hall.getSeatsPerRow()
        );
    }
}