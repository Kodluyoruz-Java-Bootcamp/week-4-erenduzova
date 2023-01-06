package com.eren.emlakcepteservice.controller;

import com.eren.emlakcepteservice.entity.User;
import com.eren.emlakcepteservice.request.UserRequest;
import com.eren.emlakcepteservice.response.UserResponse;
import com.eren.emlakcepteservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Create User
    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody UserRequest userRequest) {
        UserResponse userResponse = userService.createUser(userRequest);
        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }

    // Get All Users
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        List<UserResponse> userResponseList = userService.getAll();
        return new ResponseEntity<>(userResponseList, HttpStatus.FOUND);
    }

    // Get User By Id
    @GetMapping(value = "/id/{userId}")
    public ResponseEntity<UserResponse> getById(@PathVariable Integer userId) {
        UserResponse userResponse = userService.getUserResponseById(userId);
        return new ResponseEntity<>(userResponse, HttpStatus.FOUND);
    }

    // Get User By Email ( if needed )
    @GetMapping(value = "/email/{email}")
    public ResponseEntity<UserResponse> getByEmail(@PathVariable String email) {
        UserResponse userResponse = userService.getUserResponseByEmail(email);
        return new ResponseEntity<>(userResponse, HttpStatus.FOUND);
    }

    // Update Password



}
