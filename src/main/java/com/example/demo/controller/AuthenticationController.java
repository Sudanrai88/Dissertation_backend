package com.example.demo.controller;

import com.example.demo.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.example.demo.services.UserManagementService;

import java.util.concurrent.ExecutionException;


@RestController
@RequestMapping("/api")
public class AuthenticationController {

    @Autowired
    private UserManagementService userManagementService;

    @CrossOrigin(origins = {"http://localhost:3000", "https://gentrip.netlify.app"})

    @PostMapping("/verify-token")
    public ResponseEntity<Boolean> verifyToken(@RequestBody String request) {
        try {
            // Verify the Firebase token using the Firebase Admin SDK
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(request);
            User user = new User();


            user.setEmail(decodedToken.getEmail());
            user.setName(decodedToken.getName());
            user.setPhotoURL(decodedToken.getPicture());
            user.setUid(decodedToken.getUid());

            userManagementService.createUserDocument(user);

            //token is valid
            System.out.println("worked!");
            System.out.println(user);
            return new ResponseEntity<>(true, HttpStatus.OK);

        } catch (FirebaseAuthException | ExecutionException | InterruptedException e) {
            System.out.println(request);
            // Token verification failed
            System.out.println("Failed");
            return new ResponseEntity<>(false, HttpStatus.NO_CONTENT);
        }
    }
}

