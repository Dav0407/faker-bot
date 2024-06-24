package com.igriss;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;

import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TelegramBotRunner {
    public static final ResourceBundle settings = ResourceBundle.getBundle("settings");
    public static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    public static final ThreadLocal <TelegramBotUpdateHandler> telegramBotUpdateHandlerThreadLocal = ThreadLocal.withInitial(TelegramBotUpdateHandler::new);

    public static void main(String[] args) {
        TelegramBot telegramBot = new TelegramBot(settings.getString("bot.token"));
        telegramBot.setUpdatesListener((updates)->{
            for (Update update : updates)
                CompletableFuture.runAsync(()-> telegramBotUpdateHandlerThreadLocal.get().handle(update), executorService);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        },
        Throwable :: printStackTrace);
    }
}
