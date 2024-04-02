package com.stegano.steg0vault.controllers;

import com.stegano.steg0vault.models.DTOs.*;
import com.stegano.steg0vault.models.entities.Resource;
import com.stegano.steg0vault.models.entities.User;
import com.stegano.steg0vault.services.UserService;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@Slf4j
@RequestMapping("/api/v1/user")
@CrossOrigin
//        (
//    origins = {
//            "http://localhost:4200"
//    },
//    methods = {
//            RequestMethod.POST,
//            RequestMethod.GET,
//            RequestMethod.PATCH
//    })
public class UserController { }
