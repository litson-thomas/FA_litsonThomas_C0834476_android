package com.example.fa_litsonthomas_c0834476_android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.example.fa_litsonthomas_c0834476_android.Model.Location;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomDirections extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_CODE = 101;
    Button getDirectionsBtn;
    LatLng startLatLang, endLatLang;
    //polyline object
    private List<Polyline> polylines=null;
    Location fromLocation, toLocation;
    LinearLayout fromLayout, toLayout, directionsLayout;
    Route selectedRoute;
    TextView fromAddress, toAddress, durationText, distanceText, routeDesc;
    Marker fromMaker, toMarker;
    Polyline polyLineInMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_directions);
        setTitle("Get Directions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // connect the elements
        fromLayout = findViewById(R.id.fromSearch);
        toLayout = findViewById(R.id.toSearch);
        fromAddress = findViewById(R.id.fromAddress);
        toAddress = findViewById(R.id.toAddress);
        getDirectionsBtn = findViewById(R.id.getCustomDirectionsBtn);
        directionsLayout = findViewById(R.id.directionsDetails);
        durationText = findViewById(R.id.duration);
        distanceText = findViewById(R.id.distanceKms);
        routeDesc = findViewById(R.id.routerDesc);

        // trigger get directions btn
        getDirectionsBtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            if(startLatLang != null && endLatLang != null){
              Findroutes(startLatLang, endLatLang);
            }
            else{
              Toast.makeText(CustomDirections.this,"Please select from and to addresses!", Toast.LENGTH_LONG).show();
            }
          }
        });

        // trigger from search
        fromLayout.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            List<Place.Field> fieldList = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(CustomDirections.this);
            startActivityForResult(intent, 101);
          }
        });

        // trigger to search
        toLayout.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            List<Place.Field> fieldList = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(CustomDirections.this);
            startActivityForResult(intent, 102);
          }
        });

        // set the google maps fragment
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
          .findFragmentById(R.id.customDetailsMap);

        if (isLocationPermissionGranted()){
          mapFragment.getMapAsync(this);
        }
        else{
          requestLocation();
        }
    }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    if(requestCode == 101 && resultCode == RESULT_OK){
      Place place = Autocomplete.getPlaceFromIntent(data);
      // Add a marker
      LatLng locationCoordinates = place.getLatLng();
      if(fromMaker == null){
        fromMaker = mMap.addMarker(new MarkerOptions().position(locationCoordinates).title(place.getName()));
      }
      else{
        fromMaker.setPosition(locationCoordinates);
      }
      mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationCoordinates.latitude, locationCoordinates.longitude), 17.0f));
      fromAddress.setText(place.getName());
      startLatLang = place.getLatLng();
      fromLocation = new Location(place.getName(), place.getAddress(), place.getLatLng().latitude, place.getLatLng().longitude, "", place.getId());
    }
    if(requestCode == 102 && resultCode == RESULT_OK){
      Place place = Autocomplete.getPlaceFromIntent(data);
      // Add a marker
      LatLng locationCoordinates = place.getLatLng();
      if(toMarker == null){
        toMarker = mMap.addMarker(new MarkerOptions().position(locationCoordinates).title(place.getName()));
      }
      else{
        toMarker.setPosition(locationCoordinates);
      }
      mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationCoordinates.latitude, locationCoordinates.longitude), 17.0f));
      toAddress.setText(place.getName());
      endLatLang = place.getLatLng();
      toLocation = new Location(place.getName(), place.getAddress(), place.getLatLng().latitude, place.getLatLng().longitude, "", place.getId());
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  private void displayDirectionsDetails(){
    if(fromLocation != null && toLocation != null){
      directionsLayout.setVisibility(View.VISIBLE);
      durationText.setText("("+selectedRoute.getDurationText()+")");
      distanceText.setText(selectedRoute.getDistanceText());
      routeDesc.setText("The distance from" + fromLocation.getLocationName() + " to " + toLocation.getLocationName() + " is " + selectedRoute.getDistanceText());
    }
  }

  @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
      mMap = googleMap;
      if(isLocationPermissionGranted()){
        mMap.setMyLocationEnabled(true);
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


    /**
     * Following are the methods used to
     * find the route between the two points/coordinates
     */

    // function to find Routes.
    public void Findroutes(LatLng Start, LatLng End)
    {
      if(Start==null || End==null) {
        Toast.makeText(CustomDirections.this,"Unable to get location", Toast.LENGTH_LONG).show();
      }
      else
      {
        if(polyLineInMap != null){
          polyLineInMap.remove();
        }
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
    }

    @Override
    public void onRoutingStart() {
      Toast.makeText(CustomDirections.this,"Finding Route...",Toast.LENGTH_LONG).show();
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
          polyLineInMap = mMap.addPolyline(polyOptions);
          polylineStartLatLng = polyLineInMap.getPoints().get(0);
          int k = polyLineInMap.getPoints().size();
          polylineEndLatLng=polyLineInMap.getPoints().get(k-1);
          polylines.add(polyLineInMap);
          // Log.e("ROUTE ====", ""+route.get(shortestRouteIndex).getDistanceText()+"----"+route.get(shortestRouteIndex).getDurationText());
          selectedRoute = route.get(shortestRouteIndex);
          displayDirectionsDetails();
        }

      }
    }

    @Override
    public void onRoutingCancelled() {
      Findroutes(startLatLang, endLatLang);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
      Findroutes(startLatLang, endLatLang);
    }

    private void requestLocation(){
      ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
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


}
