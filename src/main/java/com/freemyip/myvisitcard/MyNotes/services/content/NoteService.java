package com.freemyip.myvisitcard.MyNotes.services.content;

import com.freemyip.myvisitcard.MyNotes.models.User;
import com.freemyip.myvisitcard.MyNotes.models.UserStatus;
import com.freemyip.myvisitcard.MyNotes.models.content.Note;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

public interface NoteService {
    List<Note> getAllNotesForCategoryFromUser(String name, User user);
    Note getNoteById(Long id);
    boolean existsByTransliterateNameAndUserId(String transliterateName, long userId);
    boolean existsByTransliterateNameAndCategoryId(String transliterateName, long categoryId);
    String showListOfNotes(UserStatus userStatus, InlineKeyboardMarkup keyboardMarkup);
    String showNote(UserStatus userStatus, String transliterateName, List<String> filesId, InlineKeyboardMarkup keyboardMarkup);
    String createNote(Update update, UserStatus userStatus, List<String> filesId, InlineKeyboardMarkup keyboardMarkup);
    Long createNote(Note note);
    String createNoteWithFiles(UserStatus userStatus, List<String> usersFiles, List<String> fileIdsForShow, InlineKeyboardMarkup keyboardMarkup);
    void updateNote(Note note);
    void deleteNoteById(Long id);
}
