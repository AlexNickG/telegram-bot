package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.entity.User;
import com.skillbox.cryptobot.service.CryptoCurrencyService;
import com.skillbox.cryptobot.service.UserService;
import com.skillbox.cryptobot.utils.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Обработка команды подписки на курс валюты
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SubscribeCommand implements IBotCommand {

    private final NotifyUserService notifyUserService;

    private final CryptoCurrencyService service;

    private final UserService userService;

    @Override
    public String getCommandIdentifier() {
        return "subscribe";
    }

    @Override
    public String getDescription() {
        return "Подписывает пользователя на стоимость биткоина";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());
        String[] text = message.getText().split(" ");
        if (text.length < 2) {
            try {
                answer.setText("Неправильный ввод цены");
                absSender.execute(answer);
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке сообщения пользователю. ", e);
            }
            return;
        }
        double subscriptionPrice = Double.parseDouble(text[1]);
        User existedUser = userService.getUserByTelegramId(message.getFrom().getId());
        if (existedUser != null) {
            existedUser.setSubscriptionPrice(subscriptionPrice);
            userService.saveUser(existedUser);
        } else {
            notifyUserService.userNotExistMessage(message.getFrom().getId());
            return;
        }

        double actualBitcoinPrice = service.getBitcoinPrice();
        notifyUserService.setBitcoinPrice(actualBitcoinPrice);
        try {
            answer.setText("Текущая цена биткоина " + TextUtil.toString(actualBitcoinPrice) + " USD");
            absSender.execute(answer);
            answer.setText("Новая подписка создана на стоимость " + subscriptionPrice + " USD");
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения пользователю. ", e);
        }

        if (actualBitcoinPrice < subscriptionPrice) {
            notifyUserService.processUserNotification(existedUser);
        }

    }
}