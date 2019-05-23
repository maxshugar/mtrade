package com.example.mtrade;

public class room {
    public String id;
    public String order_id;
    public String floor_id;
    public String floor_name;
    public Double size;
    public Double cost;
    public Boolean complete;
    public Boolean selected = false;
    public Boolean checked = false;

    /* Constructor. */
    public room(String id, String order_id, String floor_id, String floor_name, Double size, Double cost,  Boolean complete){
        this.id = id;
        this.order_id = order_id;
        this.floor_id = floor_id;
        this.floor_name = floor_name;
        this.size = size;
        this.cost = cost;
        this.complete = complete;
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
