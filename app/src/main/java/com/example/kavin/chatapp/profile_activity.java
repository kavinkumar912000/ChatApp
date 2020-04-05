package com.example.kavin.chatapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class profile_activity extends AppCompatActivity
{
    private String receiveruserid, senderserid, current_state;

    private CircleImageView userprofileimage;
    private TextView userprofilename,userprofilestatus;
    private Button sendmessagerequestbutton, Declinemessagerequestbutton;

    private DatabaseReference userref, chatrequestref, contactsref, notificationref;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_activity);

        mAuth =FirebaseAuth.getInstance();
        userref =FirebaseDatabase.getInstance().getReference().child("users");
        chatrequestref =FirebaseDatabase.getInstance().getReference().child("chat Requests");
        contactsref =FirebaseDatabase.getInstance().getReference().child("contacts");
        notificationref =FirebaseDatabase.getInstance().getReference().child("Notifications");

        senderserid =mAuth.getCurrentUser().getUid();
        receiveruserid =getIntent().getExtras().get("visit_user_id").toString();

        userprofileimage =(CircleImageView) findViewById(R.id.visit_profile_image);
        userprofilename =(TextView) findViewById(R.id.visit_user_name);
        userprofilestatus =(TextView) findViewById(R.id.visit_profile_status);
        sendmessagerequestbutton =(Button) findViewById(R.id.send_message_requesr_button);
        Declinemessagerequestbutton =(Button) findViewById(R.id.decline_message_requesr_button);
        current_state ="new";

        RetriveUserInfo();

    }

    private void RetriveUserInfo() {
        userref.child(receiveruserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               if((dataSnapshot.exists()) && (dataSnapshot.hasChild("image")))
               {
                   String userimage =dataSnapshot.child("image").getValue().toString();
                   String username =dataSnapshot.child("name").getValue().toString();
                   String userstatus =dataSnapshot.child("status").getValue().toString();

                   Picasso.get().load(userimage).placeholder(R.drawable.profile).into(userprofileimage);
                   userprofilename.setText(username);
                   userprofilestatus.setText(userstatus);

                   Managechatsrequest();
               }
               else
               {
                   String username =dataSnapshot.child("name").getValue().toString();
                   String userstatus =dataSnapshot.child("status").getValue().toString();


                   userprofilename.setText(username);
                   userprofilestatus.setText(userstatus);

                   Managechatsrequest();
               }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void Managechatsrequest()
    {
        chatrequestref.child(senderserid)
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild(receiveruserid))
                {
                    String request_type = dataSnapshot.child(receiveruserid).child("request_type").getValue().toString();

                    if (request_type.equals("sent"))
                    {
                        current_state ="request_sent";
                        sendmessagerequestbutton.setText("cancel Requset");
                    }
                    else if(request_type.equals("received"))
                    {
                        current_state ="request_received";
                        sendmessagerequestbutton.setText("Accept Request");

                        Declinemessagerequestbutton.setVisibility(View.VISIBLE);
                        Declinemessagerequestbutton.setEnabled(true);

                        Declinemessagerequestbutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                               cancelchatrequest();
                            }
                        });
                    }
                }
                else
                {
                    contactsref.child(senderserid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(receiveruserid)) {
                                        current_state = "friends";
                                        sendmessagerequestbutton.setText("Remove Friend");
                                    }
                                    }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        if (!senderserid.equals(receiveruserid))
        {
            sendmessagerequestbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   sendmessagerequestbutton.setEnabled(false);

                   if(current_state.equals("new"))
                   {
                       sendchatrequest();
                   }
                   if(current_state.equals("request_sent"))
                   {
                       cancelchatrequest();
                   }
                    if(current_state.equals("request_received"))
                    {
                        acceptchatrequest();
                    }
                    if(current_state.equals("friends"))
                    {
                        removespecificcontact();
                    }
                }
            });
        }
        else
        {
            sendmessagerequestbutton.setVisibility(View.INVISIBLE);
        }
    }

    private void removespecificcontact() {
        contactsref.child(senderserid).child(receiveruserid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            contactsref.child(receiveruserid).child(senderserid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                sendmessagerequestbutton.setEnabled(true);
                                                current_state ="new";
                                                sendmessagerequestbutton.setText("Send Request");

                                                Declinemessagerequestbutton.setVisibility(View.INVISIBLE);
                                                Declinemessagerequestbutton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptchatrequest() {
        contactsref.child(senderserid).child(receiveruserid)
                .child("contacts").setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                       if (task.isSuccessful())
                       {
                           contactsref.child(receiveruserid).child(senderserid)
                                   .child("contacts").setValue("saved")
                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           if (task.isSuccessful())
                                           {
                                                       chatrequestref.child(senderserid).child(receiveruserid)
                                                               .removeValue()
                                                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                   @Override
                                                                   public void onComplete(@NonNull Task<Void> task) {
                                                                       if (task.isSuccessful())
                                                                       {
                                                                           chatrequestref.child(receiveruserid).child(senderserid)
                                                                                   .removeValue()
                                                                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                       @Override
                                                                                       public void onComplete(@NonNull Task<Void> task) {
                                                                                                sendmessagerequestbutton.setEnabled(true);
                                                                                                current_state ="friends";
                                                                                                sendmessagerequestbutton.setText("Remove Friend");

                                                                                                Declinemessagerequestbutton.setVisibility(View.INVISIBLE);
                                                                                                Declinemessagerequestbutton.setEnabled(false);
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

    private void cancelchatrequest() {
        chatrequestref.child(senderserid).child(receiveruserid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                       if (task.isSuccessful())
                       {
                           chatrequestref.child(receiveruserid).child(senderserid)
                                   .removeValue()
                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                              if(task.isSuccessful())
                                              {
                                                  sendmessagerequestbutton.setEnabled(true);
                                                  current_state ="new";
                                                  sendmessagerequestbutton.setText("Send Request");

                                                  Declinemessagerequestbutton.setVisibility(View.INVISIBLE);
                                                  Declinemessagerequestbutton.setEnabled(false);
                                              }
                                       }
                                   });
                       }
                    }
                });
    }

    private void sendchatrequest() {
       chatrequestref.child(senderserid).child(receiveruserid)
               .child("request_type").setValue("sent")
               .addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        chatrequestref.child(receiveruserid).child(senderserid)
                                .child("request_type").setValue("received")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            HashMap<String, String> chatnotification = new HashMap<>();
                                            chatnotification.put("from", senderserid);
                                            chatnotification.put("type","request");

                                            notificationref.child(receiveruserid).push()
                                                    .setValue(chatnotification)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful())
                                                            {
                                                                sendmessagerequestbutton.setEnabled(true);
                                                                current_state = "request_sent";
                                                                sendmessagerequestbutton.setText("cancel request");
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
