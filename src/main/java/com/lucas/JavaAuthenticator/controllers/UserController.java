package com.lucas.JavaAuthenticator.controllers;

import com.lucas.JavaAuthenticator.dtos.CreateUserDto;
import com.lucas.JavaAuthenticator.entities.Role;
import com.lucas.JavaAuthenticator.entities.User;
import com.lucas.JavaAuthenticator.repositories.RoleRepository;
import com.lucas.JavaAuthenticator.repositories.UseRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
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

  private final UseRepository useRepository;
  private final RoleRepository roleRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  public UserController(UseRepository useRepository, RoleRepository roleRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
    this.useRepository = useRepository;
    this.roleRepository = roleRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  @Operation(summary = "Criar novo usuário",
          description = "Esse método é responsável por criar novos usuários, informando o username, email e senha.")
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

  @Operation(summary = "Listar todos os usuários",
          description = "Esse método retorna todos os usuários cadastrados no sistema. Apenas usuários com autoridade 'SUPERUSER' podem acessar esse método.")
  @GetMapping("/api/user")
  @PreAuthorize("hasAuthority('SCOPE_SUPERUSER')")
  public ResponseEntity<List<User>> listUsers() {
    var users = useRepository.findAll();
    return ResponseEntity.ok(users);
  }

  @Operation(summary = "Recuperar usuários pelo seu username",
          description = "Esse método retorna apenas um usuário escolhido pelo seu username. Apenas usuários com autoridade 'SUPERUSER' podem acessar esse método.")
  @GetMapping("/api/user/{username}")
  @PreAuthorize("hasAuthority('SCOPE_SUPERUSER')")
  public Optional<User> listUserByUsername(@PathVariable String username) {
    return useRepository.findByUsername(username);
  }

  @Operation(summary = "Atualizar usuário pelo seu username",
          description = "Esse método é responsável por atualizar as informações do usuário pelo username fornecido. Tanto usuários 'SUPERUSER' e 'USER' podem acessar esse método.")
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

  @Operation(summary = "Deletar usuário pelo seu username",
          description = "Esse método é responsável por deletar os usuários pelo seu username. Tanto usuários 'SUPERUSER' e 'USER' podem acessar esse método.")
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
