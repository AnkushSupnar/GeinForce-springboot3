package com.geinforce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.geinforce.model.Contact;

@Repository
public interface ContactRepository extends JpaRepository<Contact,Long> {

}
