package com.example.mtrade;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class register extends AppCompatActivity {

    private static final String TAG =  "register";

    /* Disable back button. */
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed()
    {
        //super.onBackPressed();
    }

    /* Inflate register view and handle register button click event. */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        final database_helper my_db = new database_helper(this);
        Button register_btn = findViewById(R.id.register_btn);
        register_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                EditText username_input = findViewById(R.id.username_input);
                String username = username_input.getText().toString();
                EditText email_input = findViewById(R.id.email_input);
                String email = email_input.getText().toString();
                EditText password_input = findViewById(R.id.password_input);
                String password = password_input.getText().toString();
                EditText password_input2 = findViewById(R.id.password_input2);
                String password2 = password_input2.getText().toString();
                if((!TextUtils.isEmpty(email)) &&(Patterns.EMAIL_ADDRESS.matcher(email).matches() == false)){
                    Toast.makeText(register.this, "Email address format invalid.", Toast.LENGTH_LONG).show();
                } else{
                    if(username == "" || email == "" || password == "" || password2 == ""){
                        Toast.makeText(register.this, "Please complete all input fields.", Toast.LENGTH_LONG).show();
                    }
                    else if(password.equals(password2) == false){
                        Toast.makeText(register.this, "Passwords don't match.", Toast.LENGTH_LONG).show();
                    }else if(password.length() < 8){
                        Toast.makeText(register.this, "Password must have a minimum of eight characters.", Toast.LENGTH_LONG).show();
                    }  else {
                        int ret = my_db.register_user(username,email,password);
                        if(ret == -1){
                            Toast.makeText(register.this, "Username " + username + " already registered to an account.", Toast.LENGTH_LONG).show();
                        } else if( ret == -2){
                            Toast.makeText(register.this, "Something went wrong.", Toast.LENGTH_LONG).show();
                        }else if( ret == 0){
                            Toast.makeText(register.this, "Register successful.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });
        Button login_btn = findViewById(R.id.login_btn);
        login_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(register.this, MainActivity.class);
                intent.putExtra("TABLE_NAME", "USERS");
                startActivity(intent);
            }
        });

    }

}
