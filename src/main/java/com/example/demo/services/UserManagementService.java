package com.example.demo.services;

import com.example.demo.model.User;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;

import org.springframework.stereotype.Service;
import com.google.api.core.ApiFuture;
import java.util.concurrent.ExecutionException;


@Service
public class UserManagementService {

    private static final String COLLECTION_NAME = "users";

    public String createUserDocument(User user) throws ExecutionException, InterruptedException {

        Firestore dbFirestore = FirestoreClient.getFirestore();

        ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(COLLECTION_NAME).document(user.getUid()).set(user);

        return collectionApiFuture.get().getUpdateTime().toString();
    }


}

