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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private final SendMessage answer = new SendMessage();

    private static final Pattern SUBSCRIBE_PATTERN = Pattern.compile("^/subscribe\\s+(\\d*\\.?\\d+)$");

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
        answer.setChatId(message.getChatId());
        Double subscriptionPrice = extractSubscriptionPrice(message.getText());

        if (subscriptionPrice == null) {
            try {
                answer.setText("Неправильный ввод цены");
                absSender.execute(answer);
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке сообщения пользователю. ", e);
            }
            return;
        }

        User existedUser = userService.getUserByTelegramId(message.getFrom().getId());
        if (existedUser != null) {
            existedUser.setSubscriptionPrice(subscriptionPrice);
            userService.saveUser(existedUser);
        } else {
            notifyUserService.userNotExistMessage(message.getFrom().getId());
            return;
        }

        double actualBitcoinPrice = service.getBitcoinPrice();
        notifyUserService.setBitcoinPrice(actualBitcoinPrice);//т.к. цена обновляется каждые две минуты, установим актуальную цену для отправки в сообщении

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

    private Double extractSubscriptionPrice(String text) {
        if (text == null) return null;
        Matcher matcher = SUBSCRIBE_PATTERN.matcher(text.trim());
        if (!matcher.matches()) return null;
        return Double.parseDouble(matcher.group(1));
    }
}