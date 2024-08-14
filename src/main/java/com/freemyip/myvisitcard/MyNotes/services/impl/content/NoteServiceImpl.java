package com.freemyip.myvisitcard.MyNotes.services.impl.content;

import com.freemyip.myvisitcard.MyNotes.models.User;
import com.freemyip.myvisitcard.MyNotes.models.UserStatus;
import com.freemyip.myvisitcard.MyNotes.models.content.Note;
import com.freemyip.myvisitcard.MyNotes.models.content.NoteCategory;
import com.freemyip.myvisitcard.MyNotes.repositories.UserRepository;
import com.freemyip.myvisitcard.MyNotes.repositories.content.NoteRepository;
import com.freemyip.myvisitcard.MyNotes.services.UserService;
import com.freemyip.myvisitcard.MyNotes.services.content.NoteCategoryService;
import com.freemyip.myvisitcard.MyNotes.services.content.NoteService;
import com.freemyip.myvisitcard.MyNotes.util.Transliterator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final NoteCategoryService noteCategoryService;
    private final Transliterator transliterator;
    private final UserService userService;

    @Override
    public List<Note> getAllNotesForCategoryFromUser(String name, User user) {
        return noteRepository.findAllByNoteCategoryNameAndUser(name, user);
    }

    @Override
    public Note getNoteById(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Note with id: " + id + " not found"));
    }

    @Override
    public boolean existsByTransliterateNameAndUserId(String transliterateName, long userId) {
        return noteRepository.existsByTransliterateNameAndUserId(transliterateName, userId);
    }

    @Override
    public boolean existsByTransliterateNameAndCategoryId(String transliterateName, long categoryId) {
        return noteRepository.existsByTransliterateNameAndNoteCategoryId(transliterateName, categoryId);
    }

    @Override
    public String showListOfNotes(UserStatus userStatus, InlineKeyboardMarkup inlineKeyboardMarkup) {
        StringBuilder response = new StringBuilder();
        String categoryName;
        List<Note> notes;
        List<List<InlineKeyboardButton>> keyboardButtons = new ArrayList<>();

        categoryName = noteCategoryService.getCategoryById(userStatus.getCurrentCategoryId()).getName();
        notes = noteRepository.getAllByNoteCategoryId(userStatus.getCurrentCategoryId());

        keyboardButtons.add(List.of(
                InlineKeyboardButton.builder().text("Create note").callbackData("createNote").build(),
                InlineKeyboardButton.builder().text("Edit category").callbackData("editCategory").build()
        ));
        keyboardButtons.add(List.of(
                InlineKeyboardButton.builder().text("Delete category").callbackData("deleteCategory").build(),
                InlineKeyboardButton.builder().text("Back").callbackData("back").build()));
        if (notes.isEmpty()) {
            response.append(categoryName).append(" is empty, create a note.\n\n");
        } else {
            response.append(categoryName).append("\n\n");
        }

        inlineKeyboardMarkup.setKeyboard(keyboardButtons);
        for (Note note : notes) {
            response.append(String.format("/%s -> %s\n", note.getTransliterateName(), note.getName()));
        }

        return response.toString();
    }

    @Override
    public String showNote(UserStatus userStatus, String transliterateName, List<String> fileIds, InlineKeyboardMarkup keyboardMarkup) {
        Long categoryId = userStatus.getCurrentCategoryId();
        StringBuilder response = new StringBuilder();

        if (categoryId == null) {
            response.append("To select a note, go to the appropriate category:\n\n")
                    .append(noteCategoryService.showListOfCategories(userStatus, keyboardMarkup));
        } else {
            Note note = noteRepository.getByTransliterateNameAndNoteCategoryId(transliterateName, categoryId);
            String categoryName = noteCategoryService.getCategoryById(categoryId).getName();

            if (note == null) {
                response.append("There is no corresponding note in the current \"")
                        .append(categoryName)
                        .append("\" category. To select a note, go to the appropriate category:\n\n")
                        .append(noteCategoryService.showListOfCategories(userStatus, keyboardMarkup));
            } else {
                List<String> ids = note.getFileIds();

                userStatus.setCurrentNoteId(note.getId());
                keyboardMarkup.setKeyboard(List.of(
                        List.of(
                                InlineKeyboardButton.builder().text("Edit").url("https://myvisitcard.freemyip.com/user/note/" +
                                        note.getId()).build(),
                                InlineKeyboardButton.builder().text("Move").callbackData("moveNote").build()
                        ),
                        List.of(
                                InlineKeyboardButton.builder().text("Delete").callbackData("deleteNote").build(),
                                InlineKeyboardButton.builder().text("Add file").callbackData("addFile").build()
                        ),
                        List.of(
                                InlineKeyboardButton.builder().text("Back").callbackData("back").build()
                        )
                ));
                if (!ids.isEmpty()) {
                    fileIds.addAll(ids);
                }
                if (noteRepository.countAllByTransliterateNameAndUserId(transliterateName, userStatus.getUserId()) > 1) {
                    response.append("(This note belongs to the \"").append(categoryName).append("\" category)\n\n");
                }
                response.append(note.getName()).append("\n")
                        .append(note.getCreateDate()).append("\n\n")
                        .append(note.getLink() == null ? "link: //" : note.getLink()).append("\n\n")
                        .append(note.getContent() == null ? "content: ..." : note.getContent());
            }
        }

        return response.toString();
    }

    @Override
    @Transactional
    public String createNote(Update update, UserStatus userStatus, List<String> fileIds, InlineKeyboardMarkup keyboardMarkup) {
        Message message = update.getMessage();
        Chat forwardChat = message.getForwardFromChat();
        String text = forwardChat == null ? message.getText() : "https://t.me/" + forwardChat.getUserName() + "/" + message.getForwardFromMessageId();
        String transliterateText = transliterator.transliterate(text);
        String response;
        long userId = userStatus.getUserId();
        List<InlineKeyboardButton> keyboardButtons = new ArrayList<>();
        Note note;

        switch (userStatus.getCurrentStep()) {
            case UserStatus.WAITING_CATEGORY_FOR_NOTE -> {
                if (noteCategoryService.existsByTransliterateNameAndUserId(transliterateText, userId)) {
                    keyboardButtons.add(InlineKeyboardButton.builder().text("Back").callbackData("back").build());

                    response = "It is not possible to create a category named \"" + text +"\".";
                } else {
                    NoteCategory noteCategory = new NoteCategory();

                    noteCategory.setName(text);
                    noteCategory.setUser(userService.getUserById(userStatus.getUserId()));
                    noteCategoryService.createCategory(noteCategory);

                    userStatus.setCurrentCategoryId(noteCategory.getId());
                    userStatus.setCurrentStep(UserStatus.CREATING_CATEGORY_FOR_NOTE);

                    keyboardButtons.add(InlineKeyboardButton.builder().text("Create").callbackData("forward").build());
                    keyboardButtons.add(InlineKeyboardButton.builder().text("Back").callbackData("back").build());

                    response = "Do you want to create a category named \"" + text + "\"?";
                }
            }
            case UserStatus.CREATING_CATEGORY_FOR_NOTE -> {
                keyboardButtons.add(InlineKeyboardButton.builder().text("Create").callbackData("forward").build());
                keyboardButtons.add(InlineKeyboardButton.builder().text("Back").callbackData("back").build());

                response = "Choose an action: create a \"" + noteCategoryService.getCategoryById(userStatus.getCurrentCategoryId()).getName() +
                        "\" category or go back.";
            }
            case UserStatus.WAITING_NOTE_NAME -> {
                if (noteRepository.existsByTransliterateNameAndNoteCategoryId(transliterateText, userStatus.getCurrentCategoryId())) {
                    response = "A note with this name already exists, select a name:";
                } else {
                    if (userStatus.getCurrentNoteId() == null) {
                        note = new Note();

                        note.setUser(userService.getUserById(userStatus.getUserId()));
                        note.setCreateDate(LocalDateTime.now());

                        userStatus.setCurrentStep(UserStatus.WAITING_NOTE_LINK);
                        response = "Enter the link for this note:";
                    } else {
                        note = noteRepository.getById(userStatus.getCurrentNoteId());

                        if (note.getLink() != null) {
                            userStatus.setCurrentStep(UserStatus.WAITING_NOTE_CONTENT);
                            response = "Enter a content for the note:";
                        } else {
                            userStatus.setCurrentStep(UserStatus.WAITING_NOTE_LINK);
                            response = "Enter the link for this note:";
                        }
                    }

                    note.setName(text);
                    note.setTransliterateName(transliterateText);
                    note.setNoteCategory(noteCategoryService.getCategoryById(userStatus.getCurrentCategoryId()));
                    noteRepository.save(note);
                    userStatus.setCurrentNoteId(note.getId());
                    keyboardButtons.add(InlineKeyboardButton.builder().text("Skip").callbackData("forward").build());
                }
            }
            case UserStatus.WAITING_NOTE_LINK -> {
                note = noteRepository.getById(userStatus.getCurrentNoteId());

                note.setLink(text);
                noteRepository.save(note);

                if (note.getContent() == null) {
                    userStatus.setCurrentStep(UserStatus.WAITING_NOTE_CONTENT);
                    response = "Enter a content for the note:";
                    keyboardButtons.add(InlineKeyboardButton.builder().text("Skip").callbackData("forward").build());
                } else {
                    userStatus.setCurrentStep(UserStatus.NOTHING);
                    response = "Done. The \"" + note.getName() + "\" note has been created.\n\n" +
                            showNote(userStatus, note.getTransliterateName(), fileIds, keyboardMarkup);
                }
            }
            case UserStatus.WAITING_NOTE_CONTENT -> {
                note = noteRepository.getById(userStatus.getCurrentNoteId());

                note.setContent(text);
                noteRepository.save(note);
                userStatus.setCurrentStep(UserStatus.NOTHING);
                response = "Done. The \"" + note.getName() + "\" note has been created.\n\n" +
                    showNote(userStatus, note.getTransliterateName(), fileIds, keyboardMarkup);
            }
            default -> {
                note = new Note();

                if (text.startsWith("https")) {
                    note.setLink(text);
                } else {
                    note.setContent(text);
                }

                note.setUser(userService.getUserById(userStatus.getUserId()));
                note.setCreateDate(LocalDateTime.now());
                noteRepository.save(note);

                userStatus.setCurrentNoteId(note.getId());
                userStatus.setCurrentStep(UserStatus.WAITING_CATEGORY_FOR_NOTE);

                response = "Select the category in which you want to create a note:\n\n" +
                        noteCategoryService.showListOfCategories(userStatus, new InlineKeyboardMarkup());
            }
        }

        if (keyboardMarkup.getKeyboard() == null) {
            keyboardMarkup.setKeyboard(Collections.singletonList(keyboardButtons));
        }

        return response;
    }

    @Override
    @Transactional
    public Long createNote(Note note) {
        String username = note.getUser().getUsername();
        User user = userRepository.findUserByUsername(username)
                        .orElseThrow(() -> new EntityNotFoundException("User with name " + username + " not found"));
        NoteCategory noteCategory = note.getNoteCategory();
        String transliterateName = transliterator.transliterate(note.getName());

        if (noteCategory == null) {
            if (noteCategoryService.existsByTransliterateNameAndUserId("temporary", user.getId())) {
                noteCategory = noteCategoryService.getCategoryByTransliterateNameAndUserId("temporary", user.getId());
            } else {
                noteCategory = new NoteCategory();
                noteCategory.setName("Temporary");
                noteCategory.setUser(user);

                noteCategoryService.createCategory(noteCategory);
            }
        }
        if (noteRepository.existsByTransliterateNameAndNoteCategoryId(transliterateName, noteCategory.getId())) {
            return -1L;
        }

        note.setTransliterateName(transliterateName);
        note.setCreateDate(LocalDateTime.now());
        note.setNoteCategory(noteCategory);
        noteRepository.save(note);

        return noteCategory.getId();
    }

    @Override
    @Transactional
    public String createNoteWithFiles(UserStatus userStatus, List<String> usersFiles, List<String> fileIdsForShow, InlineKeyboardMarkup keyboardMarkup) {
        if (userStatus.getCurrentStep().equals(UserStatus.ADD_FILE)) {
            Note note = noteRepository.getById(userStatus.getCurrentNoteId());

            note.getFileIds().addAll(usersFiles);
            userStatus.setCurrentStep(UserStatus.NOTHING);

            return "Files have been added.\n\n" + showNote(userStatus, note.getTransliterateName(), fileIdsForShow, keyboardMarkup);
        } else {
            Note note = new Note();

            note.setCreateDate(LocalDateTime.now());
            note.setUser(userService.getUserById(userStatus.getUserId()));
            note.setFileIds(usersFiles);
            noteRepository.save(note);

            userStatus.setCurrentNoteId(note.getId());
            userStatus.setCurrentStep(UserStatus.WAITING_CATEGORY_FOR_NOTE);

            return "Select the category in which you want to create a note:\n\n" +
                    noteCategoryService.showListOfCategories(userStatus, keyboardMarkup);
        }
    }

    @Override
    @Transactional
    public void updateNote(Note note) {
        String transliterateName = transliterator.transliterate(note.getName());
        Note existsNote = noteRepository.getByTransliterateNameAndNoteCategoryId(transliterateName, note.getNoteCategory().getId());

        if (existsNote != null && !Objects.equals(existsNote.getId(), note.getId())) {
            return;
        }

        note.setUpdateDate(LocalDateTime.now());
        note.setTransliterateName(transliterateName);

        noteRepository.save(note);
    }

    @Override
    @Transactional
    public void deleteNoteById(Long id) {
        Note noteToDelete = noteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Note with id: " + id + " not found"));

        noteRepository.delete(noteToDelete);
    }
}
