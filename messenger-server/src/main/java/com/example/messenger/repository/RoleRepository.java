package com.example.messenger.repository;

import com.example.messenger.entity.RoleEntity;
import com.example.messenger.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByName(Role name);
}
