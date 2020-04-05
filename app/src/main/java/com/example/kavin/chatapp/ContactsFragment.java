package com.example.kavin.chatapp;


import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kavin.chatapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    private View ContactsView;
    private RecyclerView mycontactsList;
    private DatabaseReference contactsref,usersref;
    private FirebaseAuth mAuth;
    private String currentuserid;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);
        mycontactsList = (RecyclerView) ContactsView.findViewById(R.id.contacts_list);
        mycontactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth =FirebaseAuth.getInstance();
        currentuserid =mAuth.getCurrentUser().getUid();

        contactsref =FirebaseDatabase.getInstance().getReference().child("contacts").child(currentuserid);
        usersref  =FirebaseDatabase.getInstance().getReference().child("users");

        return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options=
                new FirebaseRecyclerOptions.Builder<contacts>()
                .setQuery(contactsref, contacts.class)
                .build();

        FirebaseRecyclerAdapter<contacts, contactsviewholder> adapter =
                new FirebaseRecyclerAdapter<contacts, contactsviewholder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final contactsviewholder contactsviewholder, int i, @NonNull contacts contacts) {
                        String userids = getRef(i).getKey();
                        usersref.child(userids).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                if(dataSnapshot.exists())
                                {

                                    if (dataSnapshot.child("userstate").hasChild("state"))
                                    {
                                        String state=dataSnapshot.child("userstate").child("state").getValue().toString();
                                        String date=dataSnapshot.child("userstate").child("date").getValue().toString();
                                        String time=dataSnapshot.child("userstate").child("time").getValue().toString();

                                        if (state.equals("online"))
                                        {
                                            contactsviewholder.onlineIcon.setVisibility(View.VISIBLE);
                                        }

                                        else if (state.equals("offline"))
                                        {
                                            contactsviewholder.onlineIcon.setVisibility(View.INVISIBLE);
                                        }

                                    }
                                    else
                                    {
                                        contactsviewholder.onlineIcon.setVisibility(View.INVISIBLE);
                                    }

                                    if (dataSnapshot.hasChild("image"))
                                    {
                                        String userimage = dataSnapshot.child("image").getValue().toString();
                                        String profileName = dataSnapshot.child("name").getValue().toString();
                                        String profileStatus = dataSnapshot.child("status").getValue().toString();

                                        contactsviewholder.username.setText(profileName);
                                        contactsviewholder.userstatus.setText(profileStatus);
                                        Picasso.get().load(userimage).placeholder(R.drawable.profile_image).into(contactsviewholder.profileImage);
                                    }
                                    else
                                    {
                                        String profileName = dataSnapshot.child("name").getValue().toString();
                                        String profileStatus = dataSnapshot.child("status").getValue().toString();

                                        contactsviewholder.username.setText(profileName);
                                        contactsviewholder.userstatus.setText(profileStatus);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public contactsviewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        contactsviewholder viewholder =new contactsviewholder(view);
                        return viewholder;
                    }
                };
        mycontactsList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class contactsviewholder extends RecyclerView.ViewHolder
    {
        TextView username, userstatus;
        CircleImageView profileImage;
        ImageView onlineIcon;

        public contactsviewholder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.user_profile_name);
            userstatus  = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            onlineIcon =(ImageView) itemView.findViewById(R.id.user_online_status);
        }
    }
}
