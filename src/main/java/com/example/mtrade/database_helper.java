package com.example.mtrade;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

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

    /* Return the users ID. */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public int validate_login(String username, String password){

        SQLiteDatabase db = this.getWritableDatabase();

        int ret = 0;

        Cursor cursor=db.rawQuery("select * from USERS where username=?",new String[]{username});
        if (cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndex("USER_ID"));
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
                ret = Integer.parseInt(id);
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

    /* Create a customer assigning parameters to properties. */
    public int create_customer(String firstname, String lastname, String phone, String email, int user_id){

        SQLiteDatabase db = this.getWritableDatabase();
        /* Get content values. */
        ContentValues contentValues = new ContentValues();
        contentValues.put("FIRST_NAME", firstname);
        contentValues.put("LAST_NAME", lastname);
        contentValues.put("MOBILE_NUMBER", phone);
        contentValues.put("EMAIL_ADDRESS", email);
        contentValues.put("USER_ID", user_id);
        long result = 0;
        try{
            result = db.insertOrThrow("CUSTOMERS", null, contentValues);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
        if (result == -1) {
            return -2;
        } else {
            return 0;
        }

    }

    /* Delete customer. */
    public int delete_customer(String customer_id){

        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("CUSTOMERS", "CUSTOMER_ID=?", new String[]{customer_id});

    }

    /* Update customer. */
    public int update_customer(String customer_id, String firstname, String lastname, String phone, String email){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("FIRST_NAME",firstname); //These Fields should be your String values of actual column names
        cv.put("LAST_NAME",lastname);
        cv.put("MOBILE_NUMBER",phone);
        cv.put("EMAIL_ADDRESS",email);
        return db.update("CUSTOMERS", cv, "CUSTOMER_ID=?", new String[]{customer_id});

    }

    /* Get all customers for user. */
    public Cursor get_customers(String user_id){

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        try{
            cursor = db.rawQuery("select * from CUSTOMERS WHERE USER_ID=?", new String[]{user_id});
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
        return cursor;

    }

    /* Get all floors for user. */
    public Cursor get_floors(String user_id){

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        try{
            cursor = db.rawQuery("select * from FLOORS WHERE USER_ID=?", new String[]{user_id});
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
        return cursor;

    }

    /* Create floor. */
    public int create_floor(String name, String cost, int user_id){

        SQLiteDatabase db = this.getWritableDatabase();
        /* Get content values. */
        ContentValues contentValues = new ContentValues();
        contentValues.put("FLOOR_NAME", name);
        contentValues.put("FLOOR_COST", cost);
        contentValues.put("USER_ID", user_id);
        long result = 0;
        try{
            result = db.insertOrThrow("FLOORS", null, contentValues);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
        if (result == -1) {
            return -2;
        } else {
            return 0;
        }

    }

    /* Delete floor. */
    public int delete_floor(String floor_id){

        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("FLOORS", "FLOOR_ID=?", new String[]{floor_id});

    }

    /* Update floor. */
    public int update_floor(String floor_id, String name, String cost){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("FLOOR_NAME",name);
        cv.put("FLOOR_COST",cost);
        return db.update("FLOORS", cv, "FLOOR_ID=?", new String[]{floor_id});

    }

    /* Get orders for user. */
    public Cursor get_orders(String user_id){

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        try{
            cursor = db.rawQuery("select * from ORDERS WHERE USER_ID=?", new String[]{user_id});
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
        return cursor;

    }

    /* Get rooms for user. */
    public Cursor get_rooms(String user_id, String order_id){

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        try{
            cursor = db.rawQuery("select * from ROOMS WHERE USER_ID=? AND ORDER_ID=?", new String[]{user_id, order_id});
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
        return cursor;

    }

    /* Create order. */
    public int create_order(String user_id, int customer_id, String due_date, Double total_cost, Boolean payment_recieved, String customer_name, Boolean status_complete ){

        SQLiteDatabase db = this.getWritableDatabase();
        /* Get content values. */
        ContentValues contentValues = new ContentValues();
        contentValues.put("USER_ID", user_id);
        contentValues.put("CUSTOMER_ID", customer_id);
        contentValues.put("DUE_DATE", due_date);
        contentValues.put("TOTAL_COST", total_cost);
        contentValues.put("PAYMENT", payment_recieved);
        contentValues.put("CUSTOMER_NAME", customer_name);
        contentValues.put("STATUS", status_complete);
        long result = 0;
        try{
            result = db.insertOrThrow("ORDERS", null, contentValues);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
        if (result == -1) {
            return -2;
        } else {
            return 0;
        }

    }

    /* Create room for user. */
    public int create_room(String user_id, int order_id, int floor_id, Double room_size, Double room_cost, Boolean complete){

        SQLiteDatabase db = this.getWritableDatabase();
        /* Get content values. */
        ContentValues contentValues = new ContentValues();
        contentValues.put("USER_ID", user_id);
        contentValues.put("ORDER_ID", order_id);
        contentValues.put("FLOOR_ID", floor_id);
        contentValues.put("ROOM_SIZE", room_size);
        contentValues.put("ROOM_COST", room_cost);
        contentValues.put("COMPLETE", complete);
        long result = 0;
        try{
            result = db.insertOrThrow("ROOMS", null, contentValues);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
        if (result == -1) {
            return -2;
        } else {
            return 0;
        }

    }

    /* Update room. */
    public int update_room(String room_id, int floor_id, Double room_size, Double room_cost){

        SQLiteDatabase db = this.getWritableDatabase();
        /* Get content values. */
        ContentValues contentValues = new ContentValues();
        contentValues.put("FLOOR_ID", floor_id);
        contentValues.put("ROOM_SIZE", room_size);
        contentValues.put("ROOM_COST", room_cost);
        return db.update("ROOMS", contentValues, "ROOM_ID=?", new String[]{room_id});

    }

    /* Delete order. */
    public int delete_order(String order_id){

        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("ORDERS", "ORDER_ID=?", new String[]{order_id});

    }

    /* Delete room. */
    public int delete_room(String room_id){

        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("ROOMS", "ROOM_ID=?", new String[]{room_id});

    }


    /* Update order. */
    public int update_order(String order_id, int customer_id, String due_date, String customer_name ){

        SQLiteDatabase db = this.getWritableDatabase();
        /* Get content values. */
        ContentValues contentValues = new ContentValues();
        contentValues.put("CUSTOMER_ID", customer_id);
        contentValues.put("DUE_DATE", due_date);
        contentValues.put("CUSTOMER_NAME", customer_name);
        return db.update("ORDERS", contentValues, "ORDER_ID=?", new String[]{order_id});

    }

}
