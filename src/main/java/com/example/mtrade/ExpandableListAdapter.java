package com.example.mtrade;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private static final String TAG =  "ExpandableListAdapter";

    private List<order> ordersList;

    public ExpandableListAdapter(List<order> ordersList) {
        this.ordersList = ordersList;
    }

    @Override
    public int getGroupCount() {
        return ordersList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return ordersList.get(groupPosition).rooms.size();
    }

    @Override
    public Object getGroup(int groupPosition) {

        return ordersList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return ordersList.get(groupPosition).rooms.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition*childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private View orderView;

    /* Inflate and populate group view. Handle check box click. */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if(convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.order, parent, false);
        orderView = convertView;
        final order currentOrder = ordersList.get(groupPosition);
        TextView customer_name = convertView.findViewById(R.id.customer_name);
        customer_name.setText(currentOrder.customer_name);
        TextView due_date = convertView.findViewById(R.id.due_date);
        due_date.setText(currentOrder.due_date);
        Double total = 0.0;
        for(room room: currentOrder.rooms)
            total += room.cost;
        TextView total_cost = convertView.findViewById(R.id.total_cost);
        total_cost.setText("£" + Double.toString(total));
        final ProgressBar order_progress = convertView.findViewById(R.id.order_progress);
        CheckBox payment_checkbox = convertView.findViewById(R.id.payment_checkbox);
        payment_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    currentOrder.check();
                 else
                    currentOrder.uncheck();
                order_progress.setProgress(calculate_order_progress(currentOrder));
            }
        });
        if(currentOrder.selected)
            convertView.setBackgroundColor(Color.LTGRAY);
         else
            convertView.setBackgroundColor(Color.WHITE);
        return convertView;

    }

    /* Inflate and populate child view. Handle check box click. */
    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        if(convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.room, parent, false);
        final room currentRoom = ordersList.get(groupPosition).rooms.get(childPosition);
        TextView floor_name = convertView.findViewById(R.id.floor_name);
        floor_name.setText(currentRoom.floor_name);
        TextView room_size = convertView.findViewById(R.id.room_size);
        room_size.setText(Double.toString(currentRoom.size) + "m²");
        TextView room_cost = convertView.findViewById(R.id.room_cost);
        room_cost.setText("£" + Double.toString(currentRoom.cost));
        final ProgressBar order_progress = orderView.findViewById(R.id.order_progress);
        CheckBox completed_checkbox = convertView.findViewById(R.id.completed_checkbox);
        completed_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    currentRoom.check();
                else
                    currentRoom.uncheck();
                order_progress.setProgress(calculate_order_progress(ordersList.get(groupPosition)));
            }
        });
        if(currentRoom.selected)
            convertView.setBackgroundColor(Color.LTGRAY);
        else
            convertView.setBackgroundColor(Color.WHITE);
        return convertView;

    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    /* Calculate order progress as percentage, rounded to the nearest integer. */
    private int calculate_order_progress(order currentOrder){

        Double total = Double.valueOf(currentOrder.rooms.size() + 1);
        Double complete = 0.0;
        Double result = 0.0;
        if(currentOrder.checked)
            complete = 1.0;
        for(room room: currentOrder.rooms)
            if(room.checked)
                complete += 1.0;
        result = (complete / total) * 100;
        return (int)Math.round(result);

    }

}
