package com.freemyip.mynotesproject.MyNotes.controllers.telegrambot;

import com.freemyip.mynotesproject.MyNotes.models.UserStatus;
import com.freemyip.mynotesproject.MyNotes.models.content.Note;
import com.freemyip.mynotesproject.MyNotes.models.content.NoteCategory;
import com.freemyip.mynotesproject.MyNotes.services.RegistrationService;
import com.freemyip.mynotesproject.MyNotes.services.UserStatusService;
import com.freemyip.mynotesproject.MyNotes.services.content.NoteCategoryService;
import com.freemyip.mynotesproject.MyNotes.services.content.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

@Controller
@RequiredArgsConstructor
@Log4j2
public class UpdateController {
    private TelegramBot telegramBot;
    private final UserStatusService userStatusService;
    private final RegistrationService registrationService;
    private final NoteCategoryService noteCategoryService;
    private final NoteService noteService;
    private final static Map<Long, List<String>> usersFiles = new HashMap<>();
    private final static Map<Long, Timer> usersTimer = new HashMap<>();

    public void telegramBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void checkUpdate(Update update) {
        if (update == null) {
            log.info("Update is null");

            return;
        }

        if (update.hasMessage()) {
            messageProcessing(update);
        } else if (update.hasCallbackQuery()) {
            callbackProcessing(update);
        } else {
            createSendMessage(update, "Unsupported message", null);
        }
    }

    private void messageProcessing(Update update) {
        Message message = update.getMessage();
        Chat forwardChat = message.getForwardFromChat();
        long chatId = message.getChatId();
        UserStatus userStatus = userStatusService.getUserStatusByTelegramUserId(chatId);
        String currentStep = userStatus.getCurrentStep();
        String userRegistrationStatus = userStatus.getUserRegistrationStatus();
        InlineKeyboardMarkup keyboardMarkup;
        String text;

        if (message.getForwardFrom() != null) {
            createSendMessage(update, "Forwarding messages from private chats has not yet been implemented.", null);
        } else if (forwardChat != null) {
            userStatus.setCurrentStep(UserStatus.FORWARD_FROM_CHANEL);
            restartTimer(userStatus, update);
        } else if (message.hasText()) {
            text = message.getText();

            if (userRegistrationStatus == null || userRegistrationStatus.equals(UserStatus.UNREGISTERED)) {
                createSendMessage(update, registrationService.registration(update, userStatus), null);
            } else if (text.startsWith("/")) {
                switch (text) {
                    case "/start" -> startAction(update, userStatus);
                    case "/help" -> helpAction(update, userStatus);
                    case "/cancel" -> {
                        cancelAction(userStatus);
                        createSendMessage(update, "Processing of all actions is completed.", null);
                    }
                    case "/categories" -> {
                        keyboardMarkup = new InlineKeyboardMarkup();

                        cancelAction(userStatus);
                        createSendMessage(
                                update,
                                noteCategoryService.showListOfCategories(userStatus, keyboardMarkup),
                                keyboardMarkup
                        );
                    }
                    default -> defaultSlashAction(update, userStatus);
                }
            } else if (currentStep.equals(UserStatus.WAITING_CATEGORY_NAME)) {
                keyboardMarkup = new InlineKeyboardMarkup();

                createSendMessage(update, noteCategoryService.createCategory(update, userStatus), null);
                if (userStatus.getCurrentCategoryId() != null) {
                    createSendMessage(update,
                            noteService.showListOfNotes(userStatus, keyboardMarkup),
                            keyboardMarkup);
                }
            } else if (currentStep.equals(UserStatus.UPDATING_CATEGORY)) {
                keyboardMarkup = new InlineKeyboardMarkup();
                String response;

                createSendMessage(update,
                        (response = noteCategoryService.updateCategory(update, userStatus, keyboardMarkup)).isEmpty() ?
                        noteService.showListOfNotes(userStatus, keyboardMarkup) :
                        response,
                        keyboardMarkup);
            } else if (currentStep.equals(UserStatus.DELETING_CATEGORY)) {
                createSendMessage(update, "Confirm the deletion or return to the category.",
                        InlineKeyboardMarkup.builder().keyboardRow(List.of(
                                InlineKeyboardButton.builder().text("Delete").callbackData("forward").build(),
                                InlineKeyboardButton.builder().text("Back").callbackData("back").build()
                        )).build());
            } else if (currentStep.equals(UserStatus.NOTHING) ||
                currentStep.equals(UserStatus.WAITING_CATEGORY_FOR_NOTE) ||
                currentStep.equals(UserStatus.CREATING_CATEGORY_FOR_NOTE) ||
                currentStep.equals(UserStatus.WAITING_NOTE_NAME) ||
                currentStep.equals(UserStatus.WAITING_NOTE_LINK) ||
                currentStep.equals(UserStatus.WAITING_NOTE_CONTENT)) {
                keyboardMarkup = new InlineKeyboardMarkup();
                List<String> fileIds = new ArrayList<>();

                createShowNoteMessage(userStatus, noteService.createNote(update, userStatus, fileIds, keyboardMarkup),
                        fileIds, keyboardMarkup);
            } else if (currentStep.equals(UserStatus.DELETING_NOTE)) {
                createSendMessage(update, "Confirm the deletion or return to the note.",
                        InlineKeyboardMarkup.builder().keyboardRow(List.of(
                                InlineKeyboardButton.builder().text("Delete").callbackData("forward").build(),
                                InlineKeyboardButton.builder().text("Back").callbackData("back").build()
                        )).build());
            }
        } else if (message.hasVoice() || message.hasAudio() || message.hasVideo() || message.hasPhoto()) {
            String fileId;
            Long telegramId = userStatus.getTelegramUserId();

            if (message.hasVoice()) {
                fileId = "voice_" + message.getVoice().getFileId();
            } else if (message.hasAudio()) {
                fileId = "audio_" + message.getAudio().getFileId();
            } else if (message.hasVideo()) {
                fileId = "video_" + message.getVideo().getFileId();
            } else if (message.hasPhoto()) {
                List<PhotoSize> photo = message.getPhoto();

                fileId = "photo_" + photo.get(photo.size() - 1).getFileId();
            } else {
                fileId = "";
            }

            usersFiles.putIfAbsent(telegramId, new ArrayList<>());
            usersFiles.get(telegramId).add(fileId);
            restartTimer(userStatus, null);
        } else {
            createSendMessage(update, "Unsupported message", null);
            log.info(message.getChatId() + " unsupported message.");
        }

        userStatusService.updateUserStatus(userStatus);
    }

