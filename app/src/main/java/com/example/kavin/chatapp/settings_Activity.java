package com.example.kavin.chatapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class settings_Activity extends AppCompatActivity {
    private Button update;
    private EditText username, userstatus;
    private CircleImageView userprofile;
    private String currentuserid;
    private FirebaseAuth mAuth;
    private DatabaseReference rootref;

    private static final int GalleryPick=1;
    private StorageReference userProfileImageref;
    private ProgressDialog loadingbar;

    private Toolbar SettingsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_);


        mAuth = FirebaseAuth.getInstance();
        currentuserid = mAuth.getCurrentUser().getUid();
        rootref = FirebaseDatabase.getInstance().getReference();
        userProfileImageref =FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatesettings();
            }
        });
        Retriveuserinfo();

        userprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent =new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });
    }

    @SuppressLint("NewApi")
    private void InitializeFields() {
        update = (Button) findViewById(R.id.update_settings);
        username = (EditText) findViewById(R.id.profile_user_name);
        userstatus = (EditText) findViewById(R.id.profile_status);
        userprofile = (CircleImageView) findViewById(R.id.profile_image);

        loadingbar =new ProgressDialog(this);
        SettingsToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if(requestCode == GalleryPick && resultCode ==RESULT_OK && data!=null)
            {
                Uri Imageuri =data.getData();
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(settings_Activity.this);
            }

            if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE )
            {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode ==RESULT_OK)
                {

                    loadingbar.setTitle("set Profile Image..!");
                    loadingbar.setMessage("Please wait, your Profile Image is updated..!");
                    loadingbar.setCanceledOnTouchOutside(false);
                    loadingbar.show();

                    Uri resultUri=result.getUri();

                    StorageReference filepath=userProfileImageref.child(currentuserid + ".jpg");

                    filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(settings_Activity.this, "profile updated successfully...!", Toast.LENGTH_LONG).show();

                                final String downloadurl = task.getResult().getDownloadUrl().toString();

                                rootref.child("users").child(currentuserid).child("image")
                                        .setValue(downloadurl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                               if (task.isSuccessful())
                                               {
                                                   loadingbar.dismiss();
                                               }
                                               else
                                               {
                                                   String msg=task.getException().toString();
                                                   Toast.makeText(settings_Activity.this, "Error:" +msg, Toast.LENGTH_SHORT).show();
                                                   loadingbar.dismiss();
                                               }
                                            }
                                        });
                            }
                            else
                            {
                                String msg=task.getException().toString();
                                Toast.makeText(settings_Activity.this, "Error:" +msg, Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                        }
                    });
                }
            }
    }

    private void updatesettings() {
        String setusername = username.getText().toString();
        String setstatus = userstatus.getText().toString();

        if (TextUtils.isEmpty(setusername)) {
            Toast.makeText(settings_Activity.this, "please write your username...!", Toast.LENGTH_SHORT).show();
        }


        if (TextUtils.isEmpty(setstatus)) {
            Toast.makeText(this, "please write something on your status...!", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentuserid);
            profileMap.put("name", setusername);
            profileMap.put("status", setstatus);
            rootref.child("users").child(currentuserid).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                SendUserMainActivity();
                                Toast.makeText(settings_Activity.this, "profile updated successfully...!", Toast.LENGTH_SHORT).show();
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(settings_Activity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }
    }



    private void Retriveuserinfo() {
        rootref.child("users").child(currentuserid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image"))))
                        {
                            String retriveusername=dataSnapshot.child("name").getValue().toString();
                            String retrivestatus=dataSnapshot.child("status").getValue().toString();
                            String retriveprofileImage=dataSnapshot.child("image").getValue().toString();
                            username.setText(retriveusername);
                            userstatus.setText(retrivestatus);
                            Picasso.get().load(retriveprofileImage).into(userprofile);
                        } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                        {

                            String retriveusername=dataSnapshot.child("name").getValue().toString();
                            String retrivestatus=dataSnapshot.child("status").getValue().toString();
                            username.setText(retriveusername);
                            userstatus.setText(retrivestatus);
                        }
                        else
                        {
                            Toast.makeText(settings_Activity.this, "please set and update your profile...!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {


                    }
                });
    }

    private void SendUserMainActivity() {
        Intent settingsIntent = new Intent(settings_Activity.this, MainActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
        finish();
    }
}
