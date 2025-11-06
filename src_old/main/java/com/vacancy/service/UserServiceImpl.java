package com.vacancy.service;

import com.vacancy.exceptions.RequestException;
import com.vacancy.model.entities.User;
import com.vacancy.model.entities.UserVacancyResponse;
import com.vacancy.model.entities.Vacancy;
import com.vacancy.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String USER_NOT_FOUND = "Пользователь не найден";

    private final UserRepository userRepository;
    private final UserVacancyResponseService responseService;

    public Page<User> getAllUsers(int page, int size) {
        if (size > 50) {
            size = 50;
        }
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RequestException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));
    }

    public User createUser(User user) {
        if (userRepository.findUserByEmail(user.getEmail()) != null) {
            throw new RequestException(HttpStatus.CONFLICT, "Пользователь с таким email уже зарегистрирован");
        }
        user.setId(0L);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RequestException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));

        if (userRepository.findUserByEmail(user.getEmail()) != null && !existingUser.getEmail().equals(user.getEmail())) {
            throw new RequestException(HttpStatus.CONFLICT, "С таким email уже зарегистрирован другой пользователь");
        }

        existingUser.setNickname(user.getNickname());
        existingUser.setEmail(user.getEmail());
        existingUser.setCvLink(user.getCvLink());
        
        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public List<Vacancy> getUserFavorites(Long id) {
        User user = getUserById(id);
        List<Vacancy> favorites = user.getFavoriteList();
        Hibernate.initialize(favorites);
        return favorites;
    }

    public List<UserVacancyResponse> getUserResponses(Long id) {
        return responseService.getUserResponses(id);
    }
}