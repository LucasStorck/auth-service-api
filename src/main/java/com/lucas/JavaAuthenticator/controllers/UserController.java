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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
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

    var newUser = new User();

    newUser.setUsername(createUserDto.username());
    newUser.setEmail(createUserDto.email());
    newUser.setPassword(bCryptPasswordEncoder.encode(createUserDto.password()));
    newUser.setRoles(Set.of(user));

    useRepository.save(newUser);

    return ResponseEntity.ok().build();
  }

  @GetMapping("/api/users")
  @PreAuthorize("hasAuthority('SCOPE_SUPERUSER')")
  public ResponseEntity<List<User>> getUsers() {
    var users = useRepository.findAll();
    return ResponseEntity.ok(users);
  }
}
