package com.geinforce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.geinforce.service.InstituteService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class RESTInstituteController {
    @Autowired
    InstituteService instituteService;

    @GetMapping("/verifiedInstituteNames")
    public ResponseEntity<List<String>>getVerifiedInstituteNames(){
        List<String>instituteList = instituteService.getVerifiedInstituteNames();
        System.out.println("list="+instituteList);
        if(instituteList.isEmpty()){
            System.out.println(HttpStatus.NO_CONTENT);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ArrayList<>());
        }else{
            System.out.println(HttpStatus.OK);
            return ResponseEntity.status(HttpStatus.OK).body(instituteList);
        }
    }
}
