package com.geinforce.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.geinforce.model.Institute;
import com.geinforce.model.Student;
import com.geinforce.model.User;
import com.geinforce.service.InstituteService;
import com.geinforce.service.StudentService;
import com.geinforce.service.UserService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


@Component
public class CommonUtility {
    @Autowired
    UserService userService;
    @Autowired
    InstituteService instituteService;
    @Autowired
    StudentService studentService;

    public User getUserByEmail(String email){
        return userService.getByEmail(email);
    }
    public Institute getInstituteByEmail(String email){
        return instituteService.getByEmail(email);
    }
    public Student getStudentByEmail(String email){
        return studentService.getByEmail(email);
    }

    public LocalDateTime getDateAndTime(String jobName){
        long timestamp = Long.parseLong(jobName);
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime;
    }
}
