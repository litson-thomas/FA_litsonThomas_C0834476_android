package com.example.fa_litsonthomas_c0834476_android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.fa_litsonthomas_c0834476_android.DB.LocationRoomDB;
import com.example.fa_litsonthomas_c0834476_android.Model.Location;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class LocationDetails extends AppCompatActivity implements OnMapReadyCallback,
  GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    Long locationId;
    LocationRoomDB database;
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_CODE = 101;
    Button markVisitedBtn, getDirectionsBtn;
    android.location.Location myLocation = null;
    Location selectedLocation;
    LatLng startLatLang, endLatLang;
    //polyline object
    private List<Polyline> polylines=null;
    LinearLayout directionsLayout;
    Route selectedRoute;
    TextView durationText, distanceText, routeDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_details);
        setTitle("Location Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        database = LocationRoomDB.getInstance(this);
        markVisitedBtn = findViewById(R.id.markVisitedBtn);
        getDirectionsBtn = findViewById(R.id.getDirectionsBtn);
        directionsLayout = findViewById(R.id.directionsDetails);
        durationText = findViewById(R.id.duration);
        distanceText = findViewById(R.id.distanceKms);
        routeDesc = findViewById(R.id.routerDesc);

        // method to trigger the visited button
        markVisitedBtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Location selectedLocation = database.locationDao().getLocationById(locationId);
            Boolean visitStatus = selectedLocation.getLocationVisited();
            database.locationDao().updateLocation(selectedLocation.getId(), selectedLocation.getLocationName(), selectedLocation.getLocationAddress(), selectedLocation.getLocationLat(), selectedLocation.getLocationLon(), !visitStatus, selectedLocation.getLocationAddressId());
            // Log.e("VISIT STATUS:", ""+visitStatus);
            if(visitStatus){
              markVisitedBtn.setText("Mark Visited");
            }
            else{
              markVisitedBtn.setText("Mark Not Visited");
            }
          }
        });

        // method to trigger the getDirectionsBtn
        getDirectionsBtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            mMap.clear();
            LatLng start = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
            //start route finding
            Findroutes(start, endLatLang);
          }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
          locationId = Long.parseLong(String.valueOf(extras.get("id")));
        }

        // set the google maps fragment
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
          .findFragmentById(R.id.detailsMap);

        if (isLocationPermissionGranted()){
          mapFragment.getMapAsync(this);
        }
        else{
          requestLocation();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
      switch (item.getItemId()) {
        case android.R.id.home:
          finish();
          return true;
      }

      return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
      mMap = googleMap;

      if(isLocationPermissionGranted()){
        mMap.setMyLocationEnabled(true);
        assignValues();
        getMyLocation();
      }
    }

    //to get user location
    private void getMyLocation(){
      mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(@NonNull android.location.Location location) {
          myLocation = location;
          LatLng ltlng = new LatLng(location.getLatitude(),location.getLongitude());
          startLatLang = ltlng;
//          CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
//            ltlng, 16f);
//          mMap.animateCamera(cameraUpdate);
        }
      });

      mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {

        }
      });

    }

    private void assignValues(){
      if(locationId != null){
        Location location = database.locationDao().getLocationById(locationId);
        selectedLocation = location;
        setTitle(location.getLocationName());
        mMap.clear();
        // Add a marker in Sydney and move the camera
        LatLng locationCoordinates = new LatLng(location.getLocationLat(), location.getLocationLon());
        mMap.addMarker(new MarkerOptions().position(locationCoordinates).title(location.getLocationName()));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationCoordinates.latitude, locationCoordinates.longitude), 17.0f));
        // change the mark visited title according to the status
        if(location.getLocationVisited()){
          markVisitedBtn.setText("Mark Not Visited");
        }
        // assign the end (destination) latlang
        endLatLang = locationCoordinates;
      }
    }

  /**
   * method to find if the application has the persmission to use the location
   * @return
   */
  private Boolean isLocationPermissionGranted(){
    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
      return true;
    }
    else{
      return false;
    }
  }

  private void requestLocation(){
    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
  }

  private void displayDirectionsDetails(){
    if(selectedRoute != null){
      directionsLayout.setVisibility(View.VISIBLE);
      durationText.setText("("+selectedRoute.getDurationText()+")");
      distanceText.setText(selectedRoute.getDistanceText());
      routeDesc.setText("The distance between Your location to " + selectedRoute.getEndAddressText() + " is " + selectedRoute.getDistanceText());
    }
  }


    /**
   * Following are the methods used to
   * find the route between the two points/coordinates
   */

    // function to find Routes.
    public void Findroutes(LatLng Start, LatLng End)
    {
      if(Start==null || End==null) {
        Toast.makeText(LocationDetails.this,"Unable to get location", Toast.LENGTH_LONG).show();
      }
      else
      {

        Routing routing = new Routing.Builder()
          .travelMode(AbstractRouting.TravelMode.DRIVING)
          .withListener(this)
          .alternativeRoutes(true)
          .waypoints(Start, End)
          .key(getString(R.string.maps_api_key))  //also define your api key here.
          .build();
        routing.execute();
      }
    }

    //Routing call back functions.
    @Override
    public void onRoutingFailure(RouteException e) {
      View parentLayout = findViewById(android.R.id.content);
      Snackbar snackbar= Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
      snackbar.show();
    //        Findroutes(start,end);
    }

    @Override
    public void onRoutingStart() {
      Toast.makeText(LocationDetails.this,"Finding Route...",Toast.LENGTH_LONG).show();
    }

    //If Route finding success..
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

      CameraUpdate center = CameraUpdateFactory.newLatLng(startLatLang);
      CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
      if(polylines!=null) {
        polylines.clear();
      }
      PolylineOptions polyOptions = new PolylineOptions();
      LatLng polylineStartLatLng=null;
      LatLng polylineEndLatLng=null;


      polylines = new ArrayList<>();
      //add route(s) to the map using polyline
      for (int i = 0; i <route.size(); i++) {

        if(i==shortestRouteIndex){
          polyOptions.color(getResources().getColor(R.color.primary));
          polyOptions.width(20);
          polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
          Polyline polyline = mMap.addPolyline(polyOptions);
          polylineStartLatLng=polyline.getPoints().get(0);
          int k=polyline.getPoints().size();
          polylineEndLatLng=polyline.getPoints().get(k-1);
          polylines.add(polyline);
          // Log.e("ROUTE ====", ""+route.get(shortestRouteIndex).getDistanceText()+"----"+route.get(shortestRouteIndex).getDurationText());
          selectedRoute = route.get(shortestRouteIndex);
          // display the directions details
          displayDirectionsDetails();
        }

      }

      //Add Marker on route starting position
      MarkerOptions startMarker = new MarkerOptions();
      startMarker.position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
      startMarker.title("My Location");
      //Add Marker on route ending position
      MarkerOptions endMarker = new MarkerOptions();
      endMarker.position(polylineEndLatLng);
      endMarker.title(selectedLocation.getLocationName());
      mMap.addMarker(endMarker);
    }

    @Override
    public void onRoutingCancelled() {
      Findroutes(startLatLang, endLatLang);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
      Findroutes(startLatLang, endLatLang);

    }

}
