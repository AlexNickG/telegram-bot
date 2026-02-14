package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.entity.User;
import com.skillbox.cryptobot.repository.UserRepository;
import com.skillbox.cryptobot.service.CryptoCurrencyService;
import com.skillbox.cryptobot.utils.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Setter
@Slf4j
public class NotifyUserService {

    private AbsSender absSender;
    private Double bitcoinPrice;
    private final CryptoCurrencyService service;
    private final UserRepository repository;
    private final Map<Long, Instant> delayForUsers = new HashMap<>();
    @Value("${telegram.bot.notify.delay.value}")
    private Integer updateDelay;
    @Value("${telegram.bot.notify.delay.unit}")
    private ChronoUnit updateUnit;

    public void setBotSender(AbsSender sender) {
        this.absSender = sender;
    }

    @Scheduled(fixedRateString = "${binance.api.updatePriceDelay}")
    public void costTrack() {
        if (absSender == null) {
            log.warn("Abs sender не инициализирован");
            return;
        }
        bitcoinPrice = service.getBitcoinPrice();
        List<User> userList = repository.findBySubscriptionPriceGreaterThan(bitcoinPrice);
        userList.forEach(this::processUserNotification);
    }

    public void processUserNotification(User user) {
        Long userId = user.getTelegramId();
        Instant now = Instant.now();

        if (shouldNotifyUser(userId, now)) {
            delayForUsers.put(userId, now);
            CompletableFuture.runAsync(() -> sendMessage(user));
        }
    }

    public boolean shouldNotifyUser(Long userId, Instant now) {
        return Optional.ofNullable(delayForUsers.get(userId))
                .map(lastNotification -> now.isAfter(lastNotification.plus(updateDelay, updateUnit)))
                .orElse(true);
    }

    public void sendMessage(User user) {
        if (absSender == null) {
            log.error("Abs sender не инициализирован");
            return;
        }
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(user.getTelegramId());
            sendMessage.setText("Пора покупать, стоимость биткоина " + TextUtil.toString(bitcoinPrice));
            absSender.executeAsync(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения пользователю {}: ", user.getTelegramId(), e);
        }
    }

    public void userNotExistMessage(Long id) {
        SendMessage answer = new SendMessage();
        answer.setChatId(id);
        answer.setText("Для начала работы с ботом введите /start");
        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения пользователю. ", e);
        }
    }
}
