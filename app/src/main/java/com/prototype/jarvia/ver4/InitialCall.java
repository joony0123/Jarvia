package com.prototype.jarvia.ver4;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class InitialCall extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_initial_call2);

       startService(new Intent(getBaseContext(), InitialBackgroundService.class));
       // startService(new Intent(getBaseContext(), OnClearFromRecentService.class));

//        Intent jarvis = new Intent(getApplicationContext(), PocketSphinxActivity.class);
//        jarvis.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(jarvis);


        finish();
    }


}
