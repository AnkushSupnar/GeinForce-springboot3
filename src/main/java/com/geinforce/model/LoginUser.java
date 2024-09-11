package com.geinforce.model;

import lombok.*;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
@Entity
@Table(name="loginUser")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class LoginUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long loginId;
    @Column(name="email")
    String email;
    String userType;
    Long userTypeId;
    String password;
    Boolean isActive;
    String otp;
    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    @JoinTable(name="loginUserRole",joinColumns = @JoinColumn(name="loginId"),inverseJoinColumns = @JoinColumn(name="roleId"))
    Set<Role> roles;

}
