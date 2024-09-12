package com.freemyip.mynotesproject.MyNotes.services.impl.content;

import com.freemyip.mynotesproject.MyNotes.exceptions.DuplicateEntityException;
import com.freemyip.mynotesproject.MyNotes.exceptions.EmptyNameException;
import com.freemyip.mynotesproject.MyNotes.repositories.UserRepository;
import com.freemyip.mynotesproject.MyNotes.services.content.NoteCategoryService;
import com.freemyip.mynotesproject.MyNotes.models.User;
import com.freemyip.mynotesproject.MyNotes.models.UserStatus;
import com.freemyip.mynotesproject.MyNotes.models.content.NoteCategory;
import com.freemyip.mynotesproject.MyNotes.repositories.content.NoteCategoryRepository;
import com.freemyip.mynotesproject.MyNotes.repositories.content.NoteRepository;
import com.freemyip.mynotesproject.MyNotes.util.Transliterator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteCategoryServiceImpl implements NoteCategoryService {
    private final NoteCategoryRepository categoryRepository;
    private final NoteRepository noteRepository;
    private final Transliterator transliterator;
    private final UserRepository userRepository;

    @Override
    public List<NoteCategory> getAllCategoriesForUserUsername(String username) {
        return categoryRepository.findAllByUserUsername(username);
    }

    @Override
    public NoteCategory getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category with id: " + id + " not found"));
    }

    @Override
    public NoteCategory getCategoryByTransliterateNameAndUserId(String transliterateName, long userId) {
        return categoryRepository.findByTransliterateNameAndUserId(transliterateName, userId)
                .orElseThrow(() -> new EntityNotFoundException("Category with name " + transliterateName + " not found."));
    }

    @Override
    public boolean existsByTransliterateNameAndUserId(String transliterationCategoryName, long userId) {
        return categoryRepository.existsByTransliterateNameAndUserId(transliterationCategoryName, userId);
    }

    @Override
    public String showListOfCategories(UserStatus userStatus, InlineKeyboardMarkup keyboardMarkup) {
        StringBuilder response = new StringBuilder();
        List<NoteCategory> categoryList = categoryRepository.getAllByUserId(userStatus.getUserId());

        keyboardMarkup.setKeyboard(Collections.singletonList(
                Collections.singletonList(
                        InlineKeyboardButton.builder().text("Create category").callbackData("createCategory").build()
                )
        ));
        if (categoryList.isEmpty()) {
            response.append("List of categories is empty. Create a category.");
        } else {
            for (NoteCategory noteCategory : categoryList) {
                response.append(String.format("/%s -> %s\n", noteCategory.getTransliterateName(), noteCategory.getName()));
            }
        }

        return response.toString();
    }

    @Override
    @Transactional
    public NoteCategory createCategory(String name, String username) {
        if (name == null) {
            throw new EmptyNameException("The name cannot be empty");
        }
        if (categoryRepository.existsByNameAndUserUsername(name, username)) {
            throw new DuplicateEntityException("Category with name " + name + " already exists");
        }

        NoteCategory newCategory = new NoteCategory();

        newCategory.setType("category");
        newCategory.setName(name);
        newCategory.setTransliterateName(transliterator.transliterate(name));
        newCategory.setCreateDate(LocalDateTime.now());
        userRepository.findUserByUsername(username).ifPresent(newCategory::setUser);

        categoryRepository.save(newCategory);

        return newCategory;
    }

    @Override
    @Transactional
    public String createCategory(Update update, UserStatus userStatus) {
        String name = update.getMessage().getText();
        String transliterateName = transliterator.transliterate(name);
        long userId = userStatus.getUserId();

        userStatus.setCurrentCategoryId(null);

        if (categoryRepository.existsByTransliterateNameAndUserId(transliterateName, userId)) {
            return "A category with that name already exists. Choose a different name:";
        }

        NoteCategory newNoteCategory = new NoteCategory();
        User user = userRepository.findUserById(userId)
                .orElseThrow();

        newNoteCategory.setName(name);
        newNoteCategory.setTransliterateName(transliterateName);
        newNoteCategory.setCreateDate(LocalDateTime.now());
        newNoteCategory.setUser(user);

        categoryRepository.save(newNoteCategory);

        userStatus.setCurrentStep(UserStatus.NOTHING);
        categoryRepository.findByTransliterateNameAndUserId(transliterateName, userId)
                .ifPresent(category -> userStatus.setCurrentCategoryId(category.getId()));

        return "The " + name + " category has been created.";
    }

    @Override
    @Transactional
    public boolean createCategory(NoteCategory noteCategory) {
        String transliterationName = transliterator.transliterate(noteCategory.getName());

        if (categoryRepository.existsByTransliterateNameAndUserId(transliterationName, noteCategory.getUser().getId())) {
            return false;
        }

        noteCategory.setCreateDate(LocalDateTime.now());
        noteCategory.setTransliterateName(transliterationName);

        categoryRepository.save(noteCategory);

        return true;
    }

    @Override
    @Transactional
    public String updateCategory(Update update, UserStatus userStatus, InlineKeyboardMarkup keyboardMarkup) {
        String response;

        if (userStatus.getCurrentCategoryId() == null) {
            response = "The category has not been selected for modification. Select a category from the list:" +
                    showListOfCategories(userStatus, keyboardMarkup);
        } else {
            if (update.hasMessage()) {
                String name = update.getMessage().getText();
                String transliterateName = transliterator.transliterate(name);

                if (categoryRepository.existsByTransliterateNameAndUserId(transliterateName, userStatus.getUserId())) {
                    keyboardMarkup.setKeyboard(new ArrayList<>());
                    response = "A category with that name already exists. Choose a different name:";
                } else {
                    NoteCategory noteCategory = categoryRepository.getById(userStatus.getCurrentCategoryId());

                    noteCategory.setName(name);
                    noteCategory.setTransliterateName(transliterateName);
                    categoryRepository.save(noteCategory);
                    userStatus.setCurrentStep(UserStatus.NOTHING);

                    response = "";
                }
            } else {
                keyboardMarkup.setKeyboard(new ArrayList<>());
                response = "Enter a new name for the category:";
            }
        }

        return response;
    }

    @Override
    @Transactional
    public void updateCategory(NoteCategory noteCategory) {
        String transliterateName = transliterator.transliterate(noteCategory.getName());
        NoteCategory categoryToUpdate = categoryRepository.findById(noteCategory.getId())
                        .orElseThrow(() -> new EntityNotFoundException("Category with id: " + noteCategory.getId() + " not found"));

        if (categoryRepository.existsByTransliterateNameAndUserId(transliterateName, categoryToUpdate.getUser().getId())) {
            return;
        }

        categoryToUpdate.setName(noteCategory.getName());
        categoryToUpdate.setTransliterateName(transliterateName);

        categoryRepository.save(categoryToUpdate);
    }

    @Override
    @Transactional
    public void deleteCategoryById(Long id) {
        NoteCategory categoryToDelete =
                categoryRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Category with id: " + id + " not found"));

        noteRepository.deleteAllByNoteCategoryId(categoryToDelete.getId());
        categoryRepository.delete(categoryToDelete);
    }
}
