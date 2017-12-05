package com.example.vukhachoi.demo_foody;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Adapter.AdapterLocation;
import Adapter.Adapter_Res;
import Adapter.MyDatabaseAdapter;
import Model.Restaurant;

public class browser_restaurant extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    Toolbar toolbar1,toolbar2;
    ListView listView;
    int Posi;
    private GoogleApiClient client;

    private LocationRequest locationRequest;

    private android.location.Location lastlocation;

    private Marker currentLocationmMarker;
    public static GoogleMap map;

    double latitude = 0, longitude = 0;
    AdapterLocation arrayAdapter;
    public static int PROXIMITY_RADIUS = 1000;
    public static LatLng userLocation;
    public static final int REQUEST_LOCATION_CODE = 99;
    private LocationManager locationManager;
    boolean isGPSEnabled =false;
    boolean isNetworkEnabled =false;
    ArrayList<String> distance;
    ArrayAdapter<String> arrayAdapterdis;
    Spinner spinner;
    Adapter_Res adapter_res;
    ArrayList<Restaurant>  arrayList;
    private BottomSheetBehavior mBottomSheetBehavior;
    View bottomSheet ;
    private  int REQUESTCODE=123;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser_restaurant);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.myMap);
        mapFragment.getMapAsync(this);
        spinner=findViewById(R.id.spn);
        listView = findViewById(R.id.listview);
        arrayList = new ArrayList<>();

        arrayAdapter = new AdapterLocation(this, R.layout.item_restaurant, arrayList);

        listView.setAdapter(arrayAdapter);
        AddControl();
AddEvent();

    }
    @Override
    protected void onStart() {
        super.onStart();





        if (client!= null && !client.isConnected())
            client.connect();
    }

    public void restaurent(View view) {
        Object dataTransfer[] = new Object[4];

        GetNearbyRestaurantsData getNearbyPlacesData = new GetNearbyRestaurantsData();

        map.clear();

        String hospital = "restaurant";


        String url = getUrl(latitude, longitude, hospital);

        dataTransfer[0] = map;

        dataTransfer[1] = url;
        dataTransfer[2] = arrayAdapter;
        dataTransfer[3] = arrayList;
        Circle circle = map.addCircle(new CircleOptions()
                .center(new LatLng(latitude, longitude))
                .radius(PROXIMITY_RADIUS)
                .fillColor(0x550000FF).strokeColor(0x550000FF));




        getNearbyPlacesData.execute(dataTransfer);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));

        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }
    private void AddEvent() {

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        PROXIMITY_RADIUS = 1000;
                        restaurent(view);

                        break;
                    case 1:
                        PROXIMITY_RADIUS = 2000;
                        restaurent(view);
                        break;
                    case 2:
                        PROXIMITY_RADIUS = 5000;
                        restaurent(view);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public android.location.Location getLocation(){
        android.location.Location location=null;
        try{

            locationManager = (LocationManager) getApplication().getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);
            isNetworkEnabled=locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);

            if(ContextCompat.checkSelfPermission(getApplication(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(getApplication(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ){

                if(isGPSEnabled){
                    if(location==null){
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000,10, (LocationListener) this);
                        if(locationManager!=null){
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }
                // tim current location
                // if lcoation is not found from GPS than it will found from network //
                if(location==null){
                    if(isNetworkEnabled){

                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000,10,this);
                        if(locationManager!=null){
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }

                    }
                }

            }

        }catch(Exception ex){

        }
        return  location;
    }
    public boolean checkLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            } else
            {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        } else
            return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng sushi1 = new LatLng(10.780407, 106.6916509);
        map = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

            map.setMyLocationEnabled(true);
            return;

        }
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setMyLocationEnabled(true);
        getLocation();
        latitude=getLocation().getLatitude();
        longitude=getLocation().getLongitude();


        userLocation=new LatLng(latitude,longitude);
        map.addMarker(new MarkerOptions()
                .title("YEN SUSHI PREMIUM ")
                .snippet("123 Bà Huyện Thanh Quan, Q.3, HCM\n" +
                        "Điện thoại:  028 39 318 828\n")
                .position(new LatLng(10.781213, 106.682021))
                );
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                sushi1, 15));

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Lỗi permission", Toast.LENGTH_LONG).show();
            return;
        }
        UpdateRes();
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {


            @Override
            public boolean onMyLocationButtonClick() {


                UpdateRes();
                return false;
            }
        });
        final int[] i = {0};
        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

            @Override
            public void onMyLocationChange(android.location.Location location) {

                latitude = location.getLatitude();
                longitude = location.getLongitude();
              
            }


        });

    }



    void UpdateRes() {



        restaurent(null);
        userLocation=new LatLng(latitude,longitude);

    }



    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode)

        {

            case REQUEST_LOCATION_CODE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)

                {

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)

                    {

                        if (client == null)

                        {

                            bulidGoogleApiClient();

                        }

                        map.setMyLocationEnabled(true);

                    }

                } else

                {

                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();

                }

        }

    }

    private String getUrl(double latitude, double longitude, String nearbyPlace)

    {


        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");

        googlePlaceUrl.append("location=" + latitude + "," + longitude);

        googlePlaceUrl.append("&radius=" + PROXIMITY_RADIUS);

        googlePlaceUrl.append("&type=" + nearbyPlace);

        googlePlaceUrl.append("&sensor=true");

        googlePlaceUrl.append("&key=" + "AIzaSyABLune_lERG5qC-CmY4wlY0nM5RuCJ4vs");


        Log.d("MapsActivity", "url = " + googlePlaceUrl.toString());


        return googlePlaceUrl.toString();

    }


