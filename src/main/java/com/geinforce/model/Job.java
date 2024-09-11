package com.geinforce.model;

import lombok.*;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String jobName;
    private String proteinFile;
    private String ligandFiles ;
    private String outputFile;
    private String coordinates;
    private int cavities;
    private String email;
    private JobStatus status;
    private LocalDateTime submitDateTime;
    private String resultLink;


}
