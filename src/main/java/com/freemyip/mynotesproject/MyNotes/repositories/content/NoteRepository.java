package com.freemyip.mynotesproject.MyNotes.repositories.content;

import com.freemyip.mynotesproject.MyNotes.models.content.Note;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface NoteRepository extends ListCrudRepository<Note, Long> {
    List<Note> findAllByNoteCategoryId(Long categoryId);
    List<Note> getAllByNoteCategoryId(Long categoryId);
    Note getById(Long id);
    boolean existsByNameAndNoteCategoryId(String name, Long categoryId);
    boolean existsByTransliterateNameAndUserId(String transliterateName, Long userId);
    boolean existsByTransliterateNameAndNoteCategoryId(String transliterateName, Long categoryId);
    Note getByTransliterateNameAndNoteCategoryId(String transliterationName, Long categoryId);
    void deleteAllByNoteCategoryId(Long id);
    void deleteAllByUserId(Long id);
    long countAllByTransliterateNameAndUserId(String transliterateName, Long userId);
}
