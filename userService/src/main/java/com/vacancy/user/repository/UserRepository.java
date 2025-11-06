package com.vacancy.user.repository;

import com.vacancy.user.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long>, PagingAndSortingRepository<User, Long> {
    User findUserByEmail(String email);
}
