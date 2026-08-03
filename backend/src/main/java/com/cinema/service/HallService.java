package com.cinema.service;

import com.cinema.dto.HallCreateRequest;
import com.cinema.dto.HallDTO;
import com.cinema.model.Hall;
import com.cinema.repository.HallRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class HallService {

    @Autowired
    private HallRepository hallRepository;

    public List<HallDTO> getAllHalls() {
        return hallRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public HallDTO getHallById(Long id) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Зал не найден"));
        return convertToDTO(hall);
    }

    public HallDTO createHall(HallCreateRequest request) {
        if (hallRepository.existsByName(request.getName())) {
            throw new RuntimeException("Зал с таким названием уже существует");
        }

        Hall hall = new Hall(
            request.getName(),
            request.getRowsCount(),
            request.getSeatsPerRow()
        );

        Hall saved = hallRepository.save(hall);
        return convertToDTO(saved);
    }

    public void deleteHall(Long id) {
        if (!hallRepository.existsById(id)) {
            throw new RuntimeException("Зал не найден");
        }
        hallRepository.deleteById(id);
    }

    public List<HallDTO> searchHalls(String query) {
        return hallRepository.findByNameContainingIgnoreCase(query).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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