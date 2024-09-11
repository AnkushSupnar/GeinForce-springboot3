package com.geinforce.model;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long roleId;

    @Column(name = "roleName")
    String roleName;
    
    public Role(String roleName) {
    	this.roleName = roleName;
    }
}
