package com.vacancy.user.service;

import com.vacancy.user.model.User;
import com.vacancy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String USER_NOT_FOUND = "Пользователь не найден";
    private static final String VACANCY_SERVICE_URL = "http://vacancy-service/api/vacancies";

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    public Page<User> getAllUsers(int page, int size) {
        if (size > 50) {
            size = 50;
        }
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
    }

    public User createUser(User user) {
        if (userRepository.findUserByEmail(user.getEmail()) != null) {
            throw new RuntimeException("Пользователь с таким email уже зарегистрирован");
        }
        user.setId(0L);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        if (userRepository.findUserByEmail(user.getEmail()) != null && !existingUser.getEmail().equals(user.getEmail())) {
            throw new RuntimeException("С таким email уже зарегистрирован другой пользователь");
        }

        existingUser.setNickname(user.getNickname());
        existingUser.setEmail(user.getEmail());
        existingUser.setCvLink(user.getCvLink());
        
        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Set<Long> getUserFavoriteVacancyIds(Long id) {
        return getUserById(id).getFavoriteVacancyIds();
    }

    public List<Object> getUserFavorites(Long id) {
        Set<Long> favoriteIds = getUserFavoriteVacancyIds(id);
        return favoriteIds.stream()
                .map(vacancyId -> restTemplate.getForObject(VACANCY_SERVICE_URL + "/" + vacancyId, Object.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Set<Long> getUserResponseVacancyIds(Long id) {
        return getUserById(id).getResponseVacancyIds();
    }

    public List<Object> getUserResponses(Long id) {
        Set<Long> responseIds = getUserResponseVacancyIds(id);
        return responseIds.stream()
                .map(vacancyId -> restTemplate.getForObject(VACANCY_SERVICE_URL + "/" + vacancyId, Object.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void addToFavorites(Long userId, Long vacancyId) {
        // Verify vacancy exists
        restTemplate.getForObject(VACANCY_SERVICE_URL + "/" + vacancyId, Object.class);
        
        User user = getUserById(userId);
        user.getFavoriteVacancyIds().add(vacancyId);
        userRepository.save(user);
    }

    @Transactional
    public void removeFromFavorites(Long userId, Long vacancyId) {
        User user = getUserById(userId);
        user.getFavoriteVacancyIds().remove(vacancyId);
        userRepository.save(user);
    }

    @Transactional
    public void respondToVacancy(Long userId, Long vacancyId) {
        // Verify vacancy exists
        restTemplate.getForObject(VACANCY_SERVICE_URL + "/" + vacancyId, Object.class);
        
        User user = getUserById(userId);
        user.getResponseVacancyIds().add(vacancyId);
        userRepository.save(user);
    }

    @Transactional
    public void removeResponseFromVacancy(Long userId, Long vacancyId) {
        User user = getUserById(userId);
        user.getResponseVacancyIds().remove(vacancyId);
        userRepository.save(user);
    }
}
