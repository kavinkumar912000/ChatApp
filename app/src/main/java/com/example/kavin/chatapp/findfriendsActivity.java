package com.example.kavin.chatapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class findfriendsActivity extends AppCompatActivity {
    private Toolbar mtoolbar;
    private RecyclerView findfriendsrecycler;
    private DatabaseReference userref;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findfriends);

        userref = FirebaseDatabase.getInstance().getReference().child("users");

        findfriendsrecycler =(RecyclerView) findViewById(R.id.findfriends_recyclelist);
        findfriendsrecycler.setLayoutManager(new LinearLayoutManager(this));
        mtoolbar =(Toolbar) findViewById(R.id.findfriends_toolbar);
        setSupportActionBar(mtoolbar) ;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<contacts> options=
                new FirebaseRecyclerOptions.Builder<contacts>()
                .setQuery(userref, contacts.class)
                .build();

        FirebaseRecyclerAdapter<contacts, FindFriendsViewHolder> adapter =
                new FirebaseRecyclerAdapter <contacts, FindFriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendsViewHolder Holder, final int position, @NonNull contacts model) {
                           Holder.username.setText(model.getName());
                           Holder.userstatus.setText(model.getStatus());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile).into(Holder.profileimage);


                        Holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                              String visit_user_id = getRef(position).getKey();

                                Intent profileintent = new Intent(findfriendsActivity.this,profile_activity.class);
                                profileintent.putExtra("visit_user_id",  visit_user_id);
                                startActivity(profileintent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        FindFriendsViewHolder viewholder=new FindFriendsViewHolder(view);
                        return viewholder;
                    }

                };
        findfriendsrecycler.setAdapter(adapter);

        adapter.startListening();
    }

    public static class FindFriendsViewHolder extends  RecyclerView.ViewHolder
    {
        TextView username,userstatus;
        CircleImageView profileimage;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            username =itemView.findViewById(R.id.user_profile_name);
            userstatus =itemView.findViewById(R.id.user_status);
            profileimage =itemView.findViewById(R.id.users_profile_image);

        }
    }
}
