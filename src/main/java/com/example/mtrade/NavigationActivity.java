package com.example.mtrade;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class NavigationActivity extends AppCompatActivity {

    private static final String TAG =  "NavigationActivity";

    /* Disable back button. */
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed()
    {
        //super.onBackPressed();
    }

    private String user_id;

    @Override
    public boolean onCreateOptionsMenu(Menu menu){ return true; }

    /* Set logout click event listener. */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem item = menu.findItem(R.id.action_logout);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener (){

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                return true;
            }
        });
        return true;

    }


    /* Inflate orders fragment by default. */
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation);
        user_id = getIntent().getStringExtra("USER_ID");
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListner);
        Fragment f = new OrdersFragment();
        Bundle bundle = new Bundle();
        bundle.putString("USER_ID", user_id);
        f.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, f).commit();

    }

    /* Switch between selected navigation buttons. */
    private BottomNavigationView.OnNavigationItemSelectedListener navListner =
        new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment selectedFragment = null;
                switch (menuItem.getItemId()) {
                    case R.id.nav_orders:
                        selectedFragment = new OrdersFragment();
                        break;
                    case R.id.nav_floors:
                        selectedFragment = new FloorsFragment();
                        break;
                    case R.id.nav_customers:
                        selectedFragment = new CustomersFragment();
                        break;
                }
                invalidateOptionsMenu();
                Bundle bundle = new Bundle();
                bundle.putString("USER_ID", user_id);
                selectedFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedFragment).commit();
                return true;
            }
        };
}
