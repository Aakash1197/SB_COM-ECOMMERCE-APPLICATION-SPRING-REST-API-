package com.ecommerce.project.repository;

import com.ecommerce.project.entity.AppRole;
import com.ecommerce.project.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(AppRole appRole);

    Role findByRoleId(Long roleId);
}
