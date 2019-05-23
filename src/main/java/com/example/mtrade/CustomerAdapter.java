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

public class CustomerAdapter extends ArrayAdapter<customer> {

    private Context cContext;
    private List<customer> customersList;

    /* Constructor - get customer class list. */
    public CustomerAdapter(@NonNull Context context, @SuppressLint("SupportAnnotationUsage") @LayoutRes ArrayList<customer> list) {
        super(context, 0 , list);
        cContext = context;
        customersList = list;
    }

    /* Bind customer to text views and handle selected background. */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(cContext).inflate(R.layout.customer_item,parent,false);

        customer currentCustomer = customersList.get(position);

        TextView name = listItem.findViewById(R.id.customer_name);
        name.setText(currentCustomer.first_name + " " + currentCustomer.last_name);

        TextView email = listItem.findViewById(R.id.customer_email);
        email.setText(currentCustomer.email);

        if(currentCustomer.selected){
            listItem.setBackgroundColor(Color.LTGRAY);
        } else {
            listItem.setBackgroundColor(Color.WHITE);
        }

        return listItem;
    }

}
