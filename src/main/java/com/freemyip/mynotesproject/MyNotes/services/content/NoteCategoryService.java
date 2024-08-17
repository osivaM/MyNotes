package com.freemyip.mynotesproject.MyNotes.services.content;

import com.freemyip.mynotesproject.MyNotes.models.UserStatus;
import com.freemyip.mynotesproject.MyNotes.models.content.NoteCategory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

public interface NoteCategoryService {
    List<NoteCategory> getAllCategoriesForUser(String username);
    NoteCategory getCategoryById(Long id);
    NoteCategory getCategoryByTransliterateNameAndUserId(String transliterateName, long userId);
    boolean existsByTransliterateNameAndUserId(String transliterateName, long userId);
    String showListOfCategories(UserStatus userStatus, InlineKeyboardMarkup keyboardMarkup);
    String createCategory(Update update, UserStatus userStatus);
    boolean createCategory(NoteCategory noteCategory);
    String updateCategory(Update update, UserStatus userStatus, InlineKeyboardMarkup keyboardMarkup);
    void updateCategory(NoteCategory noteCategory);
    void deleteCategoryById(Long id);
}
