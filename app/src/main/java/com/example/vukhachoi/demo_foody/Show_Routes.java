package com.example.vukhachoi.demo_foody;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
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



public class Show_Routes extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    public static GoogleMap map;
    private LocationManager locationManager;
    boolean isGPSEnabled =false;
    boolean isNetworkEnabled =false;
    double latitude = 0, longitude = 0;
    public static LatLng userLocation;
    Toolbar toolbar1,toolbar2;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show__routes);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)

        {

            checkLocationPermission();


        }
        createMap();
        AddControl();
        AddStatus();

    }

    private void AddControl() {
        toolbar1=findViewById(R.id.tool_back);
        toolbar1.inflateMenu(R.menu.back_detail);

        toolbar1.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                finish();
                return true;
            }
        });


        toolbar2=findViewById(R.id.tool_fav);
        toolbar2.inflateMenu(R.menu.favourites_details);

        toolbar2.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(Show_Routes.this,"fav",Toast.LENGTH_LONG).show();
                return true;
            }
        });
    }

    private void createMap() {
        SupportMapFragment smf = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        smf.getMapAsync(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        createMap();
    }
    LatLng latLng;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            map = googleMap;
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                map.setMyLocationEnabled(true);
                return;

            }
            map.getUiSettings().setMyLocationButtonEnabled(true);
            map.setMyLocationEnabled(true);


            latitude = getLocation().getLatitude();
            longitude = getLocation().getLongitude();
            userLocation = new LatLng(latitude, longitude);


            String name=getIntent().getStringExtra("title");
            String address=getIntent().getStringExtra("address");
            double lati=getIntent().getDoubleExtra("lati",0);
            double longti=getIntent().getDoubleExtra("longti",0);
            latLng=new LatLng(lati,longti);
            map.addMarker(new MarkerOptions()
                    .title(name)
                    .snippet(address)
                    .position(latLng));


            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "L?i permission", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                if (userLocation != null) {

                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    String url = getMapsApiDirectionsUrl(userLocation, latLng);
//                            String url = getMapsApiDirectionsUrl(new LatLng(10.794708,106.668081), marker.getPosition());
                    new ReadTask().execute(url);
                    //Start downloading json data from Google Directions API
                }
            } catch (Exception e) {
            }
            map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {

                @Override
                public boolean onMyLocationButtonClick() {
                    try {
                        if (userLocation != null) {
                            for (Polyline polyline:polyline)
                            {
                                polyline.remove();
                            }
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                            String url = getMapsApiDirectionsUrl(userLocation, latLng);
//                            String url = getMapsApiDirectionsUrl(new LatLng(10.794708,106.668081), marker.getPosition());
                            new ReadTask().execute(url);
                            //Start downloading json data from Google Directions API
                        }
                    } catch (Exception e) {
                    }
                    return false;
                }
            });
            map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

                @Override
                public void onMyLocationChange(Location location) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    userLocation = new LatLng(latitude, longitude);


                }


            });


        }catch (Exception e){};
        //Add direction

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private GoogleApiClient client;

    private LocationRequest locationRequest;

    private android.location.Location lastlocation;

    private Marker currentLocationmMarker;

    protected synchronized void bulidGoogleApiClient() {

        client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();

        client.connect();


    }
    int flag=0;
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



        if(flag<4) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            flag++;
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



        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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




        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED)

        {

            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, (com.google.android.gms.location.LocationListener) this);

        }
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
    public static final int REQUEST_LOCATION_CODE = 99;
    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode)

        {

            case REQUEST_LOCATION_CODE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)

                {

                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)

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
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
    ArrayList<Polyline> polyline=new ArrayList<>();
    private String  getMapsApiDirectionsUrl(LatLng origin,LatLng dest) {
        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
        return url;

    }
    public class ParserTask extends AsyncTask<String,Integer, List<List<HashMap<String , String >>>> {



        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {
            // TODO Auto-generated method stub
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;
            Log.d("xx",routes.size()+"");
            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(7);
                polyLineOptions.color(getResources().getColor(R.color.my_color));
            }
            polyline.add(map.addPolyline(polyLineOptions));
        }
    }

    public class MapHttpConnection {
        public String readUr(String mapsApiDirectionsUrl) throws IOException {
            String data = "";
            InputStream istream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(mapsApiDirectionsUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                istream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(istream));
                StringBuffer sb = new StringBuffer();
                String line ="";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                data = sb.toString();
                br.close();


            }
            catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                istream.close();
                urlConnection.disconnect();
            }
            return data;

        }
    }
    public class PathJSONParser {

        public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
            List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>();
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONArray jSteps = null;
            try {
                jRoutes = jObject.getJSONArray("routes");
                for (int i=0 ; i < jRoutes.length() ; i ++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List<HashMap<String, String>> path = new ArrayList<HashMap<String,String>>();
                    for(int j = 0 ; j < jLegs.length() ; j++) {
                        jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                        for(int k = 0 ; k < jSteps.length() ; k ++) {
                            String polyline = "";
                            polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);
                            for(int l = 0 ; l < list.size() ; l ++){
                                HashMap<String, String> hm = new HashMap<String, String>();
                                hm.put("lat",
                                        Double.toString(((LatLng) list.get(l)).latitude));
                                hm.put("lng",
                                        Double.toString(((LatLng) list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;

        }

        private List<LatLng> decodePoly(String encoded) {
            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }
            return poly;
        }}

    public class ReadTask extends AsyncTask<String, Void , String> {



        @Override
        protected String doInBackground(String... url) {
            // TODO Auto-generated method stub
            String data = "";
            try {
              MapHttpConnection http = new MapHttpConnection();
                data = http.readUr(url[0]);


            } catch (Exception e) {
                // TODO: handle exception
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
            Log.d("resultP",result);
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void AddStatus()
    { Window window = this.getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(getResources().getColor(R.color.my_color));
//       getSupportActionBar().hide();
    }
}
