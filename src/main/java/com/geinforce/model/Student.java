package com.geinforce.model;

import jakarta.persistence.*;
import lombok.*;



@Entity
@Table(name="Student")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studentId;
    private String firstName;
    private String lastName ;
    @Column(unique = true)
    private String email;
    private String countryCode;
    private String mobile;
    @ManyToOne
    @JoinColumn(name="instituteId")
    private Institute institute;
    private String purpose;
    private String department;
    private String country;
    private String state;
    private String city;
    private String otp;
    private Boolean verified;
    private String verificationMethod;

}
