package com.example.fa_litsonthomas_c0834476_android.Model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(
  tableName = "locations"
)
public class Location {

  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "location_id")
  private long id;

  @NonNull
  @ColumnInfo(name = "location_name")
  private String locationName;

  @ColumnInfo(name = "location_address")
  private String locationAddress;

  @ColumnInfo(name = "location_address_id")
  private String locationAddressId;

  @ColumnInfo(name = "location_visited")
  private Boolean locationVisited;

  @ColumnInfo(name = "location_latitude")
  private Double locationLat;

  @ColumnInfo(name = "location_longitude")
  private Double locationLon;

  @ColumnInfo(name = "created_date")
  private String createdDate;

  public Location(@NonNull String locationName, String locationAddress, Double locationLat, double locationLon, String createdDate, String locationAddressId) {
    this.locationAddressId = locationAddressId;
    this.createdDate = createdDate;
    this.locationAddress = locationAddress;
    this.locationName = locationName;
    this.locationVisited = false;
    this.locationLat = locationLat;
    this.locationLon = locationLon;
  }

  @Override
  public String toString() {
    return "Location{" +
      "id=" + id +
      ", locationName='" + locationName + '\'' +
      ", locationAddress='" + locationAddress + '\'' +
      ", locationVisited=" + locationVisited +
      ", locationLat=" + locationLat +
      ", locationLon=" + locationLon +
      ", createdDate='" + createdDate + '\'' +
      '}';
  }

  public long getId() {
    return id;
  }

  @NonNull
  public String getLocationName() {
    return locationName;
  }

  public String getLocationAddress() {
    return locationAddress;
  }

  public Boolean getLocationVisited() {
    return locationVisited;
  }

  public String getLocationAddressId() {
    return locationAddressId;
  }

  public Double getLocationLat() {
    return locationLat;
  }

  public Double getLocationLon() {
    return locationLon;
  }

  public String getCreatedDate() {
    return createdDate;
  }

  public void setId(long id) {
    this.id = id;
  }

  public void setLocationName(@NonNull String locationName) {
    this.locationName = locationName;
  }

  public void setLocationAddress(String locationAddress) {
    this.locationAddress = locationAddress;
  }

  public void setLocationVisited(Boolean locationVisited) {
    this.locationVisited = locationVisited;
  }

  public void setLocationLat(Double locationLat) {
    this.locationLat = locationLat;
  }

  public void setLocationLon(Double locationLon) {
    this.locationLon = locationLon;
  }

  public void setCreatedDate(String createdDate) {
    this.createdDate = createdDate;
  }

  public void setLocationAddressId(String locationAddressId) {
    this.locationAddressId = locationAddressId;
  }
}
