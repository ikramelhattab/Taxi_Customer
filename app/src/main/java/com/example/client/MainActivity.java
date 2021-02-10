package com.example.client;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static  int taxi_SCREEN = 1000;
    Animation topAnim,bottomAnim;
    ImageView image;
    TextView logo,slogo;
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //Aniamtion
//topAnim= AnimationUtils.loadAnimation(this,R.drawable.top_animation);
//bottomAnim= AnimationUtils.loadAnimation(this,R.drawable.bottom_animation);

//hooks
        image =findViewById(R.id.imageView);
        logo =findViewById(R.id.nameapp);
        slogo =findViewById(R.id.v);

        image.setAnimation(topAnim);
        logo.setAnimation(bottomAnim);
        slogo.setAnimation(bottomAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this,Login.class);
                Pair[] pairs = new Pair[2];
                pairs[0]=new Pair<View,String>(image,"logo_image");
                pairs[1]=new Pair<View,String>(image,"logo_text");
                ActivityOptions options =ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,pairs);
                startActivity(intent,options.toBundle());
                //   finish();

            }
        }, taxi_SCREEN );

    }
}