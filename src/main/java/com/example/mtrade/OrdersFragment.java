package com.example.mtrade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OrdersFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);



        ExpandableListView expandableListView = view.findViewById(R.id.expandableListView);

        HashMap<String, List<String>> item = new HashMap<>();

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

        ExpandableListAdapter adapter = new ExpandableListAdapter(item);
        expandableListView.setAdapter(adapter);


        return view;
    }
}