//    void AddData()
//    {
//
//        MyDatabaseAdapter myDatabase;
//        SQLiteDatabase database;
//        myDatabase= new MyDatabaseAdapter(this);
//        myDatabase.Khoitai();
//        database=myDatabase.getMyDatabase();
//
//        Cursor cursor = database.rawQuery("select * from Restaurant", null);
//        cursor.moveToFirst();
//        while (!cursor.isAfterLast())
//        {
//            String name=cursor.getString(1);
//            String restype=cursor.getString(7);
//            String address=cursor.getString(4);
//            byte[] hinhanh = cursor.getBlob(3);
//            arrayList.add(new Restaurant(name,address,restype,hinhanh));
//            cursor.moveToNext();
//        }
//        cursor.close();
//
//
//    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void AddControl() {
        bottomSheet = findViewById( R.id.bottom_sheet );
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setPeekHeight(150);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        TranslateAnimation animation = new TranslateAnimation(0.0f, 0.0f,
                200f, 0.0f);          //  new TranslateAnimation(xFrom,xTo, yFrom,yTo)
        animation.setDuration(2000);  // animation duration
        animation.setRepeatCount(0);  // animation repeat count
        animation.setRepeatMode(0);   // repeat animation (left to right, right to left )
        animation.setFillAfter(true);

        bottomSheet.startAnimation(animation);
        { Window window = this.getWindow();
            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
            window.setStatusBarColor(getResources().getColor(R.color.my_color));
//       getSupportActionBar().hide();
        }
        toolbar1 = findViewById(R.id.tool_back1);
        toolbar1.setNavigationIcon(R.drawable.navi);
        toolbar1.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                finish();
                return true;
            }
        });
        toolbar2=findViewById(R.id.tool_fav1);
        toolbar2.inflateMenu(R.menu.search);

        toolbar2.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
//                Toast.makeText(Restaurant_Details.this,"fav",Toast.LENGTH_LONG).show();
                Intent intent=new Intent(browser_restaurant.this,location_chooser.class);
                startActivityForResult(intent,REQUESTCODE);
                return true;
            }
        });





        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent=new Intent(browser_restaurant.this,Restaurant_Details.class);
                intent.putExtra("ChooseLocation",arrayList.get(i));
                Toast.makeText(browser_restaurant.this,arrayList.get(i).getAddress()+arrayList.get(i).getTitle()+arrayList.get(i).getLati()+arrayList.get(i).getLongti()+"" , Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        });




        distance = new ArrayList<>();
        distance.add("1km");
        distance.add("2km");
        distance.add("5km");
        arrayAdapterdis = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, distance);
        spinner.setAdapter(arrayAdapterdis);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==REQUESTCODE&&resultCode==RESULT_OK)
        {
            try {
                arrayList.clear();
                    ArrayList<Restaurant> arrayListResult = (ArrayList<Restaurant>) data.getSerializableExtra("arraylist");
                    for (Restaurant i : arrayListResult) {
                        arrayList.add(i);
                    adapter_res.notifyDataSetChanged();
                }
            }catch (Exception e){};
        }else {
            arrayList.clear();

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected synchronized void bulidGoogleApiClient() {

        client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();

        client.connect();


    }

    @Override
    public void onLocationChanged(android.location.Location location) {


        latitude = location.getLatitude();

        longitude = location.getLongitude();



        lastlocation = location;
        userLocation=new LatLng(latitude,longitude);
        if (currentLocationmMarker != null)

        {

            currentLocationmMarker.remove();


        }






        if (client != null)

        {

            LocationServices.FusedLocationApi.removeLocationUpdates(client, (com.google.android.gms.location.LocationListener) this);

        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

        startActivity(i);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {


        locationRequest = new LocationRequest();

        locationRequest.setInterval(100);

        locationRequest.setFastestInterval(1000);

        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        android.location.Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
        if (mLastLocation != null) {
            //place marker at current position
            //mGoogleMap.clear();
            latitude=mLastLocation.getLatitude();
            longitude= mLastLocation.getLongitude();
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(latitude,longitude));
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            map.addMarker(markerOptions);
            userLocation=new LatLng(latitude,longitude);
            Toast.makeText(this, latitude+""+longitude+"", Toast.LENGTH_SHORT).show();
        }




        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED)

        {

            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, (com.google.android.gms.location.LocationListener) this);

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}