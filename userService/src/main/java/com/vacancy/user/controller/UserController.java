package com.vacancy.user.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vacancy.user.model.User;
import com.vacancy.user.model.dto.UserRequestCreateDto;
import com.vacancy.user.model.dto.UserRequestUpdateDto;
import com.vacancy.user.model.dto.UserResponseDto;
import com.vacancy.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper = new ModelMapper();

    @Operation(summary = "Получить список всех пользователей")
    @GetMapping
    public Flux<UserResponseDto> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return userService.getAllUsers(page, size)
                .map(user -> modelMapper.map(user, UserResponseDto.class));
    }

    @Operation(summary = "Получить пользователя по id")
    @GetMapping("/{userId}")
    public Mono<ResponseEntity<UserResponseDto>> getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId)
                .map(usr -> modelMapper.map(usr, UserResponseDto.class))
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Создать пользователя")
    @PostMapping
    @PreAuthorize("hasRole(ROLE_SUPERVISOR)")
    public Mono<ResponseEntity<UserResponseDto>> createUser(@Valid @RequestBody UserRequestCreateDto user) {
        return userService.createUser(modelMapper.map(user, User.class))
                .map(usr -> modelMapper.map(usr, UserResponseDto.class))
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @Operation(summary = "Обновить пользователя по id")
    @PutMapping("/{userId}")
    public Mono<ResponseEntity<UserResponseDto>> updateUser(@PathVariable Long userId, @Valid @RequestBody UserRequestUpdateDto user) {
        return userService.updateUser(userId, modelMapper.map(user, User.class))
                .map(usr -> modelMapper.map(usr, UserResponseDto.class))
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Удалить пользователя по id")
    @DeleteMapping("/{userId}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable Long userId) {
        return userService.deleteUser(userId)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Получить избранные вакансии пользователя")
    @GetMapping("/{userId}/favorite")
    public Mono<ResponseEntity<List<Long>>> getUserFavorites(@PathVariable Long userId) {
        return userService.getUserFavoriteVacancyIds(userId)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Добавить вакансию в избранное")
    @PutMapping("/{userId}/favorite/{vacancyId}")
    public Mono<ResponseEntity<Void>> addToFavorites(
            @PathVariable Long userId,
            @PathVariable Long vacancyId) {
        return userService.addToFavorites(userId, vacancyId)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Убрать вакансию из избранного")
    @DeleteMapping("/{userId}/favorite/{vacancyId}")
    public Mono<ResponseEntity<Void>> removeFromFavorites(@PathVariable Long userId, @PathVariable Long vacancyId) {
        return userService.removeFromFavorites(userId, vacancyId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
