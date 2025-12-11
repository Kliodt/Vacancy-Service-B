package com.vacancy.vacancy.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public Object getUserById(Long id) {
        Map<String, Object> fallbackUser = new HashMap<>();
        fallbackUser.put("id", id);
        fallbackUser.put("error", "Пользователь временно недоступен");
        return fallbackUser;   
    }

}
