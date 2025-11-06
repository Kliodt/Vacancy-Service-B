package com.vacancy.user.controller;

import com.vacancy.user.model.User;
import com.vacancy.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<User> userPage = userService.getAllUsers(page, size);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(userPage.getTotalElements()));
        return ResponseEntity.ok().headers(headers).body(userPage.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        User savedUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/favorites")
    public ResponseEntity<List<Object>> getUserFavorites(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserFavorites(id));
    }

    @GetMapping("/{id}/favorites/ids")
    public ResponseEntity<Set<Long>> getUserFavoriteIds(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserFavoriteVacancyIds(id));
    }

    @PutMapping("/{userId}/favorite/{vacancyId}")
    public ResponseEntity<Void> addToFavorites(@PathVariable Long userId, @PathVariable Long vacancyId) {
        userService.addToFavorites(userId, vacancyId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/favorite/{vacancyId}")
    public ResponseEntity<Void> removeFromFavorites(@PathVariable Long userId, @PathVariable Long vacancyId) {
        userService.removeFromFavorites(userId, vacancyId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/responses")
    public ResponseEntity<List<Object>> getUserResponses(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserResponses(userId));
    }

    @GetMapping("/{userId}/responses/ids")
    public ResponseEntity<Set<Long>> getUserResponseIds(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserResponseVacancyIds(userId));
    }

    @PutMapping("/{userId}/respond/{vacancyId}")
    public ResponseEntity<Void> respondToVacancy(@PathVariable Long userId, @PathVariable Long vacancyId) {
        userService.respondToVacancy(userId, vacancyId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/respond/{vacancyId}")
    public ResponseEntity<Void> removeResponseFromVacancy(@PathVariable Long userId, @PathVariable Long vacancyId) {
        userService.removeResponseFromVacancy(userId, vacancyId);
        return ResponseEntity.ok().build();
    }
}
