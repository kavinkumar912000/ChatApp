package com.example.kavin.chatapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatActivity extends AppCompatActivity {

    private String messageReceiverId, messageReceivername,messagereceiverimage,messagesenderid;

    private TextView username,userlastseen;
    private CircleImageView userimage;
    private Toolbar Chattoolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference rootref;

    private ImageButton sendmessageButton,sendfilebutton;
    private EditText messageinputtext;

    private final List<messages> messageslist = new ArrayList<>();
    private LinearLayoutManager linearlayoutmanager;
    private messageAdapter messageAdapter;
    private RecyclerView Usermessageslist;

    private String savecurrrenttime,savecurrentdate;
    private String checker="", myUrl="";
    private StorageTask uploadTask;
    private Uri fileuri;
    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messagesenderid =mAuth.getCurrentUser().getUid();
        rootref = FirebaseDatabase.getInstance().getReference();

        messageReceiverId =getIntent().getExtras().get("Visit_user_id").toString();
        messageReceivername =getIntent().getExtras().get("Visit_user_name").toString();
        messagereceiverimage  =getIntent().getExtras().get("Visit_user_image").toString();

        InitializeControlles();

        username.setText(messageReceivername);
        Picasso.get().load(messagereceiverimage).placeholder(R.drawable.profile).into(userimage);

        sendmessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendmessage();
            }
        });

        rootref.child("Messages").child(messagesenderid).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        messages messages = dataSnapshot.getValue(messages.class);

                        messageslist.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        Usermessageslist.smoothScrollToPosition(Usermessageslist.getAdapter().getItemCount());

                        DisplayLastseen();
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        DisplayLastseen();

        sendfilebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               CharSequence options[] = new CharSequence[]
                       {
                         "Images",
                         "PDF Files",
                         "Docs"
                       };
                AlertDialog.Builder builder = new AlertDialog.Builder(chatActivity.this);
                builder.setTitle("Select File..!");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if (i == 0)
                        {
                            checker ="image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select Image"),438);
                        }
                        if(i == 1)
                        {
                          checker ="pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent,"Select PDF file"),438);
                        }
                        if(i == 2)
                        {
                            checker ="docx";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/msword");
                            startActivityForResult(intent.createChooser(intent,"Select MS Word File"),438);
                        }
                    }
                });
                builder.show();
            }
        });
    }

    private void InitializeControlles() {

        Chattoolbar =(Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(Chattoolbar);

        ActionBar actionbar =getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarView = layoutInflater.inflate(R.layout.custum_chat_bar,null);
        actionbar.setCustomView(actionbarView);

        userimage =(CircleImageView) findViewById(R.id.custum_profile_image);
        username =(TextView) findViewById(R.id.custum_profile_name);
        userlastseen = (TextView) findViewById(R.id.custum_user_last_seen);

        sendmessageButton =(ImageButton) findViewById(R.id.send_msg_btn);
        sendfilebutton =(ImageButton) findViewById(R.id.send_files_btn);
        messageinputtext =(EditText) findViewById(R.id.input_message);

        messageAdapter =new messageAdapter(messageslist);
        Usermessageslist = (RecyclerView) findViewById(R.id.private_msg_list_of_users);
        linearlayoutmanager = new LinearLayoutManager(this);
        Usermessageslist.setLayoutManager(linearlayoutmanager);
        Usermessageslist.setAdapter(messageAdapter);

        loadingbar = new ProgressDialog(chatActivity.this);

        Calendar calender =Calendar.getInstance();

        SimpleDateFormat currentdate = new SimpleDateFormat("MMM dd,YYYY");
        savecurrentdate = currentdate.format(calender.getTime());

        SimpleDateFormat currenttime = new SimpleDateFormat("hh:mm a");
        savecurrrenttime = currenttime.format(calender.getTime());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
         super.onActivityResult(requestCode, resultCode, data );

         if(requestCode==438 && resultCode ==RESULT_OK && data!=null && data.getData()!=null)
         {

             loadingbar.setTitle("Sending File..!");
             loadingbar.setMessage("Please wait,we are sending..!");
             loadingbar.setCanceledOnTouchOutside(false);
             loadingbar.show();

             fileuri = data.getData();

             if (!checker.equals("image"))
             {
                 StorageReference storagereference =FirebaseStorage.getInstance().getReference().child("Document Files");

                 final String messagesenderref ="Messages/" + messagesenderid + "/" + messageReceiverId;
                 final String messagereceiverref ="Messages/" + messageReceiverId + "/" + messagesenderid;

                 DatabaseReference usermessagekeyref = rootref.child("Messages")
                         .child(messagesenderid).child(messageReceiverId).push();

                 final String messagepushid =usermessagekeyref.getKey();

                 final StorageReference filePath= storagereference.child(messagepushid +"." +checker);

                 filePath.putFile(fileuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                     @Override
                     public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                         if(task.isSuccessful())
                         {
                             Map messagetextbody = new HashMap();
                             messagetextbody.put("message",task.getResult().getDownloadUrl().toString());
                             messagetextbody.put("name",fileuri.getLastPathSegment());
                             messagetextbody.put("type",checker);
                             messagetextbody.put("from",messagesenderid);
                             messagetextbody.put("to",messageReceiverId);
                             messagetextbody.put("messageID",messagepushid);
                             messagetextbody.put("time",savecurrrenttime);
                             messagetextbody.put("date",savecurrentdate);

                             Map messagebodydetail = new HashMap();
                             messagebodydetail.put(messagesenderref + "/" + messagepushid, messagetextbody);
                             messagebodydetail.put(messagereceiverref + "/" + messagepushid, messagetextbody);

                             rootref.updateChildren(messagebodydetail);
                             loadingbar.dismiss();
                         }
                     }
                 }).addOnFailureListener(new OnFailureListener() {
                     @Override
                     public void onFailure(@NonNull Exception e) {
                         loadingbar.dismiss();
                         Toast.makeText(chatActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

                     }
                 }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                     @Override
                     public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                         double p=(100.0*taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                         loadingbar.setMessage((int) p +"% Uploading....");
                     }
                 });

             }
             else if(checker.equals("image"))
             {
                 StorageReference storagereference =FirebaseStorage.getInstance().getReference().child("Image Files");

                 final String messagesenderref ="Messages/" + messagesenderid + "/" + messageReceiverId;
                 final String messagereceiverref ="Messages/" + messageReceiverId + "/" + messagesenderid;

                 DatabaseReference usermessagekeyref = rootref.child("Messages")
                         .child(messagesenderid).child(messageReceiverId).push();

                 final String messagepushid =usermessagekeyref.getKey();

                 final StorageReference filePath= storagereference.child(messagepushid +"." +"jpg");

                 uploadTask = filePath.putFile(fileuri);

                 uploadTask.continueWithTask(new Continuation() {
                     @Override
                     public Object then(@NonNull Task task) throws Exception {
                         if(!task.isSuccessful())
                         {
                             throw task.getException();
                         }
                         return filePath.getDownloadUrl();
                     }
                 }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                     @Override
                     public void onComplete(@NonNull Task<Uri> task) {
                         if(task.isSuccessful())
                         {
                             Uri downloadurl =task.getResult();
                             myUrl = downloadurl.toString();

                             Map messagetextbody = new HashMap();
                             messagetextbody.put("message",myUrl);
                             messagetextbody.put("name",fileuri.getLastPathSegment());
                             messagetextbody.put("type",checker);
                             messagetextbody.put("from",messagesenderid);
                             messagetextbody.put("to",messageReceiverId);
                             messagetextbody.put("messageID",messagepushid);
                             messagetextbody.put("time",savecurrrenttime);
                             messagetextbody.put("date",savecurrentdate);

                             Map messagebodydetail = new HashMap();
                             messagebodydetail.put(messagesenderref + "/" + messagepushid, messagetextbody);
                             messagebodydetail.put(messagereceiverref + "/" + messagepushid, messagetextbody);

                             rootref.updateChildren(messagebodydetail).addOnCompleteListener(new OnCompleteListener() {
                                 @Override
                                 public void onComplete(@NonNull Task task) {
                                     if(task.isSuccessful())
                                     {
                                         loadingbar.dismiss();
                                         Toast.makeText(chatActivity.this,"Message sent Successfully...!",Toast.LENGTH_SHORT).show();
                                     }
                                     else
                                     {
                                         loadingbar.dismiss();
                                         Toast.makeText(chatActivity.this,"Error",Toast.LENGTH_SHORT).show();
                                     }

                                     messageinputtext.setText("");
                                 }
                             });
                         }
                     }
                 }) ;

             }
             else
             {
                 loadingbar.dismiss();
                 Toast.makeText(this,"Nothing Selected,Error.",Toast.LENGTH_SHORT).show();
             }
         }
    }

    private void DisplayLastseen()
    {
        rootref.child("users").child(messageReceiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("userstate").hasChild("state"))
                        {
                            String state=dataSnapshot.child("userstate").child("state").getValue().toString();
                            String date=dataSnapshot.child("userstate").child("date").getValue().toString();
                            String time=dataSnapshot.child("userstate").child("time").getValue().toString();

                            if (state.equals("online"))
                            {
                                userlastseen.setText("online");
                            }

                            else if (state.equals("offline"))
                            {
                                userlastseen.setText("Last Seen: " + date +"  " +time);
                            }

                        }
                        else
                        {
                            userlastseen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    @Override
    protected void onStart() {
         super.onStart();
        {

        }
    }

    private void sendmessage()
    {
        String messagetext = messageinputtext.getText().toString();

        if(TextUtils.isEmpty(messagetext))
        {
            Toast.makeText(chatActivity.this,"write message first..!",Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messagesenderref ="Messages/" + messagesenderid + "/" + messageReceiverId;
            String messagereceiverref ="Messages/" + messageReceiverId + "/" + messagesenderid;

            DatabaseReference usermessagekeyref = rootref.child("Messages")
                    .child(messagesenderid).child(messageReceiverId).push();

            String messagepushid =usermessagekeyref.getKey();

            Map messagetextbody = new HashMap();
            messagetextbody.put("message",messagetext);
            messagetextbody.put("type","text");
            messagetextbody.put("from",messagesenderid);
            messagetextbody.put("to",messageReceiverId);
            messagetextbody.put("messageID",messagepushid);
            messagetextbody.put("time",savecurrrenttime);
            messagetextbody.put("date",savecurrentdate);

            Map messagebodydetail = new HashMap();
            messagebodydetail.put(messagesenderref + "/" + messagepushid, messagetextbody);
            messagebodydetail.put(messagereceiverref + "/" + messagepushid, messagetextbody);

            rootref.updateChildren(messagebodydetail).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                       if(task.isSuccessful())
                       {
                           Toast.makeText(chatActivity.this,"Message sent Successfully...!",Toast.LENGTH_SHORT).show();
                       }
                       else
                       {
                           Toast.makeText(chatActivity.this,"Error",Toast.LENGTH_SHORT).show();
                       }

                       messageinputtext.setText("");
                }
            });
        }
    }
}
