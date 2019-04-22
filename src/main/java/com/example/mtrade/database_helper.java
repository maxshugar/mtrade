package com.example.mtrade;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import androidx.annotation.RequiresApi;

public class database_helper extends SQLiteOpenHelper {

    private static final String TAG =  "database_helper";
    private static final SecureRandom RAND = new SecureRandom();

    database_helper(Context context) {
        super(context, "mtrade.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        create_tables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /* Create the applications tables. */
    public void create_tables(SQLiteDatabase db){
        String sql_query = "CREATE TABLE USERS (USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, USERNAME TEXT, EMAIL_ADDRESS TEXT, PASSWORD_HASH TEXT, PASSWORD_SALT TEXT, VERIFICATION_KEY TEXT, VERIFIED TEXT, FAILED_ATTEMPTS TEXT, SESSION_EXPIRES TEXT);";
        db.execSQL(sql_query);
        sql_query = "CREATE TABLE FLOORS (FLOOR_ID INTEGER PRIMARY KEY AUTOINCREMENT, USER_ID INTEGER, FLOOR_NAME TEXT, FLOOR_COST TEXT, FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID));";
        db.execSQL(sql_query);
        sql_query = "CREATE TABLE CUSTOMERS (CUSTOMER_ID INTEGER PRIMARY KEY AUTOINCREMENT, USER_ID INTEGER, FIRST_NAME TEXT, LAST_NAME TEXT, MOBILE_NUMBER TEXT, EMAIL_ADDRESS TEXT, FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID));";
        db.execSQL(sql_query);
        sql_query = "CREATE TABLE ORDERS (ORDER_ID INTEGER PRIMARY KEY AUTOINCREMENT, USER_ID INTEGER, CUSTOMER_ID INTEGER, DUE_DATE TEXT, TOTAL_COST TEXT, PAYMENT TEXT, CUSTOMER_NAME TEXT, STATUS TEXT, FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID), FOREIGN KEY (CUSTOMER_ID) REFERENCES CUSTOMERS(CUSTOMER_ID));";
        db.execSQL(sql_query);
        sql_query = "CREATE TABLE ROOMS (ROOM_ID INTEGER PRIMARY KEY AUTOINCREMENT, USER_ID INTEGER, ORDER_ID INTEGER, FLOOR_ID INTEGER, ROOM_SIZE TEXT, ROOM_COST TEXT, COMPLETE TEXT, FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID), FOREIGN KEY (ORDER_ID) REFERENCES ORDERS(ORDER_ID), FOREIGN KEY (FLOOR_ID) REFERENCES FLOORS(FLOOR_ID));";
        db.execSQL(sql_query);
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.N)
    public int register_user(String username, String email, String password){
        SQLiteDatabase db = this.getWritableDatabase();

        /* Check if username is unique. */
        Cursor cursor=db.rawQuery("select * from USERS where username=?",new String[]{username});
        if (cursor.moveToFirst()) {
            return -1;
        }
        else {

            String password_salt = generate_salt(512);
            String password_hash = generate_hash(password, password_salt);
            Log.e(TAG, "register_user: password_hash: " + password_hash);

            /* Get content values. */
            ContentValues contentValues = new ContentValues();
            contentValues.put("USERNAME", username);
            contentValues.put("EMAIL_ADDRESS", email);
            contentValues.put("PASSWORD_HASH", password_hash);
            contentValues.put("PASSWORD_SALT", String.valueOf(password_salt));
            contentValues.put("VERIFICATION_KEY", "1234");
            contentValues.put("VERIFIED", "FALSE");
            contentValues.put("FAILED_ATTEMPTS", "0");


            long result = 0;

            try{
                result = db.insertOrThrow("USERS", null, contentValues);

            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            }
            Log.d(TAG, "register_user: " + (result >= 0));
            if (result == -1) {
                return -2;
            } else {
                return 0;
            }

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public int validate_login(String username, String password){

        SQLiteDatabase db = this.getWritableDatabase();

        int ret = 0;

        Cursor cursor=db.rawQuery("select * from USERS where username=?",new String[]{username});
        if (cursor.moveToFirst()) {
            String hash = cursor.getString(cursor.getColumnIndex("PASSWORD_HASH"));
            String salt = cursor.getString(cursor.getColumnIndex("PASSWORD_SALT"));
            Log.d(TAG, "validate_login: hash: " + hash);
            /* Generate hash. */
            String new_hash = generate_hash(password, salt);
            Log.d(TAG, "validate_login: new_hash: " + new_hash);
            if(new_hash == null){
                ret = -2;
            } else if(new_hash.equals(hash) == false){
                ret = -3;
            } else if(new_hash.equals(hash) == true){
                ret = 0;
            }
        }
        else {
            /* If no user was found with the specified username. */
            ret = -1;
        }

        return ret;
    }

    /* Returns a hashed password. */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String generate_hash(String password, String salt){

        String hash = null;
        char[] chars = password.toCharArray();
        byte[] bytes = salt.getBytes();
        /* Build Spec. */
        PBEKeySpec spec = new PBEKeySpec(chars, bytes, 65536, 512);

        try {
            Arrays.fill(chars, Character.MIN_VALUE);
            /* Get algorithm instance. */
            SecretKeyFactory fac = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            /* Generate hash. */
            byte[] securePassword = fac.generateSecret(spec).getEncoded();
            /* Store hash in string. */
            hash = Base64.getEncoder().encodeToString(securePassword);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            System.err.println("Exception encountered in hashPassword()");
            Optional.empty();
        } finally {
            spec.clearPassword();
        }
        return hash;

    }

    /* Generate a random salt. */
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.N)
    public String generate_salt (final int length) {
        if (length < 1) {
            System.err.println("error in generateSalt: length must be > 0");
            return null;
        }
        byte[] salt = new byte[length];
        RAND.nextBytes(salt);
        /* Set the value. */
        return Base64.getEncoder().encodeToString(salt);
    }

}
