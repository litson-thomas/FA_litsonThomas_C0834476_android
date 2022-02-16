package com.example.fa_litsonthomas_c0834476_android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.fa_litsonthomas_c0834476_android.DB.LocationAdaptor;
import com.example.fa_litsonthomas_c0834476_android.DB.LocationRoomDB;
import com.example.fa_litsonthomas_c0834476_android.Model.Location;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class FavouritesList extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Location> locationsList = new ArrayList<Location>();
    LinearLayoutManager linearLayoutManager;
    LocationRoomDB database;
    LocationAdaptor listAdapter;
    Location selectedLocationToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites_list);
        setTitle("Favourite List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // connect elements
        recyclerView = findViewById(R.id.locations_list);
        database = LocationRoomDB.getInstance(this);
        getLocationsList();

        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
          @Override
          public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
          }

          @Override
          public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
          }

          @Override
          public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            // Take action for the swiped item
            // swipe to left (EDIT ACTION)
            Location location = locationsList.get(viewHolder.getLayoutPosition());
            if(direction == 4){
              List<Place.Field> fieldList = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
              Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(FavouritesList.this);
              selectedLocationToEdit = location;
              startActivityForResult(intent, 24);
            }
            // swipe to right (DELETE ACTION)
            if(direction == 8){
              database.locationDao().delete(location);
            }
            getLocationsList(); // reset the table after the swipe is completed
          }

          @Override
          public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
              .addSwipeRightActionIcon(R.drawable.ic_baseline_delete_24)
              .addSwipeRightBackgroundColor(ContextCompat.getColor(FavouritesList.this, R.color.danger))
              .addSwipeLeftActionIcon(R.drawable.ic_baseline_edit_24)
              .addSwipeLeftBackgroundColor(ContextCompat.getColor(FavouritesList.this, R.color.primary))
              .create()
              .decorate();
          }

          @Override
          public void onMoved(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, int fromPos, @NonNull RecyclerView.ViewHolder target, int toPos, int x, int y) {
            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
            Log.e("MOVED", "MOVED COMPLETED");
          }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    private void getLocationsList(){
      locationsList = database.locationDao().getAll();
      linearLayoutManager = new LinearLayoutManager(this);
      recyclerView.setLayoutManager(linearLayoutManager);
      listAdapter = new LocationAdaptor(FavouritesList.this, locationsList);
      recyclerView.setAdapter(listAdapter);
    }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if(requestCode == 24 && resultCode == RESULT_OK){
      Place place = Autocomplete.getPlaceFromIntent(data);
      int count = database.locationDao().checkIfHaveLocation(place.getId());
      if(count <= 0 && selectedLocationToEdit != null){
        database.locationDao().updateLocation(selectedLocationToEdit.getId(), place.getName(), place.getAddress(), place.getLatLng().latitude, place.getLatLng().longitude, false, place.getId());
        Toast.makeText(getApplicationContext(), "Location Updated Successfully!", Toast.LENGTH_SHORT).show();
        getLocationsList();
      }
      else{
        Toast.makeText(getApplicationContext(), "Location Already Exists!", Toast.LENGTH_SHORT).show();
      }
    }
    else if(requestCode == 10 && resultCode == RESULT_CANCELED){
      getLocationsList();
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
}
