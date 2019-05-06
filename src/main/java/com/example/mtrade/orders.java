package com.example.mtrade;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class orders extends AppCompatActivity {

    private static final String TAG =  "orders";

    /* Disable back button. */
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed()
    {
        //super.onBackPressed();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orders);
    }

}
