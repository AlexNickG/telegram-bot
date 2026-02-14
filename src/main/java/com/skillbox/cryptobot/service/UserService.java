package com.skillbox.cryptobot.service;

import com.skillbox.cryptobot.repository.UserRepository;
import com.skillbox.cryptobot.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public User getUserByTelegramId(Long id) {
        Optional<User> existedUser = repository.getUserByTelegramId(id);
        return existedUser.orElse(null);
    }

    public void saveUser(User user) {
        repository.save(user);
    }
}
