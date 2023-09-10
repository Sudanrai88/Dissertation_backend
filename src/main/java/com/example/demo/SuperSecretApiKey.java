package com.example.demo;

public class SuperSecretApiKey {
    public static String getApiKey() {
        return System.getenv("API_KEY");
    }
}
