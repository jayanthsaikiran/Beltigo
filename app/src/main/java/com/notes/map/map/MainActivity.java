package com.notes.map.map;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {
//AIzaSyDPnIauzatIvqIiblrF1wlyID8XFiMCwUg

    public static final String TAG = "MainActivity";

    public static final int ERROR_DIALOG_REQUEST = 9001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(isServicesOK())
        {
            init();
        }
    }

    public void init()
    {
        Button bmp = (Button) findViewById(R.id.bmap);
        bmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
    }
    public boolean isServicesOK()
    {
        Log.v("Maps", "Checking Play Services");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS)
        {
            Log.v("Maps", "Play Services is Working");
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available))
        {
            Log.v("Maps", "We can fix it");
        }
        else
        {
            Log.v("Maps", "Sorry! Maps won't work on your device");
        }
        return false;
    }
}
