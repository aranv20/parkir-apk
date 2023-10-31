package com.example.parkirfirebase;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends Activity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

        Thread thread = new Thread() {
            public void run() {
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    // Cek apakah pengguna sudah login atau belum
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        // Pengguna sudah login, pindahkan ke MainActivity
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    } else {
                        // Pengguna belum login, pindahkan ke halaman login
                        startActivity(new Intent(SplashActivity.this, Login.class));
                    }
                    finish();
                }
            }
        };
        thread.start();
    }
}
