package com.prototype.jarvia.ver4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Joon.Y.K on 2017-04-20.
 */

public class BReceiver extends BroadcastReceiver{



    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Toast.makeText(context, "TESTER", Toast.LENGTH_SHORT).show();

      //  ((MainActivity)context).tester();
      //  specificMethod();


    }
}
