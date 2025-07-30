package com.lucas.Auth.controllers;

import com.lucas.Auth.dtos.CreateUserDto;
import com.lucas.Auth.entities.Role;
import com.lucas.Auth.entities.RoleType;
import com.lucas.Auth.entities.User;
import com.lucas.Auth.repositories.RoleRepository;
import com.lucas.Auth.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
public class UserController {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  public UserController(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  @Operation(
          summary = "Create a new user",
          description = "This method is responsible for creating new users by providing a username, email, and password."
  )
  @PostMapping("/api/user")
  @Transactional
  public ResponseEntity<Void> createUser(@RequestBody CreateUserDto createUserDto) {
    var user = roleRepository.findByName(RoleType.USER.name());
    var userRepository = this.userRepository.findByUsername(createUserDto.username());

    if (userRepository.isPresent()) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    if (user == null) {
      user = new Role();
      user.setName(RoleType.USER.name());
      roleRepository.save(user);
    }

    var newUser = new User();

    newUser.setUsername(createUserDto.username());
    newUser.setEmail(createUserDto.email());
    newUser.setPassword(bCryptPasswordEncoder.encode(createUserDto.password()));
    newUser.setRoles(Set.of(user));

    this.userRepository.save(newUser);

    return ResponseEntity.ok().build();
  }

  @Operation(
          summary = "List all users",
          description = "This method returns a list of all users in the system. Only users with the 'SUPERUSER' authority can access this method."
  )
  @GetMapping("/api/user")
  @PreAuthorize("hasAuthority('SCOPE_SUPERUSER')")
  public ResponseEntity<List<User>> getUser() {
    var users = userRepository.findAll();
    return ResponseEntity.ok(users);
  }

  @Operation(
          summary = "Get a user by their username",
          description = "This method returns a user selected by their username. Only users with the 'SUPERUSER' authority can access this method."
  )
  @GetMapping("/api/user/{username}")
  @PreAuthorize("hasAuthority('SCOPE_SUPERUSER')")
  public Optional<User> getUserByUsername(@PathVariable String username) {
    return userRepository.findByUsername(username);
  }

  @Operation(
          summary = "Update a user by their username",
          description = "This method is responsible for updating a user's information by the provided username. Both 'SUPERUSER' and 'USER' roles can access this method."
  )
  @PutMapping("/api/user/{username}")
  @PreAuthorize("hasAuthority('SCOPE_SUPERUSER') or hasAuthority('SCOPE_USER')")
  @Transactional
  public ResponseEntity<Void> updateUser(@PathVariable String username, @RequestBody CreateUserDto createUserDto, JwtAuthenticationToken token) {
    var user = userRepository.findByUsername(username);

    if (user.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    var updateUser = user.get();
    updateUser.setUsername(createUserDto.username());
    updateUser.setEmail(createUserDto.email());

    if (createUserDto.password() != null && !createUserDto.password().isEmpty()) {
      updateUser.setPassword(bCryptPasswordEncoder.encode(createUserDto.password()));
    }

    if (!updateUser.getUsername().equals(createUserDto.username())) {
      var existingUser = userRepository.findByUsername(createUserDto.username());
      if (existingUser.isPresent()) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "THIS USERNAME ALREADY EXIST");
      }
    }

    userRepository.save(updateUser);
    return ResponseEntity.ok().build();
  }

  @Operation(
          summary = "Delete a user by their username",
          description = "This method is responsible for deleting a user by their username. Both 'SUPERUSER' and 'USER' roles can access this method."
  )
  @DeleteMapping("/api/user/{username}")
  @PreAuthorize("hasAuthority('SCOPE_SUPERUSER') or hasAuthority('SCOPE_USER')")
  @Transactional
  public ResponseEntity<Void> deleteUser(@PathVariable String username) {
    var user = userRepository.findByUsername(username);
    var authorization = userRepository.findByUsername(username);

    if (user.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    userRepository.delete(user.get());

    return ResponseEntity.noContent().build();
  }
}
