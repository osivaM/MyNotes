package com.freemyip.mynotesproject.MyNotes.repositories.content;

import com.freemyip.mynotesproject.MyNotes.models.content.NoteCategory;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface NoteCategoryRepository extends ListCrudRepository<NoteCategory, Long> {
    List<NoteCategory> findAllByUserUsername(String username);
    List<NoteCategory> getAllByUserId(Long userId);
    NoteCategory getById(Long id);
    Optional<NoteCategory> findByTransliterateNameAndUserId(String transliterateName, Long userId);
    boolean existsByTransliterateNameAndUserId(String transliterateName, Long userId);
    void deleteAllByUserId(Long userId);
}
