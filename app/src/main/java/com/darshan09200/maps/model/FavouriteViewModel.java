package com.darshan09200.maps.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.darshan09200.maps.data.DatabaseClient;

import java.util.List;

public class FavouriteViewModel extends AndroidViewModel {
    private DatabaseClient databaseClient;
    private final LiveData<List<Favourite>> allFavourites;

    public FavouriteViewModel(@NonNull Application application) {
        super(application);

        databaseClient = new DatabaseClient(application);
        allFavourites = databaseClient.getAllFavourites();
    }

    public LiveData<List<Favourite>> getAllFavourites() {return allFavourites;}

    public LiveData<Favourite> getFavourite(long id) {return databaseClient.getFavourite(id);}
    
    public void insert(Favourite favourite) {
        favourite.name = favourite.name.replace("\n", " ");
        databaseClient.insert(favourite);}
    
    public void delete(Favourite favourite) {databaseClient.delete(favourite);}
}
