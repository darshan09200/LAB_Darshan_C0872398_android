package com.darshan09200.maps.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.darshan09200.maps.model.Favourite;

import java.util.List;

@Dao
public interface FavouriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Favourite favourite);

    @Delete
    void delete(Favourite favourite);

    @Query("SELECT * FROM favourite ORDER BY updatedAt DESC")
    LiveData<List<Favourite>> getAllFavourites();

    @Query("SELECT * FROM favourite WHERE id == :id")
    Favourite getFavourite(String id);

    @Query("SELECT * FROM favourite WHERE name == :name")
    Favourite getFavouriteByName(String name);
}
