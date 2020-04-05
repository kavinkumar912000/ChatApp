package com.example.kavin.chatapp;

import android.app.AlertDialog;
import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kavin.ImageViewer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class messageAdapter extends RecyclerView.Adapter<messageAdapter.messageviewholder>
{
    private List<messages> usermessageslist;
    private FirebaseAuth mAuth;
    private DatabaseReference usersref;

    public messageAdapter(List<messages> usermessageslist)

    {
        this.usermessageslist =usermessageslist;
    }

    public class messageviewholder extends RecyclerView.ViewHolder
    {
        public TextView sendermessagetext,receivermessagetext;
        public CircleImageView receiverprofileimage;
        public ImageView messagesenderpicture,messagereceiverpicture;


        public messageviewholder(@NonNull View itemView) {
            super(itemView);

            sendermessagetext =(TextView) itemView.findViewById(R.id.sender_messages_text);
            receivermessagetext =(TextView) itemView.findViewById(R.id.receiver_messages_text);
            receiverprofileimage =(CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messagereceiverpicture=itemView.findViewById(R.id.message_receiver_image_view);
            messagesenderpicture=itemView.findViewById(R.id.message_sender_image_view);
        }
    }

    @NonNull
    @Override
    public messageviewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth =FirebaseAuth.getInstance();

        return new messageviewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final messageviewholder messageviewholder, final int i) {
        String messagesenderid = mAuth.getCurrentUser().getUid();
        messages message =usermessageslist.get(i);

        String fromuserid = message.getFrom();
        String frommessagetype = message.getType();

        usersref = FirebaseDatabase.getInstance().getReference().child("users").child(fromuserid);

        usersref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("image"))
                {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile).into(messageviewholder.receiverprofileimage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        messageviewholder.receivermessagetext.setVisibility(View.GONE);
        messageviewholder.receiverprofileimage.setVisibility(View.GONE);
        messageviewholder.sendermessagetext.setVisibility(View.GONE);
        messageviewholder.messagesenderpicture.setVisibility(View.GONE);
        messageviewholder.messagereceiverpicture.setVisibility(View.GONE);



        if(frommessagetype.equals("text"))
        {

            if(fromuserid.equals(messagesenderid))
            {
                messageviewholder.sendermessagetext.setVisibility(View.VISIBLE);
                messageviewholder.sendermessagetext.setBackgroundResource(R.drawable.sender_message_layout);
                messageviewholder.sendermessagetext.setTextColor(Color.BLACK);
                messageviewholder.sendermessagetext.setText(message.getMessage() + "\n\n" + message.getTime() + " - " + message.getDate());
            }
            else
            {

                messageviewholder.receiverprofileimage.setVisibility(View.VISIBLE);
                messageviewholder.receivermessagetext.setVisibility(View.VISIBLE);

                messageviewholder.receivermessagetext.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageviewholder.receivermessagetext.setTextColor(Color.BLACK);
                messageviewholder.receivermessagetext.setText(message.getMessage() + "\n\n" + message.getTime() + " - " + message.getDate());


            }
        }
        else  if(frommessagetype.equals("image"))
        {
            if(fromuserid.equals(messagesenderid))
            {
                messageviewholder.messagesenderpicture.setVisibility(View.VISIBLE);

                Picasso.get().load(message.getMessage()).into(messageviewholder.messagesenderpicture);
            }
            else
            {
                messageviewholder.receiverprofileimage.setVisibility(View.VISIBLE);
                messageviewholder.messagereceiverpicture.setVisibility(View.VISIBLE);

                Picasso.get().load(message.getMessage()).into(messageviewholder.messagereceiverpicture);
            }
        }
        else if(frommessagetype.equals("pdf") || frommessagetype.equals("docx"))
        {
            if(fromuserid.equals(messagesenderid))
            {
                messageviewholder.messagesenderpicture.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatapp-bae3f.appspot.com/o/Image%20Files%2Ffile.jpg?alt=media&token=7ac500f6-5401-4de0-a548-bccb53010d37").into(messageviewholder.messagesenderpicture);


            }
            else
            {
                messageviewholder.receiverprofileimage.setVisibility(View.VISIBLE);
                messageviewholder.messagereceiverpicture.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatapp-bae3f.appspot.com/o/Image%20Files%2Ffile.jpg?alt=media&token=7ac500f6-5401-4de0-a548-bccb53010d37").into(messageviewholder.messagereceiverpicture);


            }
        }


        if(fromuserid.equals(messagesenderid))
        {
            messageviewholder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                      if(usermessageslist.get(i).getType().equals("pdf") || usermessageslist.get(i).getType().equals("docx") )
                      {
                          CharSequence options[]=new CharSequence[]
                                  {
                                          "Delete for me",
                                          "Download and view This Document",
                                          "cancel",
                                          "Delete for Everyone"
                                  };
                          AlertDialog.Builder builder=new AlertDialog.Builder(messageviewholder.itemView.getContext());
                          builder.setTitle("Delete Message..?");

                          builder.setItems(options, new DialogInterface.OnClickListener() {
                              @Override
                              public void onClick(DialogInterface dialog, int i) {
                                  if(i==0)
                                  {
                                      deletesentmessages(i,messageviewholder);
                                  }
                                  else if(i==1)
                                  {
                                      Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse(usermessageslist.get(i).getMessage()));
                                      messageviewholder.itemView.getContext().startActivity(intent);
                                  }

                                  else if(i==3)
                                  {
                                      deletemessagesforeveryone(i,messageviewholder);
                                  }
                              }
                          });
                          builder.show();
                      }
                    else if(usermessageslist.get(i).getType().equals("text"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "cancel",
                                        "Delete for Everyone"
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(messageviewholder.itemView.getContext());
                        builder.setTitle("Delete Message..?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i==0)
                                {
                                    deletesentmessages(i,messageviewholder);
                                    Intent intent =new Intent(messageviewholder.itemView.getContext(), MainActivity.class);
                                    messageviewholder.itemView.getContext().startActivity(intent);
                                }
                                else if(i==2)
                                {
                                    deletemessagesforeveryone(i,messageviewholder);
                                    Intent intent =new Intent(messageviewholder.itemView.getContext(), MainActivity.class);
                                    messageviewholder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }
                    else if(usermessageslist.get(i).getType().equals("image"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "View This Image",
                                        "cancel",
                                        "Delete for Everyone"
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(messageviewholder.itemView.getContext());
                        builder.setTitle("Delete Message..?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i==0)
                                {
                                    deletesentmessages(i,messageviewholder);
                                    Intent intent =new Intent(messageviewholder.itemView.getContext(), MainActivity.class);
                                    messageviewholder.itemView.getContext().startActivity(intent);
                                }
                                else if(i==1)
                                {
                                    Intent intent =new Intent(messageviewholder.itemView.getContext(), ImageViewer.class);
                                    intent.putExtra("url",usermessageslist.get(i).getMessage());
                                    messageviewholder.itemView.getContext().startActivity(intent);
                                }
                                else if(i==3)
                                {
                                    deletemessagesforeveryone(i,messageviewholder);
                                    Intent intent =new Intent(messageviewholder.itemView.getContext(), MainActivity.class);
                                    messageviewholder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
        else
        {
            messageviewholder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(usermessageslist.get(i).getType().equals("pdf") || usermessageslist.get(i).getType().equals("docx") )
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "Download and view This Document",
                                        "cancel"
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(messageviewholder.itemView.getContext());
                        builder.setTitle("Delete Message..?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i==0)
                                {
                                    deletereceivemessages(i,messageviewholder);
                                    Intent intent =new Intent(messageviewholder.itemView.getContext(), MainActivity.class);
                                    messageviewholder.itemView.getContext().startActivity(intent);
                                }
                                else if(i==1)
                                {
                                    Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse(usermessageslist.get(i).getMessage()));
                                    messageviewholder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if(usermessageslist.get(i).getType().equals("text"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "cancel"
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(messageviewholder.itemView.getContext());
                        builder.setTitle("Delete Message..?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i==0)
                                {
                                    deletereceivemessages(i,messageviewholder);

                                    Intent intent =new Intent(messageviewholder.itemView.getContext(), MainActivity.class);
                                    messageviewholder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if(usermessageslist.get(i).getType().equals("image"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete for me",
                                        "View This Image",
                                        "cancel"
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder(messageviewholder.itemView.getContext());
                        builder.setTitle("Delete Message..?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i==0)
                                {
                                    deletereceivemessages(i,messageviewholder);
                                    Intent intent =new Intent(messageviewholder.itemView.getContext(), MainActivity.class);
                                    messageviewholder.itemView.getContext().startActivity(intent);
                                }
                                else if(i==1)
                                {
                                   Intent intent =new Intent(messageviewholder.itemView.getContext(), ImageViewer.class);
                                   intent.putExtra("url",usermessageslist.get(i).getMessage());
                                   messageviewholder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return usermessageslist.size();
    }


    private void deletesentmessages(final int i,final messageviewholder holder)
    {
        DatabaseReference rootref = FirebaseDatabase.getInstance().getReference();
        rootref.child("Messages")
                .child(usermessageslist.get(i).getFrom())
                .child(usermessageslist.get(i).getTo())
                .child(usermessageslist.get(i).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(),"Deleteed Successfully",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(),"Error Occured..!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deletereceivemessages(final int i,final messageviewholder holder)
    {
        DatabaseReference rootref = FirebaseDatabase.getInstance().getReference();
        rootref.child("Messages")
                .child(usermessageslist.get(i).getTo())
                .child(usermessageslist.get(i).getFrom())
                .child(usermessageslist.get(i).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(),"Deleteed Successfully",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(),"Error Occured..!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deletemessagesforeveryone(final int i,final messageviewholder holder)
    {
        final DatabaseReference rootref = FirebaseDatabase.getInstance().getReference();
        rootref.child("Messages")
                .child(usermessageslist.get(i).getTo())
                .child(usermessageslist.get(i).getFrom())
                .child(usermessageslist.get(i).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    rootref.child("Messages")
                            .child(usermessageslist.get(i).getFrom())
                            .child(usermessageslist.get(i).getTo())
                            .child(usermessageslist.get(i).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(holder.itemView.getContext(),"Deleteed Successfully",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(),"Error Occured..!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
