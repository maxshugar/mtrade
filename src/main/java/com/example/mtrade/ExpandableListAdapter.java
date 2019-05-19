package com.example.mtrade;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

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

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.order, parent, false);

        order currentOrder = ordersList.get(groupPosition);

        TextView customer_name = convertView.findViewById(R.id.customer_name);
        customer_name.setText(currentOrder.customer_name);

        TextView due_date = convertView.findViewById(R.id.due_date);
        due_date.setText(currentOrder.due_date);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_items, parent, false);

        order currentOrder = ordersList.get(groupPosition);

        ListView listView = convertView.findViewById(R.id.childItem);

        //order currentOrder = mOrderHashMap.get(groupPosition);

        /*
        ListView listView = convertView.findViewById(R.id.childItem);
        String [] s = new String[] {"test1", "test2", "test3", "test4"};

        ArrayAdapter ad = new ArrayAdapter( convertView.getContext(), android.R.layout.simple_expandable_list_item_1, s );
        listView.setAdapter(ad);
        //textView.setText(String.valueOf(getChild(groupPosition, childPosition)));
        */

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
