package com.example.kavin.chatapp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class Groupchatactivity extends AppCompatActivity {
    private Toolbar mtoolbar;
    private ImageButton sentmsgbutton;
    private EditText usermsginput;
    private ScrollView mscroolview;
    private TextView displaytextmsgs;
    private DatabaseReference userref,groupnameref,groupmessagekeyref;

    private FirebaseAuth mAuth;
    private String currentGroupName,currentuserid,currentusername,currentdate,currenttime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupchatactivity);

        currentGroupName =getIntent().getExtras().get("groupName").toString();

        mAuth =FirebaseAuth.getInstance();
        currentuserid =mAuth.getCurrentUser().getUid();
        userref =FirebaseDatabase.getInstance().getReference().child("users");
        groupnameref =FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);



        Initializefields();

        getuserinfo();

        sentmsgbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              savemsginfotodatabase();

              usermsginput.setText("");

              mscroolview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

    @Override
    protected void onStart() {
         super.onStart();
         groupnameref.addChildEventListener(new ChildEventListener() {
             @Override
             public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                 if(dataSnapshot.exists())
                 {
                     DisplayMessages(dataSnapshot);
                 }
             }

             @Override
             public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                 if(dataSnapshot.exists())
                 {
                     DisplayMessages(dataSnapshot);
                 }
             }

             @Override
             public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

             }

             @Override
             public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });
    }

    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator =dataSnapshot.getChildren().iterator();
        while(iterator.hasNext())
        {
            String chatdate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatmessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatname = (String) ((DataSnapshot)iterator.next()).getValue();
            String chattime = (String) ((DataSnapshot)iterator.next()).getValue();

            displaytextmsgs.append(chatname + ":\n" +"    "+chatmessage +"\n" +chattime +"    "+chatdate+"\n\n\n");

            mscroolview.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }


    private void savemsginfotodatabase() {
        String message = usermsginput.getText().toString();
        String messagekey =groupnameref.push().getKey();

        if (TextUtils.isEmpty(message))
        {
            Toast.makeText(Groupchatactivity.this,"write your  message first...!",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calfordate = Calendar.getInstance();
            SimpleDateFormat currentdataformat =new SimpleDateFormat("MMM dd,YYYY");
            currentdate = currentdataformat.format(calfordate.getTime());

            Calendar calforTime = Calendar.getInstance();
            SimpleDateFormat currentTimeformat=new SimpleDateFormat("hh:mm a");
            currenttime = currentTimeformat.format(calforTime.getTime());

            HashMap<String, Object> groupmessageKey =new HashMap<>();
            groupnameref.updateChildren(groupmessageKey);

            groupmessagekeyref =groupnameref.child(messagekey);
            HashMap<String, Object> messageInfomap =new HashMap<>();
            messageInfomap.put("name", currentusername);
            messageInfomap.put("message", message);
            messageInfomap.put("date", currentdate);
            messageInfomap.put("time", currenttime);
            groupmessagekeyref.updateChildren(messageInfomap);
        }
    }

    private void getuserinfo() {
        userref.child(currentuserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    currentusername = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void Initializefields() {
        mtoolbar =(Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(currentGroupName);

        sentmsgbutton =(ImageButton) findViewById(R.id.send_message_button);
        usermsginput =(EditText)findViewById(R.id.input_group_message);
        displaytextmsgs =(TextView) findViewById(R.id.groupchat_text_display);
        mscroolview =(ScrollView) findViewById(R.id.my_scrool_view);
    }

}
