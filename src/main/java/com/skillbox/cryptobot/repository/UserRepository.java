package com.skillbox.cryptobot.repository;

import com.skillbox.cryptobot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> getUserByTelegramId(Long id);

    List<User> findBySubscriptionPriceGreaterThan(Double price);
}
