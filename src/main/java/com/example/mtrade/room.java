package com.example.mtrade;

public class room {
    public String id;
    public String order_id;
    public String floor_id;
    public Double size;
    public Double cost;
    public Boolean complete;
    public Boolean selected = false;

    public room(String id, String order_id, String floor_id, Double size, Double cost,  Boolean complete){
        this.id = id;
        this.order_id = order_id;
        this.floor_id = floor_id;
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
}
