package com.example.parkirfirebase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ButtonBarLayout;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

//    FirebaseAuth auth;
//    Button button;
//    TextView textView;
//    FirebaseUser user;

    private MeowBottomNavigation meowBottomNavigation;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        meowBottomNavigation = findViewById(R.id.meowBottom);

        //add item menu
        meowBottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.baseline_home_24));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.baseline_car_24));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.baseline_qr_code_scanner_24));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(4, R.drawable.baseline_file_24));
        meowBottomNavigation.add(new MeowBottomNavigation.Model(5, R.drawable.baseline_account_24));

        meowBottomNavigation.setOnShowListener(new MeowBottomNavigation.ShowListener() {
            @Override
            public void onShowItem(MeowBottomNavigation.Model item) {
                Fragment fragment = null;

                switch (item.getId()){
                    case 1 :
                        fragment = new HomeFragment();
                        break;
                    case 2 :
                        fragment = new ParkingFragment();
                        break;
                    case 3 :
                        fragment = new ScanFragment();
                        break;
                    case 4 :
                        fragment = new HistoryFragment();
                        break;
                    case 5 :
                        fragment = new ProfileFragment();
                        break;
                }

                loadFragment(fragment);
            }
        });

        //set nofication count
        meowBottomNavigation.setCount(1, "10");

        //set default
        meowBottomNavigation.show(2, true);

        meowBottomNavigation.setOnClickMenuListener(new MeowBottomNavigation.ClickListener() {
            @Override
            public void onClickItem(MeowBottomNavigation.Model item) {
                Toast.makeText(getApplicationContext(), + item.getId(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout,fragment)
                .commit();
    }

//        auth = FirebaseAuth.getInstance();
//        button = findViewById(R.id.logout);
//        textView = findViewById(R.id.user_details);
//        user =  auth.getCurrentUser();
//        if (user == null){
//            Intent intent = new Intent(getApplicationContext(),Login.class);
//            startActivity(intent);
//            finish();
//        }
//        else {
//            textView.setText(user.getEmail());
//        }
//
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                FirebaseAuth.getInstance().signOut();
//                Intent intent = new Intent(getApplicationContext(),Login.class);
//                startActivity(intent);
//                finish();
//            }
//        });

    }