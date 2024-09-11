package com.geinforce.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.geinforce.model.LoginUser;



@Repository
public interface LoginUserRepository extends JpaRepository<LoginUser, Long> {


    @Query("select l from LoginUser l where l.email = ?1")
    LoginUser findByEmail(String email);

    LoginUser getByEmail(String email);

}
