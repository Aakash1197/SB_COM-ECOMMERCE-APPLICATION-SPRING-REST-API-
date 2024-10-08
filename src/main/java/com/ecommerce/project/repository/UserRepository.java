package com.ecommerce.project.repository;

import com.ecommerce.project.entity.AppRole;
import com.ecommerce.project.entity.Role;
import com.ecommerce.project.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Boolean existsByUsername( String username);

    Boolean existsByEmail( String email);

    @Query("select u from User u where upper(u.email) = upper(?1)")
    Optional<User> findUserByEmailIdIgnoreCase(String emailId);
}
