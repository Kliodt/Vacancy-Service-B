package com.vacancy.user.presentation.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;

import com.vacancy.user.presentation.dto.UserResponseDto;
import com.vacancy.user.presentation.dto.UserRequestCreateDto;
import com.vacancy.user.presentation.dto.UserRequestUpdateDto;
import com.vacancy.user.application.usecase.GetAllUsersUseCase;
import com.vacancy.user.application.usecase.GetUserByIdUseCase;
import com.vacancy.user.application.usecase.CreateUserUseCase;
import com.vacancy.user.application.usecase.UpdateUserUseCase;
import com.vacancy.user.domain.model.User;
import com.vacancy.user.application.usecase.DeleteUserUseCase;
import com.vacancy.user.application.usecase.GetUserFavoritesUseCase;
import com.vacancy.user.application.usecase.AddToFavoritesUseCase;
import com.vacancy.user.application.usecase.RemoveFromFavoritesUseCase;
import com.vacancy.user.infrastructure.security.SecurityConverter;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final GetAllUsersUseCase getAllUsersUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final GetUserFavoritesUseCase getUserFavoritesUseCase;
    private final AddToFavoritesUseCase addToFavoritesUseCase;
    private final RemoveFromFavoritesUseCase removeFromFavoritesUseCase;
    private final SecurityConverter securityConverter;
    private final ModelMapper modelMapper;

    @Operation(summary = "Получить список всех пользователей")
    @GetMapping
    public Flux<UserResponseDto> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return getAllUsersUseCase.execute(page, size)
            .map(usr -> modelMapper.map(usr, UserResponseDto.class));
    }

    @Operation(summary = "Получить пользователя по id")
    @GetMapping("/{userId}")
    public Mono<ResponseEntity<UserResponseDto>> getUserById(@PathVariable Long userId) {
        return getUserByIdUseCase.execute(userId)
            .map(usr -> modelMapper.map(usr, UserResponseDto.class))
            .map(ResponseEntity::ok)
            .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @Operation(summary = "Создать пользователя (только supervisor)")
    @PostMapping
    public Mono<ResponseEntity<UserResponseDto>> createUser(
            @Valid @RequestBody UserRequestCreateDto request,
            Authentication authentication) {
        var currentUser = securityConverter.extractCurrentUser(authentication);
        var domainUser = modelMapper.map(request, User.class);
        return createUserUseCase.execute(domainUser, currentUser)
            .map(usr -> modelMapper.map(usr, UserResponseDto.class))
            .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @Operation(summary = "Обновить пользователя по id")
    @PutMapping("/{userId}")
    public Mono<ResponseEntity<UserResponseDto>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserRequestUpdateDto request,
            Authentication authentication) {
        var currentUser = securityConverter.extractCurrentUser(authentication);
        var domainUser = modelMapper.map(request, User.class);
        return updateUserUseCase.execute(userId, domainUser, currentUser)
            .map(usr -> modelMapper.map(usr, UserResponseDto.class))
            .map(ResponseEntity::ok);
    }

    @Operation(summary = "Удалить пользователя по id")
    @DeleteMapping("/{userId}")
    public Mono<ResponseEntity<Void>> deleteUser(
            @PathVariable Long userId,
            Authentication authentication) {
        var currentUser = securityConverter.extractCurrentUser(authentication);
        return deleteUserUseCase.execute(userId, currentUser)
            .thenReturn(ResponseEntity.noContent().<Void>build());
    }

    @Operation(summary = "Получить избранные вакансии пользователя")
    @GetMapping("/{userId}/favorites")
    public Mono<ResponseEntity<List<Long>>> getUserFavorites(
            @PathVariable Long userId,
            Authentication authentication) {
        var currentUser = securityConverter.extractCurrentUser(authentication);
        return getUserFavoritesUseCase.execute(userId, currentUser)
            .map(ResponseEntity::ok);
    }

    @Operation(summary = "Добавить вакансию в избранное")
    @PutMapping("/{userId}/favorites")
    public Mono<ResponseEntity<Void>> addToFavorites(
            @PathVariable Long userId,
            @RequestParam Long vacancyId,
            Authentication authentication) {
        var currentUser = securityConverter.extractCurrentUser(authentication);
        return addToFavoritesUseCase.execute(userId, vacancyId, currentUser)
            .thenReturn(ResponseEntity.noContent().<Void>build());
    }

    @Operation(summary = "Убрать вакансию из избранного")
    @DeleteMapping("/{userId}/favorites")
    public Mono<ResponseEntity<Void>> removeFromFavorites(
            @PathVariable Long userId,
            @RequestParam Long vacancyId,
            Authentication authentication) {
        var currentUser = securityConverter.extractCurrentUser(authentication);
        return removeFromFavoritesUseCase.execute(userId, vacancyId, currentUser)
            .thenReturn(ResponseEntity.noContent().<Void>build());
    }
}
