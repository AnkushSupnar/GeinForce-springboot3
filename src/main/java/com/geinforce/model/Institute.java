package com.geinforce.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;



@Entity
@Table(name = "Institute")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Institute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long instituteId;
    @Column(unique = true)
    String instituteName;
    String instituteDepartment;
    @Column(unique = true)
    String instituteEmail;
    String instituteCode;
    String instituteMobile;
    String instituteCountry;
    String instituteState;
    String instituteCity;
    private Boolean verified;
    private String verificationMethod;
    private String otp;
}
