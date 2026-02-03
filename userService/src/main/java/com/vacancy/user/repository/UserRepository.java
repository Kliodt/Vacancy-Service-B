package com.vacancy.user.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.vacancy.user.model.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long>, PagingAndSortingRepository<User, Long> {
    Optional<User> findUserByEmail(String email);
}
