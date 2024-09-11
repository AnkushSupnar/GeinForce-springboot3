package com.geinforce.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.geinforce.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
//     @Query("select r from User r where r.email = ?1 and r.password = ?2")
//     Optional<User> findByEmailAndPassword(String email, String password);

     User findByEmail(String email);

    @Query("select u from User u WHERE u.email=:email and u.otp=:otp")
     User findNonVerifiedUserByEmail(String email,String otp);




}
