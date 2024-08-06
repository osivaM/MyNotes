package com.freemyip.myvisitcard.MyNotes.repositories;

import com.freemyip.myvisitcard.MyNotes.models.Role;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface RoleRepository extends ListCrudRepository<Role, Long> {
    Optional<Role> findRoleByName(String name);
}
