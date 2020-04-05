package com.example.kavin.chatapp;


import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
public class chatsFragment extends Fragment {
    private View privatechatsview;
    private RecyclerView chatslist;
    private DatabaseReference chatsreference,usersref;
    private FirebaseAuth mAuth;
    private String currentuserid;


    public chatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privatechatsview = inflater.inflate(R.layout.fragment_chats2, container, false);


        mAuth=FirebaseAuth.getInstance();
        currentuserid =mAuth.getCurrentUser().getUid();
        chatsreference =FirebaseDatabase.getInstance().getReference().child("contacts").child(currentuserid);

        chatslist = (RecyclerView) privatechatsview.findViewById(R.id.chats_list);
        chatslist.setLayoutManager(new LinearLayoutManager(getContext()));
        usersref = FirebaseDatabase.getInstance().getReference().child("users");


        return privatechatsview;
    }
    public static class chatsViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView profileImage;
        TextView userstatus,username;


        public chatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage =itemView.findViewById(R.id.users_profile_image);
            username =itemView.findViewById(R.id.user_profile_name);
            userstatus=itemView.findViewById(R.id.user_status);
        }
    }


    @Override
    public void onStart() {
         super.onStart();

        FirebaseRecyclerOptions<contacts> options =new FirebaseRecyclerOptions.Builder<contacts>()
           .setQuery(chatsreference, contacts.class)
           .build();


        FirebaseRecyclerAdapter<contacts, chatsViewHolder> adapter=
                new FirebaseRecyclerAdapter<contacts, chatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final chatsViewHolder chatsViewHolder, int i, @NonNull contacts contacts) {
                         final String userids = getRef(i).getKey();
                        final String[] retimage = {"default_image"};

                         usersref.child(userids).addValueEventListener(new ValueEventListener() {
                             @Override
                             public void onDataChange(DataSnapshot dataSnapshot) {
                               if(dataSnapshot.exists())
                               {
                                   if(dataSnapshot.hasChild("image"))
                                   {
                                       retimage[0] =dataSnapshot.child("image").getValue().toString();
                                       Picasso.get().load(retimage[0]).into(chatsViewHolder.profileImage);
                                   }
                                   final String retname = dataSnapshot.child("name").getValue().toString();
                                   final String retstatus = dataSnapshot.child("status").getValue().toString();

                                   chatsViewHolder.username.setText(retname);

                                   if (dataSnapshot.child("userstate").hasChild("state"))
                                   {
                                       String state=dataSnapshot.child("userstate").child("state").getValue().toString();
                                       String date=dataSnapshot.child("userstate").child("date").getValue().toString();
                                       String time=dataSnapshot.child("userstate").child("time").getValue().toString();

                                       if (state.equals("online"))
                                       {
                                           chatsViewHolder.userstatus.setText("online");
                                       }

                                       else if (state.equals("offline"))
                                       {
                                           chatsViewHolder.userstatus.setText("Last Seen: " + date +"  " +time);
                                       }

                                   }
                                   else
                                   {
                                       chatsViewHolder.userstatus.setText("offline");
                                   }


                                   chatsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                         Intent chatIntent = new Intent(getContext(), chatActivity.class);
                                         chatIntent.putExtra("Visit_user_id", userids);
                                           chatIntent.putExtra("Visit_user_name", retname);
                                           chatIntent.putExtra("Visit_user_image", retimage[0]);
                                         startActivity(chatIntent);
                                       }
                                   });
                               }
                         }

                             @Override
                             public void onCancelled(DatabaseError databaseError) {

                             }
                         });

                    }

                    @NonNull
                    @Override
                    public chatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                       View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup ,false );
                       return new chatsViewHolder(view);
                    }
                };
        chatslist.setAdapter(adapter);
        adapter.startListening();
    }


}
