package com.vacancy.service;

import com.vacancy.model.entities.User;
import com.vacancy.model.entities.UserVacancyResponse;
import com.vacancy.model.entities.Vacancy;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {
    Page<User> getAllUsers(int page, int size);
    User getUserById(Long id);
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    List<Vacancy> getUserFavorites(Long id);
    List<UserVacancyResponse> getUserResponses(Long id);
}
