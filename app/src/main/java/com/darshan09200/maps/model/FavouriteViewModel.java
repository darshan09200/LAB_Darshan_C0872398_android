package com.darshan09200.maps.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.darshan09200.maps.data.DatabaseClient;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class FavouriteViewModel extends AndroidViewModel {
    private final DatabaseClient databaseClient;
    private final LiveData<List<Favourite>> allFavourites;

    public FavouriteViewModel(@NonNull Application application) {
        super(application);

        databaseClient = new DatabaseClient(application);
        allFavourites = databaseClient.getAllFavourites();
    }

    public LiveData<List<Favourite>> getAllFavourites() {return allFavourites;}

    public Favourite getFavourite(String id) {return databaseClient.getFavourite(id);}
    public Favourite getFavouriteByName(String name) {return databaseClient.getFavouriteByName(name);}
    public Favourite getFavourite(LatLng coordinate) {return databaseClient.getFavourite(coordinate);}

    public void insert(Favourite favourite) {
        favourite.name = favourite.name.replace("\n", " ");
        databaseClient.insert(favourite);}
    
    public void delete(Favourite favourite) {databaseClient.delete(favourite);}
}
