package com.freemyip.myvisitcard.MyNotes.repositories.content;

import com.freemyip.myvisitcard.MyNotes.models.User;
import com.freemyip.myvisitcard.MyNotes.models.content.Note;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface NoteRepository extends ListCrudRepository<Note, Long> {
    List<Note> findAllByNoteCategoryNameAndUser(String name, User user);
    List<Note> getAllByNoteCategoryId(Long categoryId);
    Note getById(Long id);
    boolean existsByTransliterateNameAndUserId(String transliterateName, Long userId);
    boolean existsByTransliterateNameAndNoteCategoryId(String transliterateName, Long categoryId);
    Note getByTransliterateNameAndNoteCategoryId(String transliterationName, Long categoryId);
    void deleteAllByNoteCategoryId(Long id);
    void deleteAllByUserId(Long id);
    long countAllByTransliterateNameAndUserId(String transliterateName, Long userId);
}
