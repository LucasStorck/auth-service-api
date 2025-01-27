package com.lucas.JavaAuthenticator.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@AllArgsConstructor
@NoArgsConstructor
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @Column(name = "role_id")
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public enum Values {
    SUPERUSER(2L),
    USER(1L);

    final long roleId;

    Values(long roleId) {
      this.roleId = roleId;
    }
  }
}
