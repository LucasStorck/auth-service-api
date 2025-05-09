package com.lucas.JavaAuthenticator.controllers;

import com.lucas.JavaAuthenticator.dtos.LoginRequestDto;
import com.lucas.JavaAuthenticator.dtos.LoginResponseDto;
import com.lucas.JavaAuthenticator.entities.Role;
import com.lucas.JavaAuthenticator.repositories.UseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

  private final JwtEncoder jwtEncoder;
  private final UseRepository useRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  public TokenController(JwtEncoder jwtEncoder, UseRepository useRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
    this.jwtEncoder = jwtEncoder;
    this.useRepository = useRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  @Operation(
          summary = "User Login",
          description = "This method is responsible for authenticating users via their username, email, and password."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Successful login, returns JWT token."),
          @ApiResponse(responseCode = "401", description = "Invalid username, email, or password."),
          @ApiResponse(responseCode = "400", description = "Bad request.")
  })
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
