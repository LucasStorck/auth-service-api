package com.lucas.JavaAuthenticator.repositories;

import com.lucas.JavaAuthenticator.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UseRepository extends JpaRepository<User, UUID> {
  Optional<User> findByUsername(String username);
}
