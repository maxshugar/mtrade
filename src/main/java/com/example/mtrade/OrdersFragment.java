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
import android.widget.AdapterView;
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
    ArrayList<order> orders = new ArrayList<>();
    private String user_id;
    private static final String TAG =  "OrdersFragment";

    /* Get order and rooms, map to classes and inflate views. */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle args = getArguments();
        user_id = args.getString("USER_ID");
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Orders");
        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        final ExpandableListView expandableListView = view.findViewById(R.id.expandableListView);
        database_helper db = new database_helper(getActivity());
        /* Build floors hash map. */
        Cursor floors_c = db.get_floors(user_id);
        HashMap<String, String> floors_hash = new HashMap<String, String>();
        while(floors_c.moveToNext()){
            String floor_id = floors_c.getString(floors_c.getColumnIndex("FLOOR_ID"));
            String floor_name = floors_c.getString(floors_c.getColumnIndex("FLOOR_NAME"));
            floors_hash.put(floor_id, floor_name);
        }
        floors_c.close();
        Cursor orders_c = db.get_orders(user_id);
        while(orders_c.moveToNext()){
            ArrayList<room> rooms = new ArrayList<>();
            String order_id = orders_c.getString(orders_c.getColumnIndex("ORDER_ID"));
            Cursor rooms_c = db.get_rooms(user_id, order_id);
            while(rooms_c.moveToNext()){
                String floor_id = rooms_c.getString(rooms_c.getColumnIndex("FLOOR_ID"));
                rooms.add(new room(
                        rooms_c.getString(rooms_c.getColumnIndex("ROOM_ID")),
                        rooms_c.getString(rooms_c.getColumnIndex("ORDER_ID")),
                        floor_id,
                        floors_hash.get(floor_id),
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
        final ExpandableListAdapter adapter = new ExpandableListAdapter(orders);
        expandableListView.setAdapter(adapter);
        final int[] selected_group = {0};
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener(){
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if(orders.get(groupPosition).selected){
                    selected_group[0] -= 1;
                    orders.get(groupPosition).deselect();
                } else {
                    selected_group[0] += 1;
                    orders.get(groupPosition).select();
                }
                if(selected_group[0] == 0){
                    add_order_flag = true;
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Orders");
                } else{
                    add_order_flag = false;
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(selected_group[0] + " selected");
                }
                if(selected_group[0] == 1){
                    add_room_flag = true;
                    edit_order_flag = true;
                } else {
                    add_room_flag = false;
                    edit_order_flag = false;
                }
                if(selected_group[0] >= 1){
                    delete_order_flag = true;
                } else {
                    delete_order_flag = false;
                }
                adapter.notifyDataSetChanged();
                getActivity().invalidateOptionsMenu();
                return false;
            }
        });
        final int[] selected_child = {0};
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if(orders.get(groupPosition).rooms.get(childPosition).selected){
                    selected_child[0] -= 1;
                    orders.get(groupPosition).rooms.get(childPosition).deselect();
                } else {
                    selected_child[0] += 1;
                    orders.get(groupPosition).rooms.get(childPosition).select();
                }
                if(selected_child[0] > 0){
                    add_room_flag = false;
                } else {
                    add_room_flag = true;
                }
                if(selected_child[0] == 1){
                    edit_room_flag = true;
                } else {
                    edit_room_flag = false;
                }
                adapter.notifyDataSetChanged();
                getActivity().invalidateOptionsMenu();
                return false;
            }
        });

        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.action_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /* Click listeners for action bar. */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        MenuItem item = menu.findItem(R.id.action_add);
        if(add_order_flag){
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener (){
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    LayoutInflater inflater = getLayoutInflater();
                    final View mView = inflater.inflate(R.layout.dialog_new_order, null);
                    /* Spinner. */
                    final Spinner customer_spinner = mView.findViewById(R.id.customer_spinner);
                    database_helper db = new database_helper(getActivity());
                    Cursor c = db.get_customers(user_id);
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
                                        int month = simpleDatePicker.getMonth() + 1;
                                        int day = simpleDatePicker.getDayOfMonth();
                                        String due_date = Integer.toString(day) + "-" + Integer.toString(month) + "-" + Integer.toString(year);
                                        database_helper db = new database_helper(getActivity());
                                        if(db.create_order(user_id, customer_id, due_date, 0.0, false, selected.first_name + " " + selected.last_name, false) == 0){
                                            Toast.makeText(getContext(),"Order created successfully.", Toast.LENGTH_SHORT).show();
                                            Fragment fragment = new OrdersFragment();
                                            Bundle bundle = new Bundle();
                                            bundle.putString("USER_ID", user_id);
                                            fragment.setArguments(bundle);
                                            getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                                    fragment).commit();
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

        }

        if(add_room_flag){
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener (){
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    LayoutInflater inflater = getLayoutInflater();
                    final View mView = inflater.inflate(R.layout.dialog_new_room, null);
                    /* Spinner. */
                    final Spinner floor_spinner = mView.findViewById(R.id.floor_spinner);
                    database_helper db = new database_helper(getActivity());
                    Cursor c = db.get_floors(user_id);
                    final ArrayList<floor> floors = new ArrayList<>();
                    ArrayList<String> list = new ArrayList<>();
                    list.add("");
                    while(c.moveToNext()){
                        list.add(c.getString(c.getColumnIndex("FLOOR_NAME")));
                        floors.add(new floor(
                                c.getString(c.getColumnIndex("FLOOR_ID")),
                                c.getString(c.getColumnIndex("FLOOR_NAME")),
                                c.getDouble(c.getColumnIndex("FLOOR_COST"))));
                    }
                    c.close();
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), layout.simple_spinner_dropdown_item, list);
                    floor_spinner.setAdapter(adapter);
                        builder.setView(mView)
                                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        EditText size_input = mView.findViewById(R.id.room_size);
                                        String size = size_input.getText().toString();
                                        int p = floor_spinner.getSelectedItemPosition();
                                        if(p == 0) {
                                            Toast.makeText(getContext(), "Please select a floor", Toast.LENGTH_SHORT).show();
                                        } else if(size.isEmpty()){
                                            Toast.makeText(getContext(), "Please fill out all input fields.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            database_helper db = new database_helper(getActivity());
                                            for (order order : orders) {
                                                if(order.selected){
                                                    Double room_cost = Double.parseDouble(size) * floors.get(p - 1).cost * 1.2;
                                                    if(db.create_room(user_id,  Integer.parseInt(order.id), Integer.parseInt(floors.get(p - 1).id), Double.parseDouble(size), room_cost, false) == 0){
                                                        Toast.makeText(getContext(),"Room created successfully.", Toast.LENGTH_SHORT).show();
                                                        Fragment fragment = new OrdersFragment();
                                                        Bundle bundle = new Bundle();
                                                        bundle.putString("USER_ID", user_id);
                                                        fragment.setArguments(bundle);
                                                        getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                                                fragment).commit();
                                                    } else {
                                                        Toast.makeText(getContext(),"Something went wrong.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {    }
                                })
                                .setTitle("Add Room");
                        AlertDialog dialog = builder.create();
                        dialog.show();

                        return true;
                    }
                });

        }


        if(add_order_flag || add_room_flag) item.setVisible(true);
        else item.setVisible(false);

        item = menu.findItem(R.id.action_edit);

        if(edit_order_flag){
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener (){
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    LayoutInflater inflater = getLayoutInflater();
                    final View mView = inflater.inflate(R.layout.dialog_new_order, null);

                    /* Spinner. */
                    final Spinner customer_spinner = mView.findViewById(R.id.customer_spinner);
                    database_helper db = new database_helper(getActivity());
                    Cursor c = db.get_customers(user_id);
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

                    final DatePicker simpleDatePicker = mView.findViewById(R.id.simpleDatePicker); // initiate a date picker

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
                                    int month = simpleDatePicker.getMonth() + 1;
                                    int day = simpleDatePicker.getDayOfMonth();
                                    String due_date = Integer.toString(day) + "-" + Integer.toString(month) + "-" + Integer.toString(year);

                                    database_helper db = new database_helper(getActivity());

                                    for (final order order : orders) {
                                        if (order.selected) {

                                            if(db.update_order(order.id, customer_id, due_date,selected.first_name + " " + selected.last_name) == 1){
                                                Toast.makeText(getContext(),"Order updated successfully.", Toast.LENGTH_SHORT).show();

                                                Fragment fragment = new OrdersFragment();
                                                Bundle bundle = new Bundle();
                                                bundle.putString("USER_ID", user_id);
                                                fragment.setArguments(bundle);
                                                getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                                        fragment).commit();
                                            } else {
                                                Toast.makeText(getContext(),"Something went wrong.", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    }
                                }
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //Toast.makeText(getContext(),"cancel clicked",Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setTitle("Edit Order");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;
                }
            });
        }


        if(edit_room_flag){

            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener (){
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    LayoutInflater inflater = getLayoutInflater();
                    final View mView = inflater.inflate(R.layout.dialog_new_room, null);

                    /* Spinner. */
                    final Spinner floor_spinner = mView.findViewById(R.id.floor_spinner);
                    database_helper db = new database_helper(getActivity());
                    Cursor c = db.get_floors(user_id);
                    final ArrayList<floor> floors = new ArrayList<>();
                    ArrayList<String> list = new ArrayList<>();
                    list.add("");
                    while(c.moveToNext()){
                        list.add(c.getString(c.getColumnIndex("FLOOR_NAME")));
                        floors.add(new floor(
                                c.getString(c.getColumnIndex("FLOOR_ID")),
                                c.getString(c.getColumnIndex("FLOOR_NAME")),
                                c.getDouble(c.getColumnIndex("FLOOR_COST"))));
                    }
                    c.close();
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), layout.simple_spinner_dropdown_item, list);
                    floor_spinner.setAdapter(adapter);

                    builder.setView(mView)
                            .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    final database_helper db = new database_helper(getContext());
                                    EditText size_input = mView.findViewById(R.id.room_size);
                                    String size = size_input.getText().toString();
                                    int p = floor_spinner.getSelectedItemPosition();
                                    if(p == 0) {
                                        Toast.makeText(getContext(), "Please select a floor", Toast.LENGTH_SHORT).show();
                                    } else if(size.isEmpty()){
                                        Toast.makeText(getContext(), "Please fill out all input fields.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        for (order order : orders) {
                                            if(order.selected){
                                                for(room room : order.rooms){
                                                    if(room.selected){

                                                        Double room_cost = Double.parseDouble(size) * floors.get(p - 1).cost * 1.2;
                                                        if(db.update_room(room.id, Integer.parseInt(floors.get(p - 1).id), Double.parseDouble(size), room_cost) == 1){
                                                            Toast.makeText(getContext(),"Room updated successfully.", Toast.LENGTH_SHORT).show();
                                                            Fragment fragment = new OrdersFragment();
                                                            Bundle bundle = new Bundle();
                                                            bundle.putString("USER_ID", user_id);
                                                            fragment.setArguments(bundle);
                                                            getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                                                    fragment).commit();
                                                        } else {
                                                            Toast.makeText(getContext(),"Something went wrong.", Toast.LENGTH_SHORT).show();
                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {    }
                            })
                            .setTitle("Edit Room");
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    return true;
                }
            });

        }

        if(edit_order_flag || edit_room_flag) item.setVisible(true);
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

                                Boolean fail_flag = false;
                                final database_helper my_db = new database_helper(getContext());
                                for (order order : orders) {
                                    boolean selected_room_flag = false;
                                    if(order.selected){
                                        for(room room : order.rooms){
                                            if(room.selected){
                                                selected_room_flag = true;
                                                if(my_db.delete_room(room.id) < 1) {
                                                    fail_flag = true;
                                                    //break;
                                                }
                                            }
                                        }
                                        if(selected_room_flag == false){
                                            if(my_db.delete_order(order.id) < 1) {
                                                fail_flag = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if(fail_flag) {
                                    Toast.makeText(getContext(),"Something went wrong.",Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(),"Delete successful",Toast.LENGTH_SHORT).show();

                                    Fragment fragment = new OrdersFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("USER_ID", user_id);
                                    fragment.setArguments(bundle);
                                    getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                            fragment).commit();
                                }

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
