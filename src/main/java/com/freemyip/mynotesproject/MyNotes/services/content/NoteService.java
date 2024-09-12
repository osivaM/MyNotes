package com.freemyip.mynotesproject.MyNotes.services.content;

import com.freemyip.mynotesproject.MyNotes.models.User;
import com.freemyip.mynotesproject.MyNotes.models.UserStatus;
import com.freemyip.mynotesproject.MyNotes.models.content.CreateNoteRequest;
import com.freemyip.mynotesproject.MyNotes.models.content.Note;
import org.springframework.security.core.userdetails.UserDetails;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

public interface NoteService {
    List<Note> getAllByNoteCategoryId(Long categoryId);
    Note getNoteById(Long id);
    boolean existsByTransliterateNameAndUserId(String transliterateName, long userId);
    boolean existsByTransliterateNameAndCategoryId(String transliterateName, long categoryId);
    String showListOfNotes(UserStatus userStatus, InlineKeyboardMarkup keyboardMarkup);
    String showNote(UserStatus userStatus, String transliterateName, List<String> filesId, InlineKeyboardMarkup keyboardMarkup);
    String createNote(Update update, UserStatus userStatus, List<String> filesId, InlineKeyboardMarkup keyboardMarkup);
    Note createNote(CreateNoteRequest noteRequest, UserDetails userDetails);
    String createNoteWithFiles(UserStatus userStatus, List<String> usersFiles, List<String> fileIdsForShow, InlineKeyboardMarkup keyboardMarkup);
    void updateNote(Note note);
    Note updateNote(Note editedNote, Long categoryId);
    void deleteNoteById(Long id);
}
