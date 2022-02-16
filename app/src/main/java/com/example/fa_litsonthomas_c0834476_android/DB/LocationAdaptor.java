package com.example.fa_litsonthomas_c0834476_android.DB;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fa_litsonthomas_c0834476_android.LocationDetails;
import com.example.fa_litsonthomas_c0834476_android.Model.Location;
import com.example.fa_litsonthomas_c0834476_android.R;

import java.util.List;

public class LocationAdaptor extends RecyclerView.Adapter<LocationAdaptor.ViewHolder> {


  // variables
  private List<Location> locationsList;
  private Activity context;
  private LocationRoomDB database;

  public LocationAdaptor(Activity context, List<Location> list){
    this.locationsList = list;
    this.context = context;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    // Log.e("COUNT ADAPTOR => ", ""+locationsList.size());
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_list_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    Location location = locationsList.get(position);
    database = LocationRoomDB.getInstance(context);
    holder.name.setText(location.getLocationName());
    holder.address.setText(location.getLocationAddress());
    holder.date.setText(location.getCreatedDate());

    // Log.e("LOCATION STATUS => ", ""+location.getLocationVisited());
    // change the visited icon
    holder.locationVisited.setVisibility(View.INVISIBLE);
    holder.locationNotVisited.setVisibility(View.VISIBLE);
    if(location.getLocationVisited()){
      holder.locationVisited.setVisibility(View.VISIBLE);
      holder.locationNotVisited.setVisibility(View.INVISIBLE);
    }

    holder.wrapperLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(context, LocationDetails.class);
        intent.putExtra("id", location.getId());
        // context.startActivity(intent);
        context.startActivityForResult(intent, 10);
      }
    });

  }

  @Override
  public int getItemCount() {
    return locationsList.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    LinearLayout wrapperLayout;
    TextView name, address, date;
    ImageView locationVisited, locationNotVisited;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      wrapperLayout = itemView.findViewById(R.id.detailsWrapper);
      name = itemView.findViewById(R.id.locationName);
      address = itemView.findViewById(R.id.locationAddress);
      date = itemView.findViewById(R.id.locationDate);
      locationVisited = itemView.findViewById(R.id.locationVisited);
      locationNotVisited = itemView.findViewById(R.id.notVisited);
    }
  }

}
