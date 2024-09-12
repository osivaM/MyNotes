package com.freemyip.mynotesproject.MyNotes.models.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNoteRequest {
    private String name;
    private String link;
    private String content;
    private Long categoryId;
}
