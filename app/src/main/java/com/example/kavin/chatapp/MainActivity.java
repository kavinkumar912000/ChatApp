package com.example.kavin.chatapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ViewPager myViewpager;
    private TabLayout mytablayout;
    private TabsAccessAdapter mytabsaccessadapter;
    private FirebaseAuth mAuth;
    private DatabaseReference rootref;
    private String currentuserid;
    private FirebaseUser fb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth =FirebaseAuth.getInstance();

        rootref =FirebaseDatabase.getInstance().getReference();

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("JUSCHAT");

        myViewpager = (ViewPager) findViewById(R.id.main_tabs_paper);
        mytabsaccessadapter =new TabsAccessAdapter(getSupportFragmentManager() );
        myViewpager.setAdapter(mytabsaccessadapter);

        mytablayout = (TabLayout) findViewById(R.id.main_tabs);
        mytablayout.setupWithViewPager(myViewpager);





    }

    @Override
    protected void onStart()
    {
         super.onStart();

         FirebaseUser currentuser =mAuth.getCurrentUser();
         if (currentuser == null)
         {
             SendUserToLoginActivity();
         }
         else
         {
             updateuserstatus("online");

             Verifyuserexistance();
         }
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentuser =mAuth.getCurrentUser();


         if (currentuser != null)
         {
             updateuserstatus("offline");
         }
    }


    @Override
    protected void onDestroy() {
         super.onDestroy();

        FirebaseUser currentuser =mAuth.getCurrentUser();

        if (currentuser != null)
        {
            updateuserstatus("offline");
        }

    }

    private void Verifyuserexistance() {
        String currentuserid =mAuth.getCurrentUser().getUid();

        rootref.child("users").child(currentuserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("name").exists()))
                {
                }
                else
                {
                    SendUserTosettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }) ;
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent= new Intent(MainActivity.this,loginActivity.class);
        loginIntent .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserfindfriendsactivity() {
        Intent findfriendsIntent = new Intent(MainActivity.this, findfriendsActivity.class);
        startActivity(findfriendsIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
     super.onCreateOptionsMenu(menu);
     getMenuInflater().inflate(R.menu.options_menu, menu);
     return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         if (item.getItemId() ==R.id.main_Logout)
         {
             updateuserstatus("offline");
             mAuth.signOut();
             SendUserToLoginActivity();
         }
        if (item.getItemId() ==R.id.main_settings)
        {
                SendUserTosettingsActivity();
        }
        if (item.getItemId() ==R.id.main_create_group)
        {
            Requestnewgroup();
        }
        if (item.getItemId() ==R.id.main_find_friends)
        {
            SendUserfindfriendsactivity();
        }
      /*  if (item.getItemId() ==R.id.main_Share)
        {
            ShareActionProvider.setShareIntent.
        }*/
        return true;

    }
  /*  public void share()
    {
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("application/vnd.android.package-archive");

        ApplicationInfo api=getApplicationContext().getApplicationInfo();
        String apkPath=api.sourceDir;

        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(apkPath)));
        startActivity(Intent.createChooser(intent, "Share app using...."));
    }*/



    private void Requestnewgroup() {
        AlertDialog.Builder builder =new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group name...!");
        final EditText groupNamefield =new EditText(MainActivity.this);
        builder.setView(groupNamefield);
        builder.setPositiveButton("create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                 String groupname =groupNamefield.getText().toString();
                 if(TextUtils.isEmpty(groupname))
                 {
                     Toast.makeText(MainActivity.this, "please write group name...!",Toast.LENGTH_SHORT).show();
                 }
                 else
                 {
                     Createnewgroup(groupname);
                 }

            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.cancel();
            }
        }) ;
        builder.show();
    }

    private void Createnewgroup(final String groupname) {
        rootref.child("Groups").child(groupname).setValue("")
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, groupname  + " group is created successfully..",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    String message=task.getException().toString();
                    Toast.makeText(MainActivity.this, "error:" +message,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendUserTosettingsActivity()
    {
        Intent settingsIntent= new Intent(MainActivity.this,settings_Activity.class);
        startActivity(settingsIntent);
    }

    private void updateuserstatus(String state)
    {
        String savecurrrenttime,savecurrentdate;
        Calendar calender =Calendar.getInstance();

        SimpleDateFormat currentdate = new SimpleDateFormat("MMM dd,YYYY");
        savecurrentdate = currentdate.format(calender.getTime());

        SimpleDateFormat currenttime = new SimpleDateFormat("hh:mm a");
        savecurrrenttime = currenttime.format(calender.getTime());

        HashMap <String, Object> onlinestate = new HashMap<>();
        onlinestate.put("time", savecurrrenttime);
        onlinestate.put("date", savecurrentdate);
        onlinestate.put("state", state);

        currentuserid = mAuth.getCurrentUser().getUid();

        rootref.child("users").child(currentuserid).child("userstate")
                .updateChildren(onlinestate);

    }

}
