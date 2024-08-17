package com.freemyip.mynotesproject.MyNotes.services.impl;

import com.freemyip.mynotesproject.MyNotes.models.Role;
import com.freemyip.mynotesproject.MyNotes.repositories.RoleRepository;
import com.freemyip.mynotesproject.MyNotes.services.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {
    private RoleRepository roleRepository;

    @Override
    @Transactional
    public Role getRoleByName(String name) {
        Optional<Role> role = roleRepository.findRoleByName(name);

        if (role.isPresent()) {
            return role.get();
        } else {
            Role newRole = new Role();

            newRole.setName(name);

            roleRepository.save(newRole);

            return newRole;
        }
    }
}
