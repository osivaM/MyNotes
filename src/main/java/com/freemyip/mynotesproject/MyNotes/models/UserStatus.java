package com.freemyip.mynotesproject.MyNotes.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserStatus {
    public static final String UNREGISTERED = "UNREGISTERED";
    public static final String REGISTERED = "REGISTERED";
    public static final String WAITING_USERNAME = "WAITING_USERNAME";
    public static final String WAITING_PASSWORD = "WAITING_PASSWORD";
    public static final String WAITING_CATEGORY_NAME = "WAITING_CATEGORY_NAME";
    public static final String UPDATING_CATEGORY = "UPDATING_CATEGORY";
    public static final String DELETING_CATEGORY = "DELETING_CATEGORY";
    public static final String WAITING_CATEGORY_FOR_NOTE = "WAITING_CATEGORY_FOR_NOTE";
    public static final String CREATING_CATEGORY_FOR_NOTE = "CREATING_CATEGORY_FOR_NOTE";
    public static final String WAITING_NOTE_NAME = "WAITING_NOTE_NAME";
    public static final String WAITING_NOTE_LINK = "WAITING_NOTE_LINK";
    public static final String WAITING_NOTE_CONTENT = "WAITING_NOTE_CONTENT";
    public static final String ADD_FILE = "ADD_FILE";
    public static final String MOVING_NOTE = "MOVING_NOTE";
    public static final String DELETING_NOTE = "DELETING_NOTE";
    public static final String NOTHING = "NOTHING";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long telegramUserId;
    private String username;
    private String userRegistrationStatus;
    private String currentStep;
    private Long currentCategoryId;
    private Long currentNoteId;
}
