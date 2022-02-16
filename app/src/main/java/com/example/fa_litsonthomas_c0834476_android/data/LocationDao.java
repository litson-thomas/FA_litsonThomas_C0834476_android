package com.example.fa_litsonthomas_c0834476_android.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.fa_litsonthomas_c0834476_android.Model.Location;

import java.util.List;

@Dao
public abstract class LocationDao {

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  public abstract void insert(Location location);

  @Delete
  public abstract void delete(Location location);

  @Query("DELETE FROM locations")
  public abstract void deleteAll();

  @Query("SELECT * FROM locations WHERE location_id = :id")
  public abstract Location getLocationById(Long id);

  @Query("UPDATE locations SET location_name = :locationName, location_address = :locationAddress, location_latitude = :locationLat, location_longitude = :locationLon, location_visited = :locationVisited, location_address_id = :locationAddressId  WHERE location_id = :locationId")
  public abstract void updateLocation(Long locationId, String locationName, String locationAddress, Double locationLat, Double locationLon, Boolean locationVisited, String locationAddressId);

  @Query("SELECT * FROM locations order by location_id DESC")
  public abstract List<Location> getAll();

  @Query("SELECT count(*) as totalLocations FROM locations")
  public abstract int getCount();

  @Query("SELECT count(*) as totalLocations FROM locations where location_address_id = :id")
  public abstract int checkIfHaveLocation(String id);

}

