package com.example.mtrade;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class CustomersFragment extends Fragment {

    boolean add_flag = true;
    boolean edit_flag = false;
    boolean delete_flag = false;

    ArrayList<customer> customers = new ArrayList<>();

    private String user_id;

    private static final String TAG =  "CustomersFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle args = getArguments();
        user_id = args.getString("USER_ID");

        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Customers");

        database_helper db = new database_helper(getActivity());

        final View view = inflater.inflate(R.layout.fragment_customers, container, false);

        Cursor c = db.get_customers(user_id);
        while(c.moveToNext()){
            customers.add(new customer(
                    c.getString(c.getColumnIndex("CUSTOMER_ID")),
                        c.getString(c.getColumnIndex("FIRST_NAME")),
                            c.getString(c.getColumnIndex("LAST_NAME")),
                                    c.getString(c.getColumnIndex("EMAIL_ADDRESS")),
                                            c.getString(c.getColumnIndex("MOBILE_NUMBER"))));

        }
        c.close();

        final ListView simpleList = view.findViewById(R.id.simpleListView);
        final CustomerAdapter cAdapter = new CustomerAdapter(getContext(), customers);
        simpleList.setAdapter(cAdapter);

        final int[] selected = {0};

        simpleList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(customers.get(position).selected){
                    selected[0] -= 1;
                    customers.get(position).deselect();
                } else {
                    selected[0] += 1;
                    customers.get(position).select();
                }

                if(selected[0] == 0){
                    add_flag = true;
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Customers");
                } else{
                    add_flag = false;
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(selected[0] + " selected");
                }

                if(selected[0] == 1){
                    edit_flag = true;
                } else {
                    edit_flag = false;
                }

                if(selected[0] >= 1){
                    delete_flag = true;
                } else {
                    delete_flag = false;
                }

                cAdapter.notifyDataSetChanged();
                getActivity().invalidateOptionsMenu();

            }
        });

        return view;

    }

    /* Inflate action menu. */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.action_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /* Set click event listeners for action menu buttons. */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        MenuItem item = menu.findItem(R.id.action_add);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener (){
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                LayoutInflater inflater = getLayoutInflater();
                final View mView = inflater.inflate(R.layout.dialog_new_customer, null);
                builder.setView(mView)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                EditText firstname_input = mView.findViewById(R.id.firstname);
                                String firstname = firstname_input.getText().toString();
                                EditText lastname_input = mView.findViewById(R.id.lastname);
                                String lastname = lastname_input.getText().toString();
                                EditText phone_input = mView.findViewById(R.id.mobile_number);
                                String phone = phone_input.getText().toString();
                                EditText email_input = mView.findViewById(R.id.email_address);
                                String email = email_input.getText().toString();

                                if(firstname.isEmpty() || lastname.isEmpty() || phone.isEmpty() || email.isEmpty()){
                                    Toast.makeText(getContext(),"Please fill out all input fields.", Toast.LENGTH_SHORT).show();
                                } else {
                                    final database_helper my_db = new database_helper(getContext());
                                    if(my_db.create_customer(firstname,lastname,phone, email, Integer.parseInt(user_id)) == 0){
                                        Toast.makeText(getContext(),"Customer created successfully.", Toast.LENGTH_SHORT).show();

                                        Fragment fragment = new CustomersFragment();
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
                                Toast.makeText(getContext(),"cancel clicked",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setTitle("Add Customer");
                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });
        if(add_flag) item.setVisible(true);
        else item.setVisible(false);

        item = menu.findItem(R.id.action_edit);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener (){
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                for (final customer customer : customers) {
                    if(customer.selected){

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        LayoutInflater inflater = getLayoutInflater();
                        final View mView = inflater.inflate(R.layout.dialog_new_customer, null);
                        final EditText firstname_input = mView.findViewById(R.id.firstname);
                        firstname_input.setText(customer.first_name);
                        final EditText lastname_input = mView.findViewById(R.id.lastname);
                        lastname_input.setText(customer.last_name);
                        EditText phone_input = mView.findViewById(R.id.mobile_number);
                        phone_input.setText(customer.phone_number);
                        EditText email_input = mView.findViewById(R.id.email_address);
                        email_input.setText(customer.email);
                        builder.setView(mView)
                                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        EditText firstname_input = mView.findViewById(R.id.firstname);
                                        String firstname = firstname_input.getText().toString();
                                        EditText lastname_input = mView.findViewById(R.id.lastname);
                                        String lastname = lastname_input.getText().toString();
                                        EditText phone_input = mView.findViewById(R.id.mobile_number);
                                        String phone = phone_input.getText().toString();
                                        EditText email_input = mView.findViewById(R.id.email_address);
                                        String email = email_input.getText().toString();

                                        if(firstname.isEmpty() || lastname.isEmpty() || phone.isEmpty() || email.isEmpty()){
                                            Toast.makeText(getContext(),"Please fill out all input fields.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            final database_helper my_db = new database_helper(getContext());
                                            if( my_db.update_customer(customer.id, firstname, lastname, phone, email) != 1){
                                                Toast.makeText(getContext(),"Something went wrong.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getContext(),"Update successful",Toast.LENGTH_SHORT).show();

                                                Fragment fragment = new CustomersFragment();
                                                Bundle bundle = new Bundle();
                                                bundle.putString("USER_ID", user_id);
                                                fragment.setArguments(bundle);
                                                getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                                        fragment).commit();
                                            }
                                        }
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Toast.makeText(getContext(),"cancel clicked",Toast.LENGTH_SHORT).show();
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
        if(edit_flag) item.setVisible(true);
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
                                for (customer customer : customers) {
                                    if(customer.selected){
                                        final database_helper my_db = new database_helper(getContext());
                                        if(my_db.delete_customer(customer.id) < 1) {
                                            fail_flag = true;
                                            break;
                                        }
                                    }
                                }
                                if(fail_flag) {
                                    Toast.makeText(getContext(),"Something went wrong.",Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(),"Delete successful",Toast.LENGTH_SHORT).show();

                                    Fragment fragment = new CustomersFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("USER_ID", user_id);
                                    fragment.setArguments(bundle);
                                    getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                            fragment).commit();
                                }

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
        if(delete_flag) item.setVisible(true);
        else item.setVisible(false);

        super.onPrepareOptionsMenu(menu);
    }
}