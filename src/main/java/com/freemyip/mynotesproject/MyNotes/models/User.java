package com.freemyip.mynotesproject.MyNotes.models;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "username"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @JsonView({AdminViews.class, UserViews.class})
    private Long id;

    @JsonView(AdminViews.class)
    private Long telegramId;

    @Column(unique = true)
    @JsonView({AdminViews.class, UserViews.class})
    private String username;

    @JsonView(UserViews.class)
    private String password;

    @JsonView({AdminViews.class, UserViews.class})
    private String firstName;

    @JsonView({AdminViews.class, UserViews.class})
    private String lastName;

    @JsonView(AdminViews.class)
    private LocalDateTime registrationDate;

    @Enumerated(EnumType.STRING)
    @JsonView(AdminViews.class)
    private Role role;

    @JsonView(AdminViews.class)
    private boolean notDeletion;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public interface UserViews {}
    public interface AdminViews {}
}


