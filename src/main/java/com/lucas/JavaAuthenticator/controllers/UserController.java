package com.lucas.JavaAuthenticator.controllers;

import com.lucas.JavaAuthenticator.dtos.CreateUserDto;
import com.lucas.JavaAuthenticator.entities.Role;
import com.lucas.JavaAuthenticator.entities.User;
import com.lucas.JavaAuthenticator.repositories.RoleRepository;
import com.lucas.JavaAuthenticator.repositories.UseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.processing.Generated;
import java.util.List;
import java.util.Set;

@RestController
public class UserController {

  @Autowired
  private UseRepository useRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private BCryptPasswordEncoder bCryptPasswordEncoder;

  @PostMapping("/api/user")
  @Transactional
  public ResponseEntity<Void> postUsers(@RequestBody CreateUserDto createUserDto) {
    var user = roleRepository.findByName(Role.Values.USER.name());
    var userRepository = useRepository.findByUsername(createUserDto.username());

    if (userRepository.isPresent()) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    if (user == null) {
      user = new Role();
      user.setName(Role.Values.USER.name());
      roleRepository.save(user);
    }

    var newUser = new User();

    newUser.setUsername(createUserDto.username());
    newUser.setEmail(createUserDto.email());
    newUser.setPassword(bCryptPasswordEncoder.encode(createUserDto.password()));
    newUser.setRoles(Set.of(user));

    useRepository.save(newUser);

    return ResponseEntity.ok().build();
  }

  @GetMapping("/api/user")
  @PreAuthorize("hasAuthority('SCOPE_SUPERUSER')")
  public ResponseEntity<List<User>> listUsers() {
    var users = useRepository.findAll();
    return ResponseEntity.ok(users);
  }

  @PutMapping("/api/user/{username}")
  @PreAuthorize("hasAuthority('SCOPE_SUPERUSER') or hasAuthority('SCOPE_USER')")
  @Transactional
  public ResponseEntity<Void> updateUser(@PathVariable String username, @RequestBody CreateUserDto createUserDto, JwtAuthenticationToken token) {
    var user = useRepository.findByUsername(username);

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
      var existingUser = useRepository.findByUsername(createUserDto.username());
      if (existingUser.isPresent()) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "THIS USERNAME ALREADY EXIST");
      }
    }

    useRepository.save(updateUser);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/api/user/{username}")
  @PreAuthorize("hasAuthority('SCOPE_SUPERUSER') or hasAuthority('SCOPE_USER')")
  @Transactional
  public ResponseEntity<Void> deleteUser(@PathVariable String username) {
    var user = useRepository.findByUsername(username);
    var authorization = useRepository.findByUsername(username);

    if (user.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    useRepository.delete(user.get());

    return ResponseEntity.noContent().build();
  }
}
