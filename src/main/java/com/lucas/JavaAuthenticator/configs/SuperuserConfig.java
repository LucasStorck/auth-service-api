package com.lucas.JavaAuthenticator.configs;

import com.lucas.JavaAuthenticator.entities.Role;
import com.lucas.JavaAuthenticator.entities.User;
import com.lucas.JavaAuthenticator.repositories.RoleRepository;
import com.lucas.JavaAuthenticator.repositories.UseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Configuration
public class SuperuserConfig implements CommandLineRunner {

  private final RoleRepository roleRepository;

  private final UseRepository useRepository;

  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  public SuperuserConfig(RoleRepository roleRepository, UseRepository useRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
    this.roleRepository = roleRepository;
    this.useRepository = useRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    var roleSuperuser = roleRepository.findByName(Role.Values.SUPERUSER.name());

    var userAdmin = useRepository.findByUsername("superuser");

    userAdmin.ifPresentOrElse(
            (user) -> {
              System.out.println("THE SUPERUSER ALREADY EXISTS");
            },
            () -> {
              var user = new User();
              user.setUsername("superuser");
              user.setEmail("superuser@email.com");
              user.setPassword(bCryptPasswordEncoder.encode("123"));
              user.setRoles(Set.of(roleSuperuser));
              useRepository.save(user);
            }
    );
  }
}
