package com.example.kavin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.kavin.chatapp.R;
import com.squareup.picasso.Picasso;

public class ImageViewer extends AppCompatActivity {

    private ImageView imageview;
    private String imageurl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        imageview =findViewById(R.id.imageviewer);
        imageurl =getIntent().getStringExtra("url");

        Picasso.get().load(imageurl).into(imageview);
    }
}
