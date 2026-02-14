package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.entity.User;
import com.skillbox.cryptobot.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Service
@Slf4j
@AllArgsConstructor
public class GetSubscriptionCommand implements IBotCommand {

    private final UserService userService;
    private final NotifyUserService notifyUserService;

    @Override
    public String getCommandIdentifier() {
        return "get_subscription";
    }

    @Override
    public String getDescription() {
        return "Возвращает текущую подписку";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        User existedUser = userService.getUserByTelegramId(message.getFrom().getId());
        if (existedUser == null) {
            notifyUserService.userNotExistMessage(message.getFrom().getId());
            return;
        }
        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());
        if (existedUser.getSubscriptionPrice() != null) {
            try {
                answer.setText("Вы подписаны на стоимость биткоина " + existedUser.getSubscriptionPrice() + " USD");
                absSender.execute(answer);
            } catch (Exception e) {
                log.error("Ошибка возникла /get_subscription методе", e);
            }
        } else {
            try {
                answer.setText("Активные подписки отсутствуют");
                absSender.execute(answer);
            } catch (Exception e) {
                log.error("Ошибка возникла /get_subscription методе", e);
            }
        }
    }
}