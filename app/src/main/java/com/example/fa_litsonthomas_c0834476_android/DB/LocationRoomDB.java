package com.example.fa_litsonthomas_c0834476_android.DB;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.fa_litsonthomas_c0834476_android.Model.Location;
import com.example.fa_litsonthomas_c0834476_android.data.LocationDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Location.class}, version = 1, exportSchema = false)
public abstract class LocationRoomDB extends RoomDatabase {

  public abstract LocationDao locationDao();

  private static volatile LocationRoomDB INSTANCE;

  private static final int NUMBER_OF_THREADS = 4;
  // executor service helps to do tasks in background thread
  public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

  public static LocationRoomDB getInstance(final Context context) {
    if (INSTANCE == null) {
      synchronized (LocationRoomDB.class) {
        if (INSTANCE == null) {
          INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
              LocationRoomDB.class,
              "locations_database")
            .addCallback(sRoomDatabaseCallback)
            .allowMainThreadQueries()
            .build();
        }
      }
    }
    return INSTANCE;
  }

  private static final RoomDatabase.Callback sRoomDatabaseCallback =
    new RoomDatabase.Callback() {
      @Override
      public void onCreate(@NonNull SupportSQLiteDatabase db) {
        super.onCreate(db);

        databaseWriteExecutor.execute(() -> {
          LocationDao locationDao = INSTANCE.locationDao();
          locationDao.deleteAll();

        });
      }
    };
}
