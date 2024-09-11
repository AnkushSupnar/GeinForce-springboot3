package com.geinforce.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="User")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String firstName;
    private String lastName ;
    @Column(unique = true)
    private String email;
    private String countryCode;
    private String mobile;
    private String institute;
    private String purpose;
    private String department;
    private String country;
    private String state;
    private String city;
    private String otp;
    private Boolean verified;
    private String verificationMethod;
    private Boolean isStudent;
}