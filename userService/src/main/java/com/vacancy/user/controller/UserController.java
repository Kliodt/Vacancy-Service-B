package com.vacancy.user.controller;

import com.vacancy.user.model.User;
import com.vacancy.user.model.dto.UserDtoIn;
import com.vacancy.user.model.dto.UserDtoOut;
import com.vacancy.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper = new ModelMapper();

    @GetMapping
    public Flux<UserDtoOut> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return userService.getAllUsers(page, size)
                .map(user -> modelMapper.map(user, UserDtoOut.class));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserDtoOut>> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(usr -> modelMapper.map(usr, UserDtoOut.class))
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<UserDtoOut>> createUser(@Valid @RequestBody UserDtoIn user) {
        return userService.createUser(modelMapper.map(user, User.class))
                .map(usr -> modelMapper.map(usr, UserDtoOut.class))
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<UserDtoOut>> updateUser(@PathVariable Long id, @Valid @RequestBody UserDtoIn user) {
        return userService.updateUser(id, modelMapper.map(user, User.class))
                .map(usr -> modelMapper.map(usr, UserDtoOut.class))
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}/favorites")
    public Mono<ResponseEntity<List<Object>>> getUserFavorites(@PathVariable Long id) {
        return userService.getUserFavorites(id)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}/favorites/ids")
    public Mono<ResponseEntity<Set<Long>>> getUserFavoriteIds(@PathVariable Long id) {
        return userService.getUserFavoriteVacancyIds(id)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{userId}/favorite/{vacancyId}")
    public Mono<ResponseEntity<Void>> addToFavorites(@PathVariable Long userId, @PathVariable Long vacancyId) {
        return userService.addToFavorites(userId, vacancyId)
                .thenReturn(ResponseEntity.ok().build());
    }

    @DeleteMapping("/{userId}/favorite/{vacancyId}")
    public Mono<ResponseEntity<Void>> removeFromFavorites(@PathVariable Long userId, @PathVariable Long vacancyId) {
        return userService.removeFromFavorites(userId, vacancyId)
                .thenReturn(ResponseEntity.ok().build());
    }

    @GetMapping("/{userId}/responses")
    public Mono<ResponseEntity<List<Object>>> getUserResponses(@PathVariable Long userId) {
        return userService.getUserResponses(userId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{userId}/responses/ids")
    public Mono<ResponseEntity<Set<Long>>> getUserResponseIds(@PathVariable Long userId) {
        return userService.getUserResponseVacancyIds(userId)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{userId}/respond/{vacancyId}")
    public Mono<ResponseEntity<Void>> respondToVacancy(@PathVariable Long userId, @PathVariable Long vacancyId) {
        return userService.respondToVacancy(userId, vacancyId)
                .thenReturn(ResponseEntity.ok().build());
    }

    @DeleteMapping("/{userId}/respond/{vacancyId}")
    public Mono<ResponseEntity<Void>> removeResponseFromVacancy(@PathVariable Long userId, @PathVariable Long vacancyId) {
        return userService.removeResponseFromVacancy(userId, vacancyId)
                .thenReturn(ResponseEntity.ok().build());
    }
}
