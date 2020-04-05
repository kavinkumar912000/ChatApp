package com.example.kavin.chatapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kavin.chatapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
public class Requestfragment extends Fragment {
    private View RequestFragmentView;
    private RecyclerView myrecyclerlist;
    private DatabaseReference chatrequestref,userRef,contactsref;
    private FirebaseAuth mAuth;
    private String currentuserId;


    public Requestfragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestFragmentView = inflater.inflate(R.layout.fragment_requestfragment, container, false);

        mAuth =FirebaseAuth.getInstance();
        currentuserId =mAuth.getCurrentUser().getUid();

        contactsref =FirebaseDatabase.getInstance().getReference().child("contacts");
        chatrequestref =FirebaseDatabase.getInstance().getReference().child("chat Requests");
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        myrecyclerlist = (RecyclerView) RequestFragmentView.findViewById(R.id.chat_request_list);
        myrecyclerlist.setLayoutManager(new LinearLayoutManager(getContext()));
        return RequestFragmentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<contacts> options =
                new FirebaseRecyclerOptions.Builder<contacts>()
                .setQuery(chatrequestref.child(currentuserId), contacts.class)
                .build();

        FirebaseRecyclerAdapter<contacts, RequestViewHolder> adapter =
                new FirebaseRecyclerAdapter<contacts, RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestViewHolder requestViewHolder, int i, @NonNull contacts contacts) {
                         requestViewHolder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.VISIBLE);
                        requestViewHolder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.VISIBLE);

                        final String list_user_id =getRef(i).getKey();

                        DatabaseReference gettyperef = getRef(i).child("request_type").getRef();

                        gettyperef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists())
                                {
                                    String type = dataSnapshot.getValue().toString();
                                    if(type.equals("received"))
                                    {
                                        userRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild("image"))
                                                {

                                                    final String requestProfileImage = dataSnapshot.child("image").getValue().toString();

                                                    Picasso.get().load(requestProfileImage).into(requestViewHolder.profileImage);
                                                }
                                                    final String requestusername = dataSnapshot.child("name").getValue().toString();
                                                    final String requestuserstatus = dataSnapshot.child("status").getValue().toString();

                                                    requestViewHolder.username.setText(requestusername);
                                                    requestViewHolder.userstatus.setText("wants to connects with you....!");

                                                requestViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        CharSequence options[] = new CharSequence[]
                                                                {
                                                                        "Accept",
                                                                        "Cancel"
                                                                };
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle(requestusername +" Chat Request....!");
                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                  if (which == 0)
                                                                  {
                                                                    contactsref.child(currentuserId).child(list_user_id).child("contacts")
                                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                contactsref.child(list_user_id).child(currentuserId).child("contacts")
                                                                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if(task.isSuccessful())
                                                                                        {
                                                                                            chatrequestref.child(currentuserId).child(list_user_id)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if(task.isSuccessful())
                                                                                                            {
                                                                                                                chatrequestref.child(list_user_id).child(currentuserId)
                                                                                                                        .removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                if(task.isSuccessful())
                                                                                                                                {
                                                                                                                                    Toast.makeText(getContext(),"New Contact Saved...!",Toast.LENGTH_SHORT).show();

                                                                                                                                }
                                                                                                                            }
                                                                                                                        });
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                    });
                                                                  }
                                                                  if( which == 1)
                                                                  {
                                                                      chatrequestref.child(currentuserId).child(list_user_id)
                                                                              .removeValue()
                                                                              .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                  @Override
                                                                                  public void onComplete(@NonNull Task<Void> task) {
                                                                                      if(task.isSuccessful())
                                                                                      {
                                                                                          chatrequestref.child(list_user_id).child(currentuserId)
                                                                                                  .removeValue()
                                                                                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                      @Override
                                                                                                      public void onComplete(@NonNull Task<Void> task) {
                                                                                                          if(task.isSuccessful())
                                                                                                          {
                                                                                                              Toast.makeText(getContext(),"Contact deleted..!",Toast.LENGTH_SHORT).show();

                                                                                                          }
                                                                                                      }
                                                                                                  });
                                                                                      }
                                                                                  }
                                                                              });
                                                                  }
                                                            }
                                                        });
                                                        builder.show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }

                                    else if(type.equals("sent"))
                                    {
                                        Button request_sent_btn =requestViewHolder.itemView.findViewById(R.id.request_accept_button);
                                        request_sent_btn.setText("Request sent");

                                        requestViewHolder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.INVISIBLE);

                                        userRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild("image"))
                                                {

                                                    final String requestProfileImage = dataSnapshot.child("image").getValue().toString();

                                                    Picasso.get().load(requestProfileImage).into(requestViewHolder.profileImage);
                                                }
                                                final String requestusername = dataSnapshot.child("name").getValue().toString();
                                                final String requestuserstatus = dataSnapshot.child("status").getValue().toString();

                                                requestViewHolder.username.setText(requestusername);
                                                requestViewHolder.userstatus.setText("you sent request to " +requestusername);

                                                requestViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        CharSequence options[] = new CharSequence[]
                                                                {
                                                                        "Cancel"
                                                                };
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle("Already sent Request....!");
                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {

                                                                if( which == 0)
                                                                {
                                                                    chatrequestref.child(currentuserId).child(list_user_id)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        chatrequestref.child(list_user_id).child(currentuserId)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if(task.isSuccessful())
                                                                                                        {
                                                                                                            Toast.makeText(getContext(),"you have cancelled the chat request..!",Toast.LENGTH_SHORT).show();

                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                                        builder.show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

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
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false );
                        RequestViewHolder holder =new RequestViewHolder(view);
                        return holder;
                    }
                };

        myrecyclerlist.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {

        TextView username,userstatus;
        CircleImageView profileImage;
        Button Acceptbutton,CancelButton;


        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.user_profile_name);
            userstatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            Acceptbutton = itemView.findViewById(R.id.request_accept_button);
            CancelButton = itemView.findViewById(R.id.request_cancel_button);

        }
    }
}
