package com.lucas.JavaAuthenticator.controllers;

import com.lucas.JavaAuthenticator.dtos.LoginRequestDto;
import com.lucas.JavaAuthenticator.dtos.LoginResponseDto;
import com.lucas.JavaAuthenticator.entities.Role;
import com.lucas.JavaAuthenticator.repositories.UseRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.stream.Collectors;

@RestController
public class TokenController {

  @Autowired
  private JwtEncoder jwtEncoder;

  @Autowired
  private UseRepository useRepository;

  @Autowired
  private BCryptPasswordEncoder bCryptPasswordEncoder;

  @Operation(summary = "Login dos usuários",
          description = "Esse método é responsável por realizar o login dos usuários via username, email e senha")
  @PostMapping("/api/login")
  public ResponseEntity<LoginResponseDto> loginResponse(@RequestBody LoginRequestDto loginRequest) {
    var userRepository = useRepository.findByUsername(loginRequest.username());

    if (userRepository.isEmpty() || userRepository.get().isLoginIncorrect(loginRequest, bCryptPasswordEncoder)) {
      throw new BadCredentialsException("USER, EMAIL OR PASSWORD IS INCORRECT");
    }

    var now = Instant.now();
    var expiresIn = 300L;

    var scopes = userRepository.get().getRoles()
            .stream()
            .map(Role::getName)
            .collect(Collectors.joining(" "));

    var claims = JwtClaimsSet.builder()
            .issuer("my-backend")
            .subject(userRepository.get().getId().toString())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expiresIn))
            .claim("scope", scopes)
            .build();
    var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

    return ResponseEntity.ok(new LoginResponseDto(jwtValue, expiresIn));
  }
}
