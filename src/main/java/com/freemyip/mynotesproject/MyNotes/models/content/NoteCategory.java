package com.freemyip.mynotesproject.MyNotes.models.content;

import com.freemyip.mynotesproject.MyNotes.models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NoteCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String transliterateName;
    private LocalDateTime createDate;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
