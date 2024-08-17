package com.freemyip.mynotesproject.MyNotes.controllers.user;

import com.freemyip.mynotesproject.MyNotes.models.User;
import com.freemyip.mynotesproject.MyNotes.models.content.Note;
import com.freemyip.mynotesproject.MyNotes.models.content.NoteCategory;
import com.freemyip.mynotesproject.MyNotes.services.UserService;
import com.freemyip.mynotesproject.MyNotes.services.content.NoteCategoryService;
import com.freemyip.mynotesproject.MyNotes.services.content.NoteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Controller
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {
    private UserService userService;
    private NoteCategoryService noteCategoryService;
    private NoteService noteService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userService.getUserByUsername(username);
        List<NoteCategory> categories = noteCategoryService.getAllCategoriesForUser(username);

        model.addAttribute("user", user);
        model.addAttribute("categories", categories);
        model.addAttribute("noteCategory", new NoteCategory());

        return "/user/dashboard";
    }

    @PatchMapping("/updateUser")
    public String updateUser(@ModelAttribute User user, HttpServletRequest httpServletRequest) {
        userService.updateUser(user);

        HttpSession httpSession = httpServletRequest.getSession(false);

        if (httpSession != null) {
            httpSession.invalidate();
        }

        return "redirect:/login";
    }

    @PostMapping("/createCategory")
    public String createCategory(@ModelAttribute("noteCategory") NoteCategory noteCategory,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 BindingResult bindingResult, Model model)
    {
        String username = userDetails.getUsername();
        User user = userService.getUserByUsername(username);
        List<NoteCategory> categories;

        noteCategory.setUser(user);
        if (!noteCategoryService.createCategory(noteCategory)) {
            categories = noteCategoryService.getAllCategoriesForUser(username);

            bindingResult.rejectValue("name", "error.noteCategory", "Category with this name already exists.");
            model.addAttribute("user", user);
            model.addAttribute("categories", categories);
            model.addAttribute("noteCategory", noteCategory);

            return "user/dashboard";
        }

        return "redirect:/user/dashboard";
    }

    @DeleteMapping("/deleteCategory/{id}")
    public String deleteCategory(@PathVariable("id") Long id) {
        noteCategoryService.deleteCategoryById(id);

        return "redirect:/user/dashboard";
    }

    @GetMapping("/category/{id}")
    public String category(@PathVariable("id") Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        NoteCategory noteCategory = noteCategoryService.getCategoryById(id);
        User authorizeUser = userService.getUserByUsername(userDetails.getUsername());
        User userFromCategory = noteCategory.getUser();

        if (!authorizeUser.equals(userFromCategory)) {
            return "redirect:/user/dashboard";
        }

        List<Note> notes = noteService.getAllNotesForCategoryFromUser(noteCategory.getName(), authorizeUser);
        Function<LocalDateTime, String> formatDateMethodWrapper = this::formatDateTime;

        model.addAttribute("category", noteCategory);
        model.addAttribute("notes", notes);
        model.addAttribute("formatDateMethod", formatDateMethodWrapper);

        return "/user/category";
    }

    public String formatDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy"));
    }

    @PatchMapping("/updateCategory")
    public String updateCategory(@ModelAttribute NoteCategory noteCategory) {
        noteCategoryService.updateCategory(noteCategory);

        return "redirect:/user/category/" + noteCategory.getId();
    }

    @GetMapping("/note/{id}")
    public String note(@PathVariable("id") Long id,
                       @AuthenticationPrincipal UserDetails userDetails,
                       Model model) {
        Note note = noteService.getNoteById(id);
        User authorizeUser = userService.getUserByUsername(userDetails.getUsername());
        User userFromNote = note.getUser();

        if (!authorizeUser.equals(userFromNote)) {
            return "redirect:/user/dashboard";
        }
        
        List<NoteCategory> categories = noteCategoryService.getAllCategoriesForUser(userDetails.getUsername());

        model.addAttribute("note", note);
        model.addAttribute("categories", categories);

        return "/user/note";
    }

    @GetMapping("/createNote")
    public String createNote(Model model,
                             @AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam(name = "categoryId", required = false) Long categoryId)
    {
        List<NoteCategory> categories;

        if (categoryId == null) {
            categories = noteCategoryService.getAllCategoriesForUser(userDetails.getUsername());
        } else {
            categories = Collections.singletonList(noteCategoryService.getCategoryById(categoryId));
        }

        model.addAttribute("note", new Note());
        model.addAttribute("categories", categories);

        return "/user/create_note";
    }

    @PostMapping("/createNote")
    public String createNote(@ModelAttribute("note") Note note, @AuthenticationPrincipal UserDetails userDetails,
                             BindingResult bindingResult, Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername());

        note.setUser(user);
        Long categoryId = noteService.createNote(note);

        if (categoryId == -1) {
            List<NoteCategory> categories = Collections.singletonList(noteCategoryService.getCategoryById(note.getNoteCategory().getId()));

            bindingResult.rejectValue("name", "error.note", "Note with this name already exists.");
            model.addAttribute("note", note);
            model.addAttribute("categories", categories);

            return "/user/create_note";
        }

        return "redirect:/user/category/" + categoryId;
    }

    @PatchMapping("/updateNote")
    public String updateNote(@ModelAttribute Note note,
                             @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsername(userDetails.getUsername());

        note.setUser(user);
        noteService.updateNote(note);

        return "redirect:/user/note/" + note.getId();
    }

    @DeleteMapping("/deleteNote/{id}")
    public String deleteNote(@PathVariable("id") Long id, HttpServletRequest httpServletRequest) {
        String referer = httpServletRequest.getHeader("referer");

        noteService.deleteNoteById(id);

        return "redirect:" + referer;
    }
}
