package com.example.demo.firebase;

import java.io.FileInputStream;
import java.io.IOException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;


@Component
public class FirebaseConfig {

    @PostConstruct
    public void firebaseInit() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("src/main/resources/serviceAccount.json");
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            FirebaseApp.initializeApp(options);

    }
}

