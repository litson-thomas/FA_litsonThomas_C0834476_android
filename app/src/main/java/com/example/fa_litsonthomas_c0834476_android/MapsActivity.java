package com.example.fa_litsonthomas_c0834476_android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fa_litsonthomas_c0834476_android.DB.LocationRoomDB;
import com.example.fa_litsonthomas_c0834476_android.Model.Location;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.fa_litsonthomas_c0834476_android.databinding.ActivityMapsBinding;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.model.RankBy;

import java.io.IOException;
import java.security.Provider;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private static final int LOCATION_PERMISSION_CODE = 101;
    TextView searchTitle, selectedLocationName, selectedLocationAddress;
    LinearLayout selectedLocationPanel;
    Button customDirectionBtn, locationsList, addNewLocationBtn;
    Location selectedLocation;
    LocationRoomDB database;
    List<Location> locationList;
    android.location.Location myLocation=null;
    Spinner mapsStyles;
    ChipGroup nearbyButtonChips;
    PlacesSearchResult[] placesSearchResults = null;
    Boolean ifMyLocationShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = LocationRoomDB.getInstance(this);

        // set the maps styles spinner
        String[] arraySpinner = new String[] {"Normal", "Terrain", "Satellite", "Hybrid"};
        mapsStyles = findViewById(R.id.mapStyles);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
          android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapsStyles.setAdapter(adapter);

        mapsStyles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if(mMap != null){
              if(position == 0){
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
              }
              if(position == 1){
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
              }
              if(position == 2){
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
              }
              if(position == 3){
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
              }
            }
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {

          }
        });

        // connect the elements
        nearbyButtonChips = findViewById(R.id.nearbyButtonChips);
        searchTitle = findViewById(R.id.searchTitle);
        selectedLocationName = findViewById(R.id.selectedName);
        selectedLocationAddress = findViewById(R.id.selectedAddress);
        selectedLocationPanel = findViewById(R.id.selectedLocationWrapper);
        customDirectionBtn = findViewById(R.id.addNewPlace);
        locationsList = findViewById(R.id.toMyList);
        addNewLocationBtn = findViewById(R.id.addLocationBtn);
        searchTitle.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            clearSelectedPanel();
            List<Place.Field> fieldList = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(MapsActivity.this);
            startActivityForResult(intent, 100);
          }
        });

        // go to custom directions
        customDirectionBtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent(MapsActivity.this, CustomDirections.class);
            startActivity(intent);
          }
        });

        // method to trigger the nearby button chip click
        nearbyButtonChips.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
          @Override
          public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            for(int i = 0; i < nearbyButtonChips.getChildCount(); i++){
              View chip = nearbyButtonChips.getChildAt(i);
              chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  String chipId = v.getResources().getResourceEntryName(v.getId());
                  // Log.e("ID => ", ""+v.getResources().getResourceEntryName(v.getId()));
                  Toast.makeText(getApplicationContext(), "Getting nearby places!", Toast.LENGTH_SHORT).show();
                  placesSearchResults = null;
                  placesSearchResults = getNearbyPlaces(getPlaceTypeFromString(chipId)).results;
                  if(placesSearchResults != null){
                    // Log.e("PLACES RESPONSE => ", ""+placesSearchResults.length);
                    // Log.e("PLACES RESPONSE => ", ""+placesSearchResults[0].geometry.location);
                    markNearbyPlaces();
                  }
                  else if(placesSearchResults != null && placesSearchResults.length <= 0){
                    Toast.makeText(getApplicationContext(), "No nearby locations found!", Toast.LENGTH_SHORT).show();
                  }
                }
              });
            }
          }
        });

        // add new location
        addNewLocationBtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            int count = database.locationDao().checkIfHaveLocation(selectedLocation.getLocationAddressId());
            if(selectedLocation != null){
              if(count <= 0){
                database.locationDao().insert(selectedLocation);
                Toast.makeText(getApplicationContext(), "Location Added to Favourite list!", Toast.LENGTH_SHORT).show();
                selectedLocation = null;
                clearSelectedPanel();
              }
              else{
                Toast.makeText(getApplicationContext(), "Location Already added!", Toast.LENGTH_SHORT).show();
              }
            }
          }
        });

        // navigate to the list
        locationsList.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent(MapsActivity.this, FavouritesList.class);
            startActivityForResult(intent, 5);
          }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
          .findFragmentById(R.id.map);

        if (isLocationPermissionGranted()){
          mapFragment.getMapAsync(this);
        }
        else{
          requestLocation();
          mapFragment.getMapAsync(this);
          getMyLocation();
        }

        // initialize places
        Places.initialize(getApplicationContext(), getString(R.string.maps_api_key));

        // places client
        PlacesClient placesClient = Places.createClient(this);

    }

    /**
     * This method helps to mark the near by places
     * from the placesSearchResults
     */
    private void markNearbyPlaces(){
      mMap.clear();
      displayAllFavLocations(); // display all the favorite locations after clearing the locations in the map
      if(placesSearchResults != null){
        for(int i = 0; i < placesSearchResults.length; i++){
          LatLng position = new LatLng(placesSearchResults[i].geometry.location.lat, placesSearchResults[i].geometry.location.lng);
          // Location markerLocation = new Location(placesSearchResults[i].name, placesSearchResults[i].formattedAddress, placesSearchResults[i].geometry.location.lat, placesSearchResults[i].geometry.location.lng, getTodayDate(), placesSearchResults[i].placeId);
          MarkerOptions nearbyPlaceMarker = new MarkerOptions();
          nearbyPlaceMarker.position(position);
          nearbyPlaceMarker.title(placesSearchResults[i].name);
          mMap.addMarker(nearbyPlaceMarker).setTag(i);
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
          new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 11f);
        mMap.animateCamera(cameraUpdate);
      }
    }


    /**
     * Method helps to convert the place string
     * to the place type to change the Google map
     * style according to the place type
     * @param place
     * @return
     */

    private PlaceType getPlaceTypeFromString(String place){
      PlaceType placeType = null;
      // Log.e("TYPE STRING", ""+getString(R.string.nearby_restaurants)+"----"+place);
      if(getString(R.string.nearby_restaurants).equals(place)){
        placeType = PlaceType.RESTAURANT;
      }
      if(getString(R.string.nearby_airport).equals(place)){
        placeType = PlaceType.AIRPORT;
      }
      if(getString(R.string.nearby_atm).equals(place)){
        placeType = PlaceType.ATM;
      }
      if(getString(R.string.nearby_banks).equals(place)){
        placeType = PlaceType.BANK;
      }
      if(getString(R.string.nearby_cafe).equals(place)){
        placeType = PlaceType.CAFE;
      }
      if(getString(R.string.nearby_church).equals(place)){
        placeType = PlaceType.CHURCH;
      }
      return placeType;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      if(requestCode == 100 && resultCode == RESULT_OK){
        Place place = Autocomplete.getPlaceFromIntent(data);
        // Log.e("Selected Place => ", place.getName());
        searchTitle.setText(place.getName());
        if(mMap != null) {
          mMap.clear();
          displayAllFavLocations();
          // Add a marker in Sydney and move the camera
          LatLng locationCoordinates = place.getLatLng();
          mMap.addMarker(new MarkerOptions().position(locationCoordinates).title(place.getName()));
          mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationCoordinates.latitude, locationCoordinates.longitude), 17.0f));
          // prepare the selected location details panel
          selectedLocationPanel.setVisibility(View.VISIBLE);
          selectedLocationName.setText(place.getName());
          selectedLocationAddress.setText(place.getAddress());
          // create new location object
          selectedLocation = new Location(place.getName(), place.getAddress(), place.getLatLng().latitude, place.getLatLng().longitude, getTodayDate(), place.getId());
        }
      }
      else if(resultCode == AutocompleteActivity.RESULT_ERROR){
        Status status = Autocomplete.getStatusFromIntent(data);
        Toast.makeText(getApplicationContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
      }
      else if(requestCode == 5 && resultCode == RESULT_CANCELED){
        // Log.e("RESULT BACK => ", ""+requestCode);
        displayAllFavLocations();
      }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(isLocationPermissionGranted()){
          mMap.setMyLocationEnabled(true);
          displayAllFavLocations();
          getMyLocation();
        }
    }

    /**
   * method to find the nearby places from the location
   * @param placeType
   * @return
   */
    private PlacesSearchResponse getNearbyPlaces(PlaceType placeType){
      // Log.e("TYPE", ""+placeType);
      PlacesSearchResponse request = new PlacesSearchResponse();
      GeoApiContext context = new GeoApiContext.Builder()
        .apiKey(getString(R.string.maps_api_key))
        .build();
      com.google.maps.model.LatLng location = new com.google.maps.model.LatLng(myLocation.getLatitude(), myLocation.getLongitude());
      try {
        request = PlacesApi.nearbySearchQuery(context, location)
          .radius(10000)
          .rankby(RankBy.PROMINENCE)
          .keyword("cruise")
          .language("en")
          .type(placeType)
          .await();
      } catch (ApiException | IOException | InterruptedException e) {
        e.printStackTrace();
        Toast.makeText(getApplicationContext(), "Some error occured while getting neraby places!", Toast.LENGTH_SHORT).show();
      } finally {
        return request;
      }
    }

    private void displayAllFavLocations(){
      locationList = database.locationDao().getAll();
      if(mMap != null && locationList.size() > 0){
        mMap.clear();
        for (int i = 0; i < locationList.size(); i++){
          MarkerOptions favMarker = new MarkerOptions();
          favMarker.position(new LatLng(locationList.get(i).getLocationLat(), locationList.get(i).getLocationLon()));
          favMarker.title(locationList.get(i).getLocationName());
          if(locationList.get(i).getLocationVisited()){
            favMarker.icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.check_mark));
          }
          else{
            favMarker.icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.location));
          }
          mMap.addMarker(favMarker);
        }
      }
    }

    //to get user location
    private void getMyLocation(){
      mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(@NonNull android.location.Location location) {
          if(selectedLocation == null && ifMyLocationShown == false){
            myLocation = location;
            LatLng ltlng = new LatLng(location.getLatitude(),location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
              ltlng, 12.5f);
            mMap.animateCamera(cameraUpdate);
            ifMyLocationShown = true;
          }
        }

      });

      // marker click trigger
      mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(@NonNull Marker marker) {
          // Log.e("MARKER TAG => ", ""+marker.getTag());
          // prepare the selected location details panel
          if(marker != null && marker.getTag() != null){
            int tag = (int)(marker.getTag());
            PlacesSearchResult place = placesSearchResults[tag];
            selectedLocationPanel.setVisibility(View.VISIBLE);
            selectedLocationName.setText(place.name);
            selectedLocationAddress.setText(place.formattedAddress);
            // create new location object
            selectedLocation = new Location(place.name, place.formattedAddress, place.geometry.location.lat, place.geometry.location.lng, getTodayDate(), place.placeId);
          }
          return false;
        }
      });

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

    private void clearSelectedPanel(){
      selectedLocationPanel.setVisibility(View.INVISIBLE);
      searchTitle.setText(getString(R.string.search_title));
    }

    public static String getTodayDate(){
      Date presentTime_Date = Calendar.getInstance().getTime();
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
      return dateFormat.format(presentTime_Date);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
      Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
      vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
      Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(bitmap);
      vectorDrawable.draw(canvas);
      return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
