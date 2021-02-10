package com.example.client;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.client.Common.Common;
import com.example.client.Helper.CustomInfoWindow;
import com.example.client.Model.Client;
import com.example.client.Model.FCMResponse;
import com.example.client.Model.Sender;
import com.example.client.Model.Token;
import com.example.client.Remote.IFCMService;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
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
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.example.client.Model.Notification;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    SupportMapFragment mapFragment;
    private AppBarConfiguration mAppBarConfiguration;

    private GoogleMap mMap;
    //play services
    private static final int MY_PERMISSION_REQUEST_CODE=7000;
    private static final int PLAY_SERVICE_RES_REQUEST=7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL =5000;
    private static int FATEST_INTERVAL=3000;
    private static int DISPLACEMENT=10;

    DatabaseReference drivers;
    GeoFire geoFire;

    Marker mUsermarker;

    //bottomsheet

    ImageView imgExpandable;
    BottomSheetRiderFragment mBottomSheet;
    Button btnPickupRequest;



    boolean isDriverFound=false;
    String driverId="";
    int radius=1; //1km
    int distance = 1 ; //3km

    private static final int LIMIT=400

            ;

    //send alert
    IFCMService mService;

    //presence system
    DatabaseReference driversAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mService =Common.getFCMService();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
       // NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_container);
       // NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
      //  NavigationUI.setupWithNavController(navigationView, navController);
        mapFragment=(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //geo fire

        //drivers = FirebaseDatabase.getInstance("https://taxi-3b609-default-rtdb.firebaseio.com/").getReference(Common.driver_tb1);
       // geoFire=new GeoFire(drivers);





        //init view
        imgExpandable=findViewById(R.id.imgExpandable);
        mBottomSheet= BottomSheetRiderFragment.newInstance("Rider bottom sheet");
      /*  imgExpandable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheet.show(getSupportFragmentManager(),mBottomSheet.getTag());
            }
        });*/
       btnPickupRequest=findViewById(R.id.btnPickupRequest);
       btnPickupRequest.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
         if(!isDriverFound)
             requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
         else
             sendRequestToDriver(driverId);

           }
       });

        setUpLocation();

        updateFirebaseToken();


    }


    private void updateFirebaseToken() {

        FirebaseDatabase db =FirebaseDatabase.getInstance();
        DatabaseReference tokens =db.getReference(Common.token_tb1);

        Token token =new Token(FirebaseInstanceId.getInstance().getToken());

        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(token);
    }

    private void sendRequestToDriver(String driverId) {

        DatabaseReference tokens=FirebaseDatabase.getInstance().getReference(Common.token_tb1);

        tokens.orderByKey().equalTo(driverId)

                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                        {
                            Token token =postSnapshot.getValue(Token.class);//Get Token object from db with key

                           String json_lat_lng = new Gson().toJson(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
                           String ClientToToken =FirebaseInstanceId.getInstance().getToken();
                           Notification data =new Notification(ClientToToken,json_lat_lng); // send it to taxi app and wewill deserialize it again
                            Sender content = new Sender(token.getToken(),data); //send this data to token

                            mService.sendMessage(content)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            if ( response.body() != null && response.body().success == 1)
                                                Toast.makeText(Home.this,"Request sent!",Toast.LENGTH_SHORT).show();
                                            else
                                                Toast.makeText(Home.this,"Failed  !",Toast.LENGTH_SHORT).show();

                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                            Log.e("ERROR",t.getMessage());


                                        }
                                    });


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void requestPickupHere(String uid) {
    DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tb1);
    GeoFire mGeoFire = new GeoFire(dbRequest);
    mGeoFire.setLocation(uid,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

    if(mUsermarker.isVisible())
        mUsermarker.remove();

    //add new marker
        mUsermarker= mMap.addMarker(new MarkerOptions()
                .title("Pickup Here ")
                .snippet("")
                .position(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
             mUsermarker.showInfoWindow();
           btnPickupRequest.setText("Getting your Driver ....");

findDriver();


    }

    private void findDriver() {
        final DatabaseReference drivers =FirebaseDatabase.getInstance().getReference(Common.driver_tb1);
        GeoFire gfDrivers = new GeoFire(drivers);
        GeoQuery geoQuery=gfDrivers.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                //if found
                if (!isDriverFound){

                    isDriverFound=true;
                    driverId=key;
                    btnPickupRequest.setText("CALL DRIVER");
                    Toast.makeText(Home.this,""+key,Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
             //if still not found driver ,increase distance

                if(!isDriverFound){
                    radius++;
                    findDriver();

                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                  /*  if (checkPlaySercices()){
                        buildGoogleApiClient();
                        createLocationRequest();*/

                            setUpLocation();


                }
                break;
        }
    }

    private void setUpLocation() {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
            {
                //request runtime permission
                ActivityCompat.requestPermissions(this,new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                },MY_PERMISSION_REQUEST_CODE );
            }
            else{
                if (checkPlaySercices()){
                    buildGoogleApiClient();
                    createLocationRequest();
                        displayLocation();
                    
                }

            }
        }

    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mLastLocation= LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation !=null){
            //presence system
            driversAvailable =FirebaseDatabase.getInstance().getReference(Common.driver_tb1);
            driversAvailable.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    loadAllAvailableDriver();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

                final double latitude=mLastLocation.getLatitude() ;
                final double longitude = mLastLocation.getLongitude();


                        //add marker

                        if (mUsermarker != null)
                            mUsermarker.remove(); //remove already marker
                        mUsermarker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .title("You"));

                        //Move camera to this position
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));



                        loadAllAvailableDriver();
            Log.d("EDMTDEV",String.format("Your location was changed : %f /%f",latitude,longitude));


        }



        else {
            Log.d("EDMTDEV","Cannot get your location");
        }

    }

    private void loadAllAvailableDriver() {

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()))
        .title("You"));


        //load all available Driver in  distance 3km

        DatabaseReference driverLocation=FirebaseDatabase.getInstance().getReference(Common.driver_tb1);
        GeoFire gf =new GeoFire(driverLocation);

        GeoQuery  geoQuery=gf.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()),distance);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                //use key to get email from table Users
                //Table Users is table when driver register account and update information
                FirebaseDatabase.getInstance().getReference(Common.user_driver_tb1)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                //
                                Client c= dataSnapshot.getValue(Client.class);

                                //add driver to map
                                if (c != null) {

                                    mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(location.latitude, location.longitude))
                                            .flat(true)
                                            .title("Phone: "+c.getPhoneNo())
                                            .snippet("Driver: "+c.getName())
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi_icon)));

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if (distance <=LIMIT) //distance just find for 3 km
                {
                    distance++;
                    loadAllAvailableDriver();

                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }


    private void createLocationRequest() {

        mLocationRequest =new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {

        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlaySercices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if
            (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RES_REQUEST).show();
            else{

                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();

            }
            return false;

        }

        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_container);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
       mMap=googleMap;
       mMap.getUiSettings().setZoomControlsEnabled(true);
       mMap.getUiSettings().setZoomGesturesEnabled(true);
       mMap.setInfoWindowAdapter(new CustomInfoWindow(this));
        //add sample marker
       /* googleMap.addMarker((new MarkerOptions()
        .position(new LatLng(37.7750,-122.4183))
        .title("San Francisco")));

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.7750,-122.4183),12));*/
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates( mGoogleApiClient,
                mLocationRequest,this);
    }

    @Override
    public void onConnectionSuspended(int i) {
       mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
     mLastLocation=location;
      displayLocation();
    }
}