package com.freemyip.mynotesproject.MyNotes.models.content;

import com.fasterxml.jackson.annotation.JsonView;
import com.freemyip.mynotesproject.MyNotes.models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(ForWatching.class)
    private Long id;

    @JsonView(ForWatching.class)
    private String type;

    @JsonView(ForWatching.class)
    private String name;

    private String transliterateName;

    @Column(columnDefinition = "TEXT")
    @JsonView(ForWatching.class)
    private String content;

    @JsonView(ForWatching.class)
    private String link;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> fileIds;

    @JsonView(ForWatching.class)
    private LocalDateTime createDate;

    private LocalDateTime updateDate;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private NoteCategory noteCategory;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public interface ForWatching {};
}
