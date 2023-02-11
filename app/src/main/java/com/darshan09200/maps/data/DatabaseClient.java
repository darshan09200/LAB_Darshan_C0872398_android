package com.darshan09200.maps.data;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.darshan09200.maps.model.Favourite;

import java.util.List;

public class DatabaseClient {
    private AppDatabase appDatabase;
    private Context context;
    private static DatabaseClient mInstance;
    private FavouriteDao favouriteDao;

    private LiveData<List<Favourite>> allFavourites;

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

    public LiveData<Favourite> getFavourite(long id) {return favouriteDao.getFavourite(id);}


    public String insert(Favourite favourite) {
        AppDatabase.databaseWriteExecutor.execute(() -> favouriteDao.insert(favourite));
        return favourite.id;
    }

    public void delete(Favourite favourite){
        AppDatabase.databaseWriteExecutor.execute(() -> favouriteDao.delete(favourite));
    }

}
