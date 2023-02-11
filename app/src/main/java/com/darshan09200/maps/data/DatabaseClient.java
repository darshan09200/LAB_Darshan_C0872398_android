package com.darshan09200.maps.data;

import android.content.Context;
import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.darshan09200.maps.model.Favourite;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DatabaseClient {
    private static DatabaseClient mInstance;
    private final AppDatabase appDatabase;
    private final Context context;
    private final FavouriteDao favouriteDao;

    private final LiveData<List<Favourite>> allFavourites;

    public DatabaseClient(Context context) {
        this.context = context;

        //creating the app database with Room database builder
        //MyToDos is the name of the database
        appDatabase = Room.databaseBuilder(this.context, AppDatabase.class, "Maps").build();

        favouriteDao = appDatabase.favouriteDao();
        allFavourites = favouriteDao.getAllFavourites();
    }

    public static synchronized DatabaseClient getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DatabaseClient(context);
        }
        return mInstance;
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }

    public LiveData<List<Favourite>> getAllFavourites() {
        return allFavourites;
    }

    public Favourite getFavourite(String id) {
        Future<Favourite> future = AppDatabase.databaseWriteExecutor.submit(() -> {
            return favouriteDao.getFavourite(id);
        });
        Favourite result = null;
        try {
            result = future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Favourite getFavouriteByName(String name) {
        Future<Favourite> future = AppDatabase.databaseWriteExecutor.submit(() -> {
            return favouriteDao.getFavouriteByName(name);
        });
        Favourite result = null;
        try {
            result = future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Favourite getFavourite(LatLng coordinate) {
        Favourite result = null;
        Location startPoint = new Location("start");
        startPoint.setLatitude(coordinate.latitude);
        startPoint.setLongitude(coordinate.longitude);
        for (Favourite favourite : allFavourites.getValue()) {

            Location endPoint = new Location("end");
            endPoint.setLatitude(favourite.getLatitude());
            endPoint.setLongitude(favourite.getLongitude());

            double distance = startPoint.distanceTo(endPoint);
            System.out.println(distance);
            if (distance < 100) {
                result = favourite;
                break;
            }
        }
        return result;
    }


    public void insert(Favourite favourite) {
        AppDatabase.databaseWriteExecutor.execute(() -> favouriteDao.insert(favourite));
    }

    public void delete(Favourite favourite) {
        AppDatabase.databaseWriteExecutor.execute(() -> favouriteDao.delete(favourite));
    }

}
