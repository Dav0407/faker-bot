package com.igriss;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.seeder.*;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;


public class TelegramBotUpdateHandler {
    public TelegramBot bot = new TelegramBot(ResourceBundle.getBundle("settings").getString("bot.token"));
    public static ConcurrentHashMap<Long, State> usersState = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Long, FakerApplicationGenerateRequest> generateDataRequest = new ConcurrentHashMap<>();
    public void handle(Update update){
        Message message = update.message();
        CallbackQuery callbackQuery = update.callbackQuery();
        if (message != null) {
            Chat chat = message.chat();
            Long chatId = chat.id();
            String text = message.text();
            if(text.equals("/start")){
                SendMessage sendMessage = new SendMessage(chatId, "Assalomu Alaykum! I am @my_seeder_bot\n\n" +
                        "To generate data send /generate command.");
                bot.execute(sendMessage);
            } else if (text.equals("/generate")) {
                SendMessage sendMessage = new SendMessage(chatId, "Send File name");
                bot.execute(sendMessage);
                usersState.put(chatId, State.FILE_NAME);
                generateDataRequest.put(chatId, new FakerApplicationGenerateRequest());
            }else if (State.FILE_NAME.equals(usersState.get(chatId))) {
                SendMessage sendMessage = new SendMessage(chatId, "Send row count");
                bot.execute(sendMessage);
                usersState.put(chatId, State.ROW_COUNT);
                generateDataRequest.get(chatId).setFileName(text);
            }else if (State.ROW_COUNT.equals(usersState.get(chatId))) {
                SendMessage sendMessage = new SendMessage(chatId, "Choose file type (JSON, CSV SQL):");
                bot.execute(sendMessage);
                usersState.put(chatId, State.FILE_TYPE);
                generateDataRequest.get(chatId).setCount(Integer.parseInt(text));

                //generateDataRequest.get(chatId).setFileType(FileType.JSON);
            }else if (State.FILE_TYPE.equals(usersState.get(chatId))) {
                SendMessage sendMessage = new SendMessage(chatId, "Choose fields:");
                sendMessage.replyMarkup(getInlineMarkupKeyboard());
                bot.execute(sendMessage);
                usersState.put(chatId, State.FIELDS);
                generateDataRequest.get(chatId).setFileType(FileType.findByName(text));

            } else{
                DeleteMessage deleteMessage = new DeleteMessage(chatId, message.messageId());
                bot.execute(deleteMessage);
            }
        }else{
            FieldType[] fieldTypes = FieldType.values();
            String data = callbackQuery.data();
            Message message1 = callbackQuery.message();
            Chat chat = message1.chat();
            Long chatID = chat.id();

            if (data.equals("g")){
                FakerApplicationService fakerApplicationService = new FakerApplicationService();
                String path = fakerApplicationService.processRequest(generateDataRequest.get(chatID));
                try{
                    SendDocument sendDocument = new SendDocument(chatID, Files.readAllBytes(Path.of(path)));
                    bot.execute(sendDocument);
                    bot.execute(new DeleteMessage(chatID, message1.messageId()));
                }catch(IOException e){
                    AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(callbackQuery.id());
                    answerCallbackQuery.text("\uD83D\uDFE5Error: Please retry again later");
                    answerCallbackQuery.showAlert(true);
                    bot.execute(answerCallbackQuery);
                }
            }else{
                //FieldType fieldType = fieldTypes[Integer.parseInt(data)];
                FieldType fieldType = fieldTypes[Integer.parseInt(data)];
                generateDataRequest.get(chatID).getFields().add(new Field(fieldType.name().toLowerCase(), fieldType, 0, 0));

            }
        }

    }

    private static Keyboard getInlineMarkupKeyboard() {
        FieldType[] fieldTypes = FieldType.values();
        InlineKeyboardButton[][] buttons = new InlineKeyboardButton[12][2];
        for (int i = 0; i < fieldTypes.length/2; i++) {
            InlineKeyboardButton button1 = new InlineKeyboardButton(fieldTypes[i * 2].name());
            InlineKeyboardButton button2 = new InlineKeyboardButton(fieldTypes[i * 2+1].name());

            button1.callbackData(""+i*2);
            button2.callbackData(""+(i*2+1));
            buttons[i][0] = button1;
            buttons[i][1] = button2;
        }
        
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(buttons);
        InlineKeyboardButton generate = new InlineKeyboardButton("✅Generate");
        generate.callbackData("g");
        inlineKeyboardMarkup.addRow(generate);
        return inlineKeyboardMarkup;
    }
}