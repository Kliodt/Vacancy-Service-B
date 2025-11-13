package com.vacancy.vacancy.client;

import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public Object getUserById(Long id) {
        throw new RuntimeException("Сервис пользователей недоступен");
    }

}
