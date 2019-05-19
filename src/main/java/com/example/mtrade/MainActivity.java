package com.example.mtrade;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG =  "MainActivity";

    /* Disable back button. */
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed()
    {
        //super.onBackPressed();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        /* Override login. */
        Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
        startActivity(intent);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final database_helper my_db = new database_helper(this);

        Button login_btn = findViewById(R.id.login_btn);

        login_btn.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v){
                EditText username_input = findViewById(R.id.username_input);
                String username = username_input.getText().toString();
                EditText password_input = findViewById(R.id.password_input);
                String password = password_input.getText().toString();
                /* Input validation. */
                if(username.isEmpty() || password.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please complete all input fields.", Toast.LENGTH_LONG).show();
                }
               else{
                    int ret = my_db.validate_login(username, password);
                    Log.d(TAG, "validate_login: '" + ret + "'");
                    if( ret == -1 ){
                        Toast.makeText(MainActivity.this, "Account with username '" + username + "' does not exist.", Toast.LENGTH_LONG).show();
                    } else if( ret == -2 ){
                        Toast.makeText(MainActivity.this, "Something went wrong :(", Toast.LENGTH_LONG).show();
                    } else if( ret == -3 ){
                        Toast.makeText(MainActivity.this, "Password incorrect.", Toast.LENGTH_LONG).show();
                    } else if( ret == 0 ){
                        Toast.makeText(MainActivity.this, "Login successful :)", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
                        //intent.putExtra("TABLE_NAME", "USERS");
                        startActivity(intent);

                    }
               }
            }
        });

        Button register_btn = findViewById(R.id.register_btn);

        register_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, register.class);
                intent.putExtra("TABLE_NAME", "USERS");
                startActivity(intent);
            }
        });
    }
}
