package com.example.mtrade;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class FloorsFragment extends Fragment {

    boolean add_flag = true;
    boolean edit_flag = false;
    boolean delete_flag = false;



    ArrayList<floor> floors = new ArrayList<>();

    private static final String TAG =  "FloorsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Floor Types");

        database_helper db = new database_helper(getActivity());
        final View view = inflater.inflate(R.layout.fragment_floors, container, false);

        Cursor c = db.get_floors("1");
        while(c.moveToNext()){
            floors.add(new floor(
                    c.getString(c.getColumnIndex("FLOOR_ID")),
                    c.getString(c.getColumnIndex("FLOOR_NAME")),
                    c.getDouble(c.getColumnIndex("FLOOR_COST"))));
        }
        c.close();

        final ListView simpleList = view.findViewById(R.id.simpleListView);
        final FloorAdapter fAdapter = new FloorAdapter(getContext(), floors);
        simpleList.setAdapter(fAdapter);

        final int[] selected = {0};

        simpleList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(floors.get(position).selected){
                    selected[0] -= 1;
                    floors.get(position).deselect();
                } else {
                    selected[0] += 1;
                    floors.get(position).select();
                }

                if(selected[0] == 0){
                    add_flag = true;
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Floor Types");
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

                fAdapter.notifyDataSetChanged();
                getActivity().invalidateOptionsMenu();

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

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        MenuItem item = menu.findItem(R.id.action_add);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener (){
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                LayoutInflater inflater = getLayoutInflater();
                final View mView = inflater.inflate(R.layout.dialog_new_floor, null);
                builder.setView(mView)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                EditText name_input = mView.findViewById(R.id.name);
                                String name = name_input.getText().toString();
                                EditText cost_input = mView.findViewById(R.id.cost);
                                String cost = cost_input.getText().toString();

                                if(name.isEmpty() || cost.isEmpty()){
                                    Toast.makeText(getContext(),"Please fill out all input fields.", Toast.LENGTH_SHORT).show();
                                } else {
                                    database_helper db = new database_helper(getActivity());
                                    if(db.create_floor(name, cost, 1) == 0){
                                        Toast.makeText(getContext(),"Floor created successfully.", Toast.LENGTH_SHORT).show();
                                        getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                                new FloorsFragment()).commit();
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
                        .setTitle("Add Floor");
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

                for (final floor floor : floors) {
                    if(floor.selected){

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        LayoutInflater inflater = getLayoutInflater();
                        final View mView = inflater.inflate(R.layout.dialog_new_floor, null);
                        final EditText firstname_input = mView.findViewById(R.id.name);
                        firstname_input.setText(floor.name);
                        final EditText lastname_input = mView.findViewById(R.id.cost);
                        lastname_input.setText(floor.cost.toString());

                        builder.setView(mView)
                                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        EditText name_input = mView.findViewById(R.id.name);
                                        String name = name_input.getText().toString();
                                        EditText cost_input = mView.findViewById(R.id.cost);
                                        String cost = cost_input.getText().toString();

                                        if(name.isEmpty() || cost.isEmpty()){
                                            Toast.makeText(getContext(),"Please fill out all input fields.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            final database_helper my_db = new database_helper(getContext());
                                            if( my_db.update_floor(floor.id, name, cost) != 1){
                                                Toast.makeText(getContext(),"Something went wrong.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getContext(),"Update successful",Toast.LENGTH_SHORT).show();
                                                getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                                        new FloorsFragment()).commit();
                                            }
                                        }
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Toast.makeText(getContext(),"cancel clicked",Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setTitle("Edit Floor");
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
                                for (floor floor : floors) {
                                    if(floor.selected){
                                        final database_helper my_db = new database_helper(getContext());
                                        if(my_db.delete_floor(floor.id) < 1) {
                                            fail_flag = true;
                                            break;
                                        }
                                    }
                                }
                                if(fail_flag) {
                                    Toast.makeText(getContext(),"Something went wrong.",Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(),"Delete successful",Toast.LENGTH_SHORT).show();
                                    getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                            new FloorsFragment()).commit();
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
        if(delete_flag) item.setVisible(true);
        else item.setVisible(false);

        super.onPrepareOptionsMenu(menu);
    }

}
