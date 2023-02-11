package com.darshan09200.maps.data;

import androidx.room.RoomDatabase;

import com.darshan09200.maps.model.Favourite;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@androidx.room.Database(
        entities = {Favourite.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public static final ExecutorService databaseWriteExecutor
            = Executors.newFixedThreadPool(4);

    public abstract FavouriteDao favouriteDao();
}