    private void restartTimer(UserStatus userStatus, Update update) {
        Long telegramId = userStatus.getTelegramUserId();
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        if (usersTimer.containsKey(telegramId)) {
            usersTimer.get(telegramId).cancel();
        }

        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                List<String> fileIds = new ArrayList<>();

                if (userStatus.getCurrentStep().equals(UserStatus.FORWARD_FROM_CHANEL)) {
                    createSendMessage(update, noteService.createNote(update, userStatus, fileIds, keyboardMarkup), keyboardMarkup);
                } else {
                    createShowNoteMessage(userStatus,
                            noteService.createNoteWithFiles(userStatus, usersFiles.remove(telegramId), fileIds, keyboardMarkup),
                            fileIds,
                            userStatus.getCurrentStep().equals(UserStatus.WAITING_CATEGORY_FOR_NOTE) ? null : keyboardMarkup);
                }
                userStatusService.updateUserStatus(userStatus);
            }
        }, 500);

        usersTimer.put(telegramId, timer);
    }

    private void callbackProcessing(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        UserStatus userStatus = userStatusService.getUserStatusByTelegramUserId(chatId);
        String currentStep = userStatus.getCurrentStep();
        InlineKeyboardMarkup keyboardMarkup;

        switch (data) {
            case "createCategory" -> {
                cancelAction(userStatus);
                userStatus.setCurrentStep(UserStatus.WAITING_CATEGORY_NAME);
                createSendMessage(update, "Enter a name for the category:", null);
            }
            case "createNote" -> {
                if (currentStep.equals(UserStatus.WAITING_CATEGORY_FOR_NOTE) ||
                        currentStep.equals(UserStatus.CREATING_CATEGORY_FOR_NOTE) ||
                        currentStep.equals(UserStatus.WAITING_NOTE_NAME) ||
                        currentStep.equals(UserStatus.WAITING_NOTE_LINK) ||
                        currentStep.equals(UserStatus.WAITING_NOTE_CONTENT) ||
                        currentStep.equals(UserStatus.DELETING_CATEGORY) ||
                        currentStep.equals(UserStatus.DELETING_NOTE) ||
                currentStep.equals(UserStatus.MOVING_NOTE)) {
                    cancelAction(userStatus);
                }
                if (userStatus.getCurrentCategoryId() == null) {
                    userStatus.setCurrentStep(UserStatus.WAITING_CATEGORY_FOR_NOTE);
                    createSendMessage(update,
                            "Select the category in which you want to create a note:\n\n" +
                                    noteCategoryService.showListOfCategories(userStatus, new InlineKeyboardMarkup()),
                            null);
                } else {
                    userStatus.setCurrentStep(UserStatus.WAITING_NOTE_NAME);
                    createSendMessage(update, "Enter a name for the note:", null);
                }
            }
            case "editCategory" -> {
                keyboardMarkup = new InlineKeyboardMarkup();

                userStatus.setCurrentStep(UserStatus.UPDATING_CATEGORY);
                createSendMessage(update, noteCategoryService.updateCategory(update, userStatus, keyboardMarkup), keyboardMarkup);
            }
            case "deleteCategory" -> {
                if (userStatus.getCurrentCategoryId() == null) {
                    createSendMessage(update, "Select the category you want to delete:\n" +
                            noteCategoryService.showListOfCategories(userStatus, new InlineKeyboardMarkup()), null);
                } else {
                    userStatus.setCurrentStep(UserStatus.DELETING_CATEGORY);
                    createSendMessage(update, "Are you sure you want to delete the \"" +
                                    noteCategoryService.getCategoryById(userStatus.getCurrentCategoryId()).getName() +
                                    "\" category?",
                            InlineKeyboardMarkup.builder().keyboardRow(
                                    List.of(InlineKeyboardButton.builder().text("Delete").callbackData("forward").build(),
                                            InlineKeyboardButton.builder().text("Back").callbackData("back").build())
                            ).build());
                }
            }
            case "deleteNote" -> {
                if (userStatus.getCurrentCategoryId() == null) {
                    createSendMessage(update, "Select the category and the note to delete.\n" +
                            noteCategoryService.showListOfCategories(userStatus, new InlineKeyboardMarkup()), null);
                } else if (userStatus.getCurrentNoteId() == null) {
                    createSendMessage(update, "Select the note to delete." +
                            noteService.showListOfNotes(userStatus, new InlineKeyboardMarkup()), null);
                } else {
                    userStatus.setCurrentStep(UserStatus.DELETING_NOTE);
                    createSendMessage(update, "Are you sure you want to delete the \"" +
                                    noteService.getNoteById(userStatus.getCurrentNoteId()).getName() +
                                    "\" note?",
                            InlineKeyboardMarkup.builder().keyboardRow(
                                    List.of(InlineKeyboardButton.builder().text("Delete").callbackData("forward").build(),
                                            InlineKeyboardButton.builder().text("Back").callbackData("back").build())
                            ).build());
                }
            }
            case "moveNote" -> {
                if (userStatus.getCurrentCategoryId() == null) {
                    createSendMessage(update, "Select the category and the note to move.\n" +
                            noteCategoryService.showListOfCategories(userStatus, new InlineKeyboardMarkup()), null);
                } else if (userStatus.getCurrentNoteId() == null) {
                    createSendMessage(update, "Select the note to move.\n" +
                            noteService.showListOfNotes(userStatus, new InlineKeyboardMarkup()), null);
                } else {
                    userStatus.setCurrentStep(UserStatus.MOVING_NOTE);
                    createSendMessage(update, "Select the category to which you want to transfer the note:\n" +
                            noteCategoryService.showListOfCategories(userStatus, new InlineKeyboardMarkup()), null);
                }
            }
            case "addFile" -> {
                userStatus.setCurrentStep(UserStatus.ADD_FILE);
                createSendMessage(update, "Select the media files to download or record a voice message.", null);
            }
            case "forward" -> {
                switch (currentStep) {
                    case UserStatus.CREATING_CATEGORY_FOR_NOTE -> {
                        userStatus.setCurrentStep(UserStatus.WAITING_NOTE_NAME);
                        createSendMessage(update, "Enter a name for the note:", null);
                    }
                    case UserStatus.WAITING_NOTE_LINK -> {
                        Note note = noteService.getNoteById(userStatus.getCurrentNoteId());
                        keyboardMarkup = new InlineKeyboardMarkup();

                        if (note.getContent() == null) {
                            userStatus.setCurrentStep(UserStatus.WAITING_NOTE_CONTENT);
                            createSendMessage(update, "Enter a content for the note:",
                                    InlineKeyboardMarkup.builder().keyboardRow(
                                            Collections.singletonList(
                                                    InlineKeyboardButton.builder().text("Skip").callbackData("forward").build()
                                            )
                                    ).build());
                        } else {
                            List<String> fileIds = new ArrayList<>();

                            userStatus.setCurrentStep(UserStatus.NOTHING);
                            createShowNoteMessage(userStatus, "Done. The \"" + note.getName() + "\" note has been created.\n\n" +
                                    noteService.showNote(userStatus, note.getTransliterateName(), fileIds, keyboardMarkup),
                                    fileIds, keyboardMarkup);
                        }
                    }
                    case UserStatus.WAITING_NOTE_CONTENT -> {
                        Note note = noteService.getNoteById(userStatus.getCurrentNoteId());
                        keyboardMarkup = new InlineKeyboardMarkup();
                        List<String> fileIds = new ArrayList<>();

                        userStatus.setCurrentStep(UserStatus.NOTHING);
                        createShowNoteMessage(userStatus, "Done. The \"" + note.getName() +
                                "\" has been created.\n\n" +
                                noteService.showNote(userStatus, note.getTransliterateName(), fileIds, keyboardMarkup),
                                fileIds, keyboardMarkup);
                    }
                    case UserStatus.DELETING_CATEGORY -> {
                        keyboardMarkup = new InlineKeyboardMarkup();

                        noteCategoryService.deleteCategoryById(userStatus.getCurrentCategoryId());
                        cancelAction(userStatus);
                        createSendMessage(update, noteCategoryService.showListOfCategories(userStatus, keyboardMarkup), keyboardMarkup);
                    }
                    case UserStatus.DELETING_NOTE -> {
                        keyboardMarkup = new InlineKeyboardMarkup();

                        noteService.deleteNoteById(userStatus.getCurrentNoteId());
                        userStatus.setCurrentStep(UserStatus.NOTHING);
                        userStatus.setCurrentNoteId(null);
                        createSendMessage(update, noteService.showListOfNotes(userStatus, keyboardMarkup), keyboardMarkup);
                    }
                    default -> createSendMessage(update, "what are you doing?", null);
                }
            }
            case "back" -> {
                if (currentStep.equals(UserStatus.CREATING_CATEGORY_FOR_NOTE)) {
                    cancelAction(userStatus);
                    userStatus.setCurrentStep(UserStatus.WAITING_CATEGORY_FOR_NOTE);
                    createSendMessage(update,
                            "Select the category in which you want to create a note:\n\n" +
                                    noteCategoryService.showListOfCategories(userStatus, new InlineKeyboardMarkup()),
                            null);
                } else if (currentStep.equals(UserStatus.DELETING_CATEGORY)) {
                    keyboardMarkup = new InlineKeyboardMarkup();

                    userStatus.setCurrentStep(UserStatus.NOTHING);
                    createSendMessage(update, noteService.showListOfNotes(userStatus, keyboardMarkup), keyboardMarkup);
                } else if (currentStep.equals(UserStatus.DELETING_NOTE)) {
                    keyboardMarkup = new InlineKeyboardMarkup();
                    List<String> fileIds = new ArrayList<>();

                    userStatus.setCurrentStep(UserStatus.NOTHING);
                    createShowNoteMessage(userStatus,
                            noteService.showNote(userStatus, noteService.getNoteById(userStatus.getCurrentNoteId()).getTransliterateName(), fileIds, keyboardMarkup),
                            fileIds, keyboardMarkup);
                } else if (currentStep.equals(UserStatus.MOVING_NOTE)) {
                    cancelAction(userStatus);
                } else if (userStatus.getCurrentCategoryId() != null && userStatus.getCurrentNoteId() == null) {
                    keyboardMarkup = new InlineKeyboardMarkup();

                    userStatus.setCurrentCategoryId(null);
                    createSendMessage(update, noteCategoryService.showListOfCategories(userStatus, keyboardMarkup), keyboardMarkup);
                } else if (userStatus.getCurrentCategoryId() != null) {
                    keyboardMarkup = new InlineKeyboardMarkup();

                    userStatus.setCurrentNoteId(null);
                    createSendMessage(update, noteService.showListOfNotes(userStatus, keyboardMarkup), keyboardMarkup);
                }
            }
        }

        userStatusService.updateUserStatus(userStatus);
    }

    private void startAction(Update update, UserStatus userStatus) {
        cancelAction(userStatus);
        createSendMessage(update, "Create a category or note:", InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder().text("Category").callbackData("createCategory").build(),
                        InlineKeyboardButton.builder().text("Note").callbackData("createNote").build()
                ))
                .build());
    }

    private void helpAction(Update update, UserStatus userStatus) {
        cancelAction(userStatus);
        createSendMessage(update,
                """
                        use these commands:
                        
                        /start - creating a category or record
                        /cancel - stopping the current operation
                        /categories - displaying all categories
                        
                        Important! Users who wish to try the application should remember that access to the application is only available during the first 24 hours after registration. This is a necessary measure as I am limited in resources.
                                                
                        The application is designed for creating and managing notes. This is its first version. Its main goal is to overcome the difficulties associated with using "Saved Messages". Now everything that might be of interest can be cataloged, and you won't have to scroll through the feed to find a desired recipe, article, or list of planned purchases or tasks.
                                                
                        A note consists of a name, a link, and text. When creating a note, the only mandatory field is the name; the link and text content fields are optional.
                                                
                        You can add media files to a note, such as voice messages, audio, video, and photos. Media files can be added to an existing note or you can create a new note based on the uploaded file. To do this, simply upload the file, and the bot will then offer to create a new note with the file(s) included.
                                                
                        You can forward messages from channels to the bot. In this case, a direct link to the message in the channel will be added to the corresponding field in the new note. Similarly, you can save a YouTube video or an article from any other resource. The only requirement for this feature to work correctly is that the message must start with "https". Currently, forwarding messages from private chats does not work, as Telegram prohibits direct links to messages in private chats. Implementing proper display of such messages poses certain challenges, but this feature will be added in the future.
                                                
                        Additionally, we were unable to implement a suitable method for editing notes through the bot, so you can edit notes via the website - https://mynotesproject.freemyip.com.
                                                
                        Work on the application is ongoing.
                                                
                        If you have any questions or suggestions, please be sure to let us know — https://github.com/osivaM/MyNotes
                                                                        
                                                
                        Важно! Пользователи, желающие опробовать приложение, должны помнить, что доступ к приложению возможен только в течение первых суток после регистрации. Это вынужденная мера — я ограничен в ресурсах.
                                                
                        Приложение предназначено для создания и управления заметками. Это первая версия. Его основная задача — преодолеть трудности, связанные с использованием "Сохраненных сообщений". Теперь всё, что может быть интересно, можно каталогизировать и в дальнейшем не листать ленту в поисках нужного рецепта, статьи или списка запланированных покупок или дел.
                                                
                        Заметка состоит из имени, какой-либо ссылки и текста. При создании заметки обязательным полем для ввода является только имя, поля ссылки и текстового контента можно опустить.
                                                
                        К заметке можно добавлять медиафайлы, такие как голосовые сообщения, аудио, видео и фото. Медиафайлы можно добавлять как к уже существующей заметке, так и создать новую заметку на основе загруженного файла. Для этого нужно просто загрузить файл, а затем бот предложит создать новую заметку, в которую будет добавлен этот файл или файлы.
                                                
                        Можно пересылать в бота сообщения из каналов. В этом случае прямая ссылка на сообщение в канале будет добавлена в соответствующее поле в новой заметке. Точно так же можно сохранить, например, видео с YouTube или статью с любого другого ресурса. Единственным условием для корректной работы этой возможности является наличие "https" в начале сообщения. Временно не работает пересылка сообщений из приватных чатов, так как Telegram запрещает прямые ссылки на сообщения в приватных чатах. Реализация нормального отображения таких сообщений представляет определённые трудности, но в будущем такая возможность тоже появится.
                                                
                        Также не удалось реализовать приемлемый способ редактирования заметки через бота, поэтому отредактировать заметку можно через сайт - https://mynotesproject.freemyip.com.
                                                
                        Работа над приложением продолжается.
                                                
                        Если у вас есть вопросы или предложения, обязательно напишите об этом — https://github.com/osivaM/MyNotes""",
                null);
    }

    private void cancelAction(UserStatus userStatus) {
        if (userStatus.getCurrentStep().equals(UserStatus.CREATING_CATEGORY_FOR_NOTE)) {
            noteCategoryService.deleteCategoryById(userStatus.getCurrentCategoryId());
            noteService.deleteNoteById(userStatus.getCurrentNoteId());
        } else if (userStatus.getCurrentStep().equals(UserStatus.WAITING_CATEGORY_FOR_NOTE) ||
                userStatus.getCurrentStep().equals(UserStatus.WAITING_NOTE_LINK) ||
        userStatus.getCurrentStep().equals(UserStatus.WAITING_NOTE_CONTENT)) {
            noteService.deleteNoteById(userStatus.getCurrentNoteId());
        }
        userStatus.setCurrentStep(UserStatus.NOTHING);
        userStatus.setCurrentCategoryId(null);
        userStatus.setCurrentNoteId(null);
    }

    private void defaultSlashAction(Update update, UserStatus userStatus) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        String transliterateName = update.getMessage().getText().substring(1);
        long userId = userStatus.getUserId();

        if (userStatus.getCurrentStep().equals(UserStatus.CREATING_CATEGORY_FOR_NOTE)) {
            createSendMessage(update, noteService.createNote(update, userStatus, new ArrayList<>(), keyboardMarkup), keyboardMarkup);
        } else if (userStatus.getCurrentStep().equals(UserStatus.DELETING_CATEGORY)) {
            createSendMessage(update, "Confirm the deletion or return to the category.",
                    InlineKeyboardMarkup.builder().keyboardRow(List.of(
                            InlineKeyboardButton.builder().text("Delete").callbackData("forward").build(),
                            InlineKeyboardButton.builder().text("Back").callbackData("back").build()
                    )).build());
        } else if (userStatus.getCurrentStep().equals(UserStatus.DELETING_NOTE)) {
            createSendMessage(update, "Confirm the deletion or return to the note.",
                    InlineKeyboardMarkup.builder().keyboardRow(List.of(
                            InlineKeyboardButton.builder().text("Delete").callbackData("forward").build(),
                            InlineKeyboardButton.builder().text("Back").callbackData("back").build()
                    )).build());
        } else if (noteCategoryService.existsByTransliterateNameAndUserId(transliterateName, userId)) {
            if (userStatus.getCurrentStep().equals(UserStatus.MOVING_NOTE)) {
                NoteCategory noteCategory = noteCategoryService.getCategoryByTransliterateNameAndUserId(transliterateName, userId);
                Note note = noteService.getNoteById(userStatus.getCurrentNoteId());

                if (noteService.existsByTransliterateNameAndCategoryId(note.getTransliterateName(), noteCategory.getId())) {
                    createSendMessage(update, "There is already a note with the same name in the selected category, select another category.\n" +
                            noteCategoryService.showListOfCategories(userStatus, new InlineKeyboardMarkup()), null);
                } else {
                    List<String> fileIds = new ArrayList<>();

                    note.setNoteCategory(noteCategory);
                    noteService.updateNote(note);
                    userStatus.setCurrentStep(UserStatus.NOTHING);
                    userStatus.setCurrentCategoryId(noteCategory.getId());
                    createShowNoteMessage(userStatus, noteService.showNote(userStatus, note.getTransliterateName(), fileIds, keyboardMarkup),
                            fileIds, keyboardMarkup);
                }

                return;
            }
            userStatus.setCurrentCategoryId(
                    noteCategoryService.getCategoryByTransliterateNameAndUserId(transliterateName, userId).getId());
            if (userStatus.getCurrentStep().equals(UserStatus.WAITING_CATEGORY_FOR_NOTE)) {
                userStatus.setCurrentStep(UserStatus.WAITING_NOTE_NAME);
                createSendMessage(update, "Enter a name for the note:", null);

                return;
            }
            createSendMessage(update,
                    noteService.showListOfNotes(userStatus, keyboardMarkup),
                    keyboardMarkup);
        } else if (userStatus.getCurrentStep().equals(UserStatus.WAITING_CATEGORY_FOR_NOTE)) {
            createSendMessage(update,
                    "The selected command is not part of the list of categories. Select the appropriate command:\n" +
                    noteCategoryService.showListOfCategories(userStatus, new InlineKeyboardMarkup()),
                    null);
        } else if (noteService.existsByTransliterateNameAndUserId(transliterateName, userId)) {
            List<String> fileIds = new ArrayList<>();

            createShowNoteMessage(userStatus, noteService.showNote(userStatus, transliterateName, fileIds, keyboardMarkup),
                    fileIds, keyboardMarkup);
        } else {
            createSendMessage(update,"Unknown command.", null);
            helpAction(update, userStatus);
        }
    }

    private void createSendMessage(Update update, String text, InlineKeyboardMarkup keyboardMarkup) {
        long chatId;

        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
        } else {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        }

        telegramBot.sendTextMessage(SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboardMarkup)
                .build());
    }

    private void createShowNoteMessage(UserStatus userStatus, String response, List<String> fileIds, InlineKeyboardMarkup keyboardMarkup) {
        long chatId = userStatus.getTelegramUserId();

        if (fileIds.isEmpty()) {
            telegramBot.sendTextMessage(SendMessage.builder()
                    .chatId(chatId)
                    .text(response)
                    .replyMarkup(keyboardMarkup)
                    .build());
        } else {
            List<InputMedia> mediaGroup = new ArrayList<>();
            int endOfList = fileIds.size() - 1;

            telegramBot.sendTextMessage(SendMessage.builder()
                    .chatId(chatId)
                    .text(response)
                    .build());
            for (int i = 0; i < fileIds.size(); i++) {
                if (mediaGroup.size() == 10 || (i == endOfList && mediaGroup.size() > 1)) {
                    telegramBot.sendMediaGroupMessage(SendMediaGroup.builder()
                            .chatId(chatId)
                            .medias(mediaGroup)
                            .build());
                    mediaGroup.clear();
                } else if (i == endOfList && mediaGroup.size() == 1) {
                    if (mediaGroup.get(0) instanceof InputMediaVideo) {
                        telegramBot.sendVideoMessage(SendVideo.builder().chatId(chatId).video(new InputFile(mediaGroup.get(0).getMedia())).build());
                    } else {
                        telegramBot.sendPhotoMessage(SendPhoto.builder().chatId(chatId).photo(new InputFile(mediaGroup.get(0).getMedia())).build());
                    }
                }

                String fileId = fileIds.get(i);
                String id = fileId.substring(fileId.indexOf('_') + 1);

                if (fileId.startsWith("voice")) {
                    telegramBot.sendVoiceMessage(SendVoice.builder()
                            .chatId(chatId)
                            .voice(new InputFile(id))
                            .replyMarkup(i == endOfList ? keyboardMarkup : null)
                            .build());
                } else if (fileId.startsWith("audio")) {
                    telegramBot.sendAudioMessage(SendAudio.builder()
                            .chatId(chatId)
                            .audio(new InputFile(id))
                            .replyMarkup(i == endOfList ? keyboardMarkup : null)
                            .build());
                } else if (fileId.startsWith("video")) {
                    if (i == endOfList) {
                        telegramBot.sendVideoMessage(SendVideo.builder()
                                .chatId(chatId)
                                .video(new InputFile(id))
                                .replyMarkup(keyboardMarkup)
                                .build());
                    } else {
                        mediaGroup.add(InputMediaVideo.builder().media(id).build());
                    }
                } else {
                    if (i == endOfList) {
                        telegramBot.sendPhotoMessage(SendPhoto.builder()
                                .chatId(chatId)
                                .photo(new InputFile(id))
                                .replyMarkup(keyboardMarkup)
                                .build());
                    } else {
                        mediaGroup.add(InputMediaPhoto.builder().media(id).build());
                    }
                }
            }
        }
    }
}
