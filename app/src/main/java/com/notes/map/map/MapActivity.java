package com.notes.map.map;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MapActivity extends AppCompatActivity {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private Boolean mLocationPermissionGranted = false;

    private GoogleMap mMap;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 17f;


    Location currentLocation;

    String desplace;

    LatLng des;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        getLocationPermission();
        if (mLocationPermissionGranted) {
            getDeviceLocation();

        }

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                desplace = place.getName().toString();
                Log.i("Map", "Place: " + place.getName());
               String sample = place.getAddress().toString();
               des = place.getLatLng();

                Log.v("Map", sample);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Map", "An error occurred: " + status);
            }
        });

        Button navi = (Button) findViewById(R.id.nav);
        navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+des.latitude+","+des.longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });


    }



    private void initMap() {
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                Log.v("Map", "Entered initMap");
                Toast.makeText(MapActivity.this, "Attack", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Get the current location
    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {

            final com.google.android.gms.tasks.Task<Location> location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<Location> task) {
                    if (task.isSuccessful()) {
                        currentLocation = location.getResult();
                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                    }
                }
            });

        } catch (SecurityException e) {
            Log.v("Map", "Security Exception" + e);
        }
    }

    //Camera Zoom
    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        //For disabling location button
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
    }


    private void  getLocationPermission()
    {
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION , Manifest.permission.CAMERA};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                mLocationPermissionGranted=true;
                initMap();
            }
        }
        else {
            ActivityCompat.requestPermissions(this, permissions ,LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode)
        {
            case LOCATION_PERMISSION_REQUEST_CODE:
            {
                if(grantResults.length>0){
                    for(int i=0; i<grantResults.length;i++)
                    {
                        if(grantResults[i]!=PackageManager.PERMISSION_GRANTED)
                        {
                            mLocationPermissionGranted=false;
                            return;
                        }
                    }
                    mLocationPermissionGranted=true;
                    //Open Map
                    initMap();
                }
            }
        }
    }

    //For marking the destination address
    public void destination(View v)
    {
//        Geocoder geocoder = new Geocoder(this);
//        try
//        {
//            List<Address> myList = geocoder.getFromLocationName(desplace, 5);
//            Address address = myList.get(0);
//            Log.v("Map" , address.toString());
//            String locality = address.getLocality();
//            Log.v("Map" , locality);
//            double lat =  address.getLatitude();
//            double lon = address.getLongitude();
//            goToLocation(lat, lon , 18);
//
//            MarkerOptions markerOptions = new MarkerOptions();
//            markerOptions.title(locality);
//            markerOptions.position(new LatLng(lat,lon));
//            mMap.addMarker(markerOptions);
//
//        }catch (IOException e)
//        {
//            e.printStackTrace();
//        }
        double lat = des.latitude;
        double lon = des.longitude;
        goToLocation(des.latitude , des.longitude , 18);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(lat,lon));
        mMap.addMarker(markerOptions);



        //For getting the direction
        String url = requestUrl(des);
        TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
        taskRequestDirections.execute(url);


    }

    //Moves the camera to the location which is selected by the user
    public  void goToLocation(double latitude , double longitude , int zoom)
    {
            LatLng latLng1 = new LatLng(latitude,longitude);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng1 , zoom);
            mMap.moveCamera(cameraUpdate);
    }


    //Kiran
    private String requestUrl(LatLng des) {
        //Taking default origin location temporarily...

        String origin = "origin="+currentLocation.getLatitude()+","+currentLocation.getLongitude();
        String des1 ="destination="+des.latitude+","+des.longitude;
        String sensor ="sensor=false";
        String mode ="mode=driving";
        String param = origin+"&"+des1+"&"+sensor+"&"+mode;
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+param;
        return url;
    }



    public class TaskRequestDirections extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {
            String responseString = null;

            try {
                responseString = requestDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String,Void,List <List<HashMap<String,String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject object = null;
            List<List<HashMap<String, String>>> routes =null;
            try {
                object = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(object);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            super.onPostExecute(lists);

            ArrayList points = null;
            PolylineOptions options = null;

            for( List<HashMap<String, String>> path: lists){
                points = new ArrayList();
                options = new PolylineOptions();

                for( HashMap<String, String> point: path){

                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat,lon));
                }

                options.addAll(points);
                options.width(12);
                options.color(R.color.colorPrimaryDark);
                options.geodesic(true);
            }
            if(options!=null)
                mMap.addPolyline(options);
            else
                Toast.makeText(getApplicationContext(),"Directions not found :(",Toast.LENGTH_SHORT).show();
        }
    }


    private String requestDirection(String reqUrl) throws IOException {

        String responseString = null;
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;


        try {
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line= "";
            StringBuffer stringBuffer = new StringBuffer();

            while((line=bufferedReader.readLine())!=null){
                stringBuffer.append(line);
            }
            responseString = stringBuffer.toString();
            inputStreamReader.close();
            bufferedReader.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(inputStream!=null)
                inputStream.close();
            httpURLConnection.disconnect();
        }
        return responseString;
    }


}
