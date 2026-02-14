package com.skillbox.cryptobot.bot;

import com.skillbox.cryptobot.bot.command.NotifyUserService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;


@Service
@Slf4j
public class CryptoBot extends TelegramLongPollingCommandBot {

    private final String botUsername;
    private final NotifyUserService notifyUserService;

    public CryptoBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            List<IBotCommand> commandList, NotifyUserService notifyUserService
    ) {
        super(botToken);
        this.botUsername = botUsername;
        this.notifyUserService = notifyUserService;
        commandList.forEach(this::register);
    }

    @PostConstruct
    public void init() {
        notifyUserService.setBotSender(this);
        log.info("NotifyUser инициализирован с bot sender");
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        log.info("Получено некорректное сообщение: {}", update);
    }
}
