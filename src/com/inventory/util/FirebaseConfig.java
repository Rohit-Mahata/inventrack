package com.inventory.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.FileInputStream;
import java.io.InputStream;

public class FirebaseConfig {
    private static Firestore db = null;
    private static boolean initialized = false;

    public static void initialize() {
        try {
            if (initialized) return;
            // Try loading from inside JAR first, then fall back to filesystem
            InputStream serviceAccount = FirebaseConfig.class.getResourceAsStream("/config/serviceAccountKey.json");
            if (serviceAccount == null) {
                serviceAccount = new FileInputStream("serviceAccountKey.json");
            }
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
            FirebaseApp.initializeApp(options);
            db = FirestoreClient.getFirestore();
            initialized = true;
            System.out.println("Firebase connected!");
        } catch (Exception e) {
            System.out.println("Firebase init error: " + e.getMessage());
        }
    }

    public static Firestore getDB() { return db; }
    public static boolean isConnected() { return initialized && db != null; }
}
