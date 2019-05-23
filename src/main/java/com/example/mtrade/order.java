package com.example.mtrade;

import java.util.ArrayList;

public class order {
    public String id;
    public String customer_id;
    public String due_date;
    public Double total_cost;
    public Boolean payment_recieved;
    public Boolean status_complete;
    public String customer_name;
    public Boolean selected = false;
    public ArrayList<room> rooms;
    public Boolean checked = false;

    /* Constructor. */
    public order(String id, String customer_id, String due_date, Double total_cost, Boolean payment_recieved, String customer_name, Boolean status_complete, ArrayList<room> rooms){
        this.id = id;
        this.customer_id = customer_id;
        this.due_date = due_date;
        this.total_cost = total_cost;
        this.payment_recieved = payment_recieved;
        this.status_complete = status_complete;
        this.customer_name = customer_name;
        this.rooms = rooms;
    }

    public void select(){
        this.selected = true;
    }

    public void deselect(){
        this.selected = false;
    }

    public void check(){
        this.checked = true;
    }

    public void uncheck(){
        this.checked = false;
    }

}
