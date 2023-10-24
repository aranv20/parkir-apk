package com.example.parkirfirebase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ButtonBarLayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    Button button;
    TextView textView;
    FirebaseUser user;

//    BottomNavigationView ButtonNavigationView;
//
//    private BottomNavigationView.OnNavigationItemSelectedListener navigation = new BottomNavigationView.OnNavigationItemSelectedListener(){
//        @Override
//        public boolean onNavigationItemSelected(MenuItem item){
//            Activity a = null;
//            switch (item.getItemId()){
//                case R.id.menu_home:
//                    a = new MainActivity();
//                    break;
//                case R.id.menu_parking:
//                    a = new ParkingActivity();
//                    break;
//                case R.id.menu_scan:
//                    a = new ScanActivity();
//                    break;
//                case R.id.menu_history:
//                    a = new HistoryActivity();
//                    break;
//                case R.id.menu_profile:
//                    a = new ProfileActivity();
//                    break;
//            }
//            return false;
//        }
//    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logout);
        textView = findViewById(R.id.user_details);
        user =  auth.getCurrentUser();
        if (user == null){
            Intent intent = new Intent(getApplicationContext(),Login.class);
            startActivity(intent);
            finish();
        }
        else {
            textView.setText(user.getEmail());
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(),Login.class);
                startActivity(intent);
                finish();
            }
        });
    }
}