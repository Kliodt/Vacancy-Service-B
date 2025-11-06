package com.vacancy.controllers;

import com.vacancy.model.dto.in.UserDtoIn;
import com.vacancy.model.dto.out.UserDtoOut;
import com.vacancy.model.dto.out.UserVacancyResponseDtoOut;
import com.vacancy.model.entities.User;
import com.vacancy.model.entities.Vacancy;
import com.vacancy.service.UserService;
import com.vacancy.service.VacancyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final VacancyService vacancyService;
    private final ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<List<UserDtoOut>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<User> userPage = userService.getAllUsers(page, size);
        List<UserDtoOut> dtos = userPage.getContent().stream()
                .map(user -> modelMapper.map(user, UserDtoOut.class)).toList();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(userPage.getTotalElements()));
        return ResponseEntity.ok().headers(headers).body(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDtoOut> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(modelMapper.map(user, UserDtoOut.class));
    }

    @PostMapping
    public ResponseEntity<UserDtoOut> createUser(@Valid @RequestBody UserDtoIn userDtoIn) {
        User user = modelMapper.map(userDtoIn, User.class);
        User savedUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelMapper.map(savedUser, UserDtoOut.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDtoOut> updateUser(@PathVariable Long id, @Valid @RequestBody UserDtoIn userDtoIn) {
        User user = modelMapper.map(userDtoIn, User.class);
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(modelMapper.map(updatedUser, UserDtoOut.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/favorites")
    public ResponseEntity<List<Vacancy>> getUserFavorites(@PathVariable Long id) {
        List<Vacancy> favorites = userService.getUserFavorites(id);
        return ResponseEntity.ok(favorites);
    }

    @PutMapping("/{userId}/favorite/{vacancyId}")
    public ResponseEntity<Void> addToFavorites(@PathVariable Long vacancyId, @PathVariable Long userId) {
        vacancyService.addToFavorites(vacancyId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/favorite/{vacancyId}")
    public ResponseEntity<Void> removeFromFavorites(@PathVariable Long vacancyId, @PathVariable Long userId) {
        vacancyService.removeFromFavorites(vacancyId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/responses")
    public ResponseEntity<List<UserVacancyResponseDtoOut>> getUserResponses(@PathVariable Long userId) {
        List<UserVacancyResponseDtoOut> responses = userService.getUserResponses(userId).stream()
                .map(response -> modelMapper.map(response, UserVacancyResponseDtoOut.class)).toList();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{userId}/respond/{vacancyId}")
    public ResponseEntity<Void> respondToVacancy(@PathVariable Long vacancyId, @PathVariable Long userId) {
        vacancyService.respondToVacancy(vacancyId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/respond/{vacancyId}")
    public ResponseEntity<Void> removeResponseFromVacancy(@PathVariable Long vacancyId, @PathVariable Long userId) {
        vacancyService.removeResponseFromVacancy(vacancyId, userId);
        return ResponseEntity.ok().build();
    }
}
