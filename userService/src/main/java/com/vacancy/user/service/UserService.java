package com.vacancy.user.service;

import com.vacancy.user.model.User;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

public interface UserService {
    Page<User> getAllUsers(int page, int size);
    User getUserById(Long id);
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    Set<Long> getUserFavoriteVacancyIds(Long id);
    List<Object> getUserFavorites(Long id);
    Set<Long> getUserResponseVacancyIds(Long id);
    List<Object> getUserResponses(Long id);
    void addToFavorites(Long userId, Long vacancyId);
    void removeFromFavorites(Long userId, Long vacancyId);
    void respondToVacancy(Long userId, Long vacancyId);
    void removeResponseFromVacancy(Long userId, Long vacancyId);
}
