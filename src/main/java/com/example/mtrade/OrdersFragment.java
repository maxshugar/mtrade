package com.example.mtrade;

import android.R.layout;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import static android.R.layout.*;

public class OrdersFragment extends Fragment {

    boolean add_order_flag = true;
    boolean add_room_flag = false;
    boolean edit_order_flag = false;
    boolean edit_room_flag = false;
    boolean delete_order_flag = false;
    boolean delete_room_flag = false;

    ArrayList<order> orders = new ArrayList<>();

    private static final String TAG =  "OrdersFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Orders");

        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        ExpandableListView expandableListView = view.findViewById(R.id.expandableListView);


        database_helper db = new database_helper(getActivity());

        Cursor orders_c = db.get_orders("1");

        while(orders_c.moveToNext()){

            ArrayList<room> rooms = new ArrayList<>();
            String order_id = orders_c.getString(orders_c.getColumnIndex("ORDER_ID"));
            Cursor rooms_c = db.get_rooms("1", order_id);
            while(rooms_c.moveToNext()){
                rooms.add(new room(
                        rooms_c.getString(rooms_c.getColumnIndex("ROOM_ID")),
                        rooms_c.getString(rooms_c.getColumnIndex("ORDER_ID")),
                        rooms_c.getString(rooms_c.getColumnIndex("FLOOR_ID")),
                        rooms_c.getDouble(rooms_c.getColumnIndex("ROOM_SIZE")),
                        rooms_c.getDouble(rooms_c.getColumnIndex("ROOM_COST")),
                        Boolean.parseBoolean(rooms_c.getString(rooms_c.getColumnIndex("COMPLETE")))
                        ));
            }
            rooms_c.close();

            orders.add(new order(
                    orders_c.getString(orders_c.getColumnIndex("ORDER_ID")),
                    orders_c.getString(orders_c.getColumnIndex("CUSTOMER_ID")),
                    orders_c.getString(orders_c.getColumnIndex("DUE_DATE")),
                    orders_c.getDouble(orders_c.getColumnIndex("TOTAL_COST")),
                    Boolean.parseBoolean(orders_c.getString(orders_c.getColumnIndex("PAYMENT"))),
                    orders_c.getString(orders_c.getColumnIndex("CUSTOMER_NAME")),
                    Boolean.parseBoolean(orders_c.getString(orders_c.getColumnIndex("STATUS"))),
                    rooms
            ));
        }
        orders_c.close();


        /*
        ArrayList<String> linuxGroups = new ArrayList<>();
        linuxGroups.add("Ubuntu");
        linuxGroups.add("Ubuntu");
        linuxGroups.add("Ubuntu");
        linuxGroups.add("Ubuntu");
        linuxGroups.add("Ubuntu");
        linuxGroups.add("Ubuntu");
        linuxGroups.add("Ubuntu");
        linuxGroups.add("Ubuntu");

        item.put("Linux", linuxGroups);

        ArrayList<String> windowsGroups = new ArrayList<>();
        windowsGroups.add("XP");
        windowsGroups.add("XP");
        windowsGroups.add("VISTA");
        windowsGroups.add("XP");
        windowsGroups.add("8");
        windowsGroups.add("XP");
        windowsGroups.add("10");
        windowsGroups.add("7");

        item.put("Windows", windowsGroups);
        */

        ExpandableListAdapter adapter = new ExpandableListAdapter(orders);
        expandableListView.setAdapter(adapter);

        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.action_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        MenuItem item = menu.findItem(R.id.action_add);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener (){
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                LayoutInflater inflater = getLayoutInflater();
                final View mView = inflater.inflate(R.layout.dialog_new_order, null);

                /* Spinner. */
                final Spinner customer_spinner = mView.findViewById(R.id.customer_spinner);
                database_helper db = new database_helper(getActivity());
                Cursor c = db.get_customers("1");
                //final ArrayList<Integer> customer_ids = new ArrayList<>();
                final ArrayList<customer> customers = new ArrayList<>();
                ArrayList<String> list = new ArrayList<>();
                list.add("");
                while(c.moveToNext()){
                    list.add(c.getString(c.getColumnIndex("FIRST_NAME")) + " "
                            + c.getString(c.getColumnIndex("LAST_NAME")) + " - "
                            + c.getString(c.getColumnIndex("EMAIL_ADDRESS")));

                    customers.add(new customer(
                            c.getString(c.getColumnIndex("CUSTOMER_ID")),
                            c.getString(c.getColumnIndex("FIRST_NAME")),
                            c.getString(c.getColumnIndex("LAST_NAME")),
                            c.getString(c.getColumnIndex("EMAIL_ADDRESS")),
                            c.getString(c.getColumnIndex("MOBILE_NUMBER"))));

                }
                c.close();
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), layout.simple_spinner_dropdown_item, list);
                customer_spinner.setAdapter(adapter);

                /* Due date. */
                final DatePicker simpleDatePicker = mView.findViewById(R.id.simpleDatePicker); // initiate a date picker
                simpleDatePicker.setMinDate(System.currentTimeMillis() - 1000);
                //simpleDatePicker.setCalenderShown(false); // set false value for the spinner shown function


                builder.setView(mView)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                int p = customer_spinner.getSelectedItemPosition();

                                if(p == 0){
                                    Toast.makeText(getContext(),"Please select a customer",Toast.LENGTH_SHORT).show();
                                } else {

                                    customer selected = customers.get(p - 1);

                                    int customer_id = Integer.parseInt(selected.id);

                                    int year = simpleDatePicker.getYear();
                                    int month = simpleDatePicker.getMonth();
                                    int day = simpleDatePicker.getDayOfMonth();
                                    String due_date = Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day);

                                    //Toast.makeText(getContext(), due_date, Toast.LENGTH_SHORT).show();

                                    database_helper db = new database_helper(getActivity());


                                    if(db.create_order(1, customer_id, due_date, 0.0, false, selected.first_name + " " + selected.last_name, false) == 0){
                                        Toast.makeText(getContext(),"Order created successfully.", Toast.LENGTH_SHORT).show();
                                        getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                                new OrdersFragment()).commit();
                                    } else {
                                        Toast.makeText(getContext(),"Something went wrong.", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Toast.makeText(getContext(),"Cancel clicked",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setTitle("Add Order");
                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });

        if(add_order_flag) item.setVisible(true);
        else item.setVisible(false);

        item = menu.findItem(R.id.action_edit);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener (){
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                for (final order order : orders) {
                    if(order.selected){

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        LayoutInflater inflater = getLayoutInflater();
                        final View mView = inflater.inflate(R.layout.dialog_new_customer, null);

                        builder.setView(mView)
                                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {


                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //Toast.makeText(getContext(),"cancel clicked",Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setTitle("Edit Customer");
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }
                }

                return true;
            }
        });
        if(edit_order_flag) item.setVisible(true);
        else item.setVisible(false);

        item = menu.findItem(R.id.action_delete);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener (){
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.N)
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:

                                break;

                            case DialogInterface.BUTTON_NEGATIVE:

                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).setTitle("Delete selected").show();

                return true;
            }
        });
        if(delete_order_flag) item.setVisible(true);
        else item.setVisible(false);

        super.onPrepareOptionsMenu(menu);
    }


}
