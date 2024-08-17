package com.freemyip.mynotesproject.MyNotes.models.content;

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
    private Long id;
    private String name;
    private String transliterateName;
    @Column(columnDefinition = "TEXT")
    private String content;
    private String link;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> fileIds;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private NoteCategory noteCategory;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
