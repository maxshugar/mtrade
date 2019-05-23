package com.example.mtrade;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FloorAdapter extends ArrayAdapter<floor> {

    private static final String TAG =  "FloorAdapter";

    private Context fContext;
    private List<floor> floorsList;

    public FloorAdapter(@NonNull Context context, @SuppressLint("SupportAnnotationUsage") @LayoutRes ArrayList<floor> list) {
        super(context, 0 , list);
        fContext = context;
        floorsList = list;
    }

    /* Inflate floor and handle selected event. */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(fContext).inflate(R.layout.floor_item,parent,false);
        floor currentFloor = floorsList.get(position);
        TextView name = listItem.findViewById(R.id.floor_name);
        name.setText(currentFloor.name);
        TextView cost = listItem.findViewById(R.id.floor_cost);
        cost.setText("£" + currentFloor.cost.toString() + "/m²");
        if(currentFloor.selected){
            listItem.setBackgroundColor(Color.LTGRAY);
        } else {
            listItem.setBackgroundColor(Color.WHITE);
        }
        return listItem;
    }

}
