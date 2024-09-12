package com.freemyip.mynotesproject.MyNotes.models.content;

import com.fasterxml.jackson.annotation.JsonView;
import com.freemyip.mynotesproject.MyNotes.models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(ForWatching.class)
    private Long id;

    @JsonView(ForWatching.class)
    private String type;

    @JsonView(ForWatching.class)
    private String name;

    private String transliterateName;

    @JsonView(ForWatching.class)
    private LocalDateTime createDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public interface ForWatching {}
}
