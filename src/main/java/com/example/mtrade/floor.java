package com.example.mtrade;

public class floor {
    public String id;
    public String name;
    public Double cost;
    public Boolean selected = false;

    public floor(String id, String name, Double cost){
        this.id = id;
        this.name = name;
        this.cost = cost;
    }

    public void select(){
        this.selected = true;
    }

    public void deselect(){
        this.selected = false;
    }
}


