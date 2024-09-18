package com.freemyip.mynotesproject.MyNotes.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.freemyip.mynotesproject.MyNotes.exceptions.DuplicateEntityException;
import com.freemyip.mynotesproject.MyNotes.exceptions.EmptyNameException;
import com.freemyip.mynotesproject.MyNotes.models.content.CreateCategoryRequest;
import com.freemyip.mynotesproject.MyNotes.models.content.CreateNoteRequest;
import com.freemyip.mynotesproject.MyNotes.models.content.Note;
import com.freemyip.mynotesproject.MyNotes.models.content.NoteCategory;
import com.freemyip.mynotesproject.MyNotes.services.content.NoteCategoryService;
import com.freemyip.mynotesproject.MyNotes.services.content.NoteService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class ContentController {
    private final NoteCategoryService categoryService;
    private final NoteService noteService;

    @GetMapping("/categories")
    @JsonView(NoteCategory.ForWatching.class)
    public ResponseEntity<List<NoteCategory>> listOfCategories(@AuthenticationPrincipal UserDetails userDetails) {
        List<NoteCategory> categoryList = categoryService.getAllCategoriesForUserUsername(userDetails.getUsername())
                .stream()
                .peek(category -> category.setType("category"))
                .toList();

        return ResponseEntity.ok(categoryList);
    }

    @GetMapping("/category")
    @JsonView(Note.ForWatching.class)
    public ResponseEntity<List<Note>> category(@RequestParam("id") Long id) {
        List<Note> noteList = noteService.getAllByNoteCategoryId(id)
                .stream()
                .peek(note -> note.setType("note"))
                .toList();

        return ResponseEntity.ok(noteList);
    }

    @PostMapping("/create-category")
    @JsonView(NoteCategory.ForWatching.class)
    public ResponseEntity<?> createCategory(
            @RequestBody CreateCategoryRequest categoryRequest,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        NoteCategory newCategory;

        try {
            newCategory = categoryService.createCategory(categoryRequest.getName(), userDetails.getUsername());
        } catch (DuplicateEntityException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }

        return ResponseEntity.ok(newCategory);
    }

    @DeleteMapping("/delete-category")
    public ResponseEntity<HttpStatus> deleteCategory(@RequestParam("id") Long id) {
        categoryService.deleteCategoryById(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/create-note")
    @JsonView(Note.ForWatching.class)
    public ResponseEntity<?> createNote(
            @RequestBody CreateNoteRequest noteRequest,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Note newNote;
        try {
            newNote = noteService.createNote(noteRequest, userDetails);
        } catch(DuplicateEntityException | EmptyNameException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }

        return ResponseEntity.ok(newNote);
    }

    @PatchMapping("/edit-note")
    @JsonView(Note.ForWatching.class)
    public ResponseEntity<?> editNote(@RequestBody Note note) {
        Note newNote;

        try {
            newNote = noteService.updateNote(note, null);
        } catch (EntityNotFoundException | DuplicateEntityException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }

        return ResponseEntity.ok(newNote);
    }

    @DeleteMapping("/delete-note")
    public ResponseEntity<HttpStatus> deleteNote(@RequestParam("id") Long id) {
        noteService.deleteNoteById(id);

        return ResponseEntity.noContent().build();
    }
}
