package com.example.mtrade;

public class customer {
    public String id;
    public String first_name;
    public String last_name;
    public String email;
    public String phone_number;
    public Boolean selected = false;

    public customer(String id, String first_name, String last_name, String email, String phone_number){
        this.id = id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.phone_number = phone_number;
    }

    public void select(){
        this.selected = true;
    }

    public void deselect(){
        this.selected = false;
    }
}
