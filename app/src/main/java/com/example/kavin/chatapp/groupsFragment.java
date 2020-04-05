package com.example.kavin.chatapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class groupsFragment extends Fragment
{
    private View groupfragmentview;
    private ListView list_view;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String>list_of_groups =new ArrayList<>();
    private DatabaseReference Groupsref;


    public groupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        groupfragmentview = inflater.inflate(R.layout.fragment_groups, container, false);

        Groupsref  = FirebaseDatabase.getInstance().getReference().child("Groups");
        Initializefields();

        Retriveanddisplaygroups();

        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentgroupname = parent.getItemAtPosition(position).toString();

                Intent groupchatIntent =new Intent(getContext(), Groupchatactivity.class);
                groupchatIntent.putExtra("groupName", currentgroupname);
                startActivity(groupchatIntent);
            }
        });

        return groupfragmentview;
    }

    private void Retriveanddisplaygroups() {
        Groupsref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set=new HashSet<>();
                Iterator iterator =dataSnapshot.getChildren().iterator();

                while (iterator.hasNext())
                {
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }
                list_of_groups.clear();
                list_of_groups.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void Initializefields() {
        list_view =(ListView)groupfragmentview.findViewById(R.id.list_view);
        arrayAdapter =new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, list_of_groups);
        list_view.setAdapter(arrayAdapter);
    }

}
