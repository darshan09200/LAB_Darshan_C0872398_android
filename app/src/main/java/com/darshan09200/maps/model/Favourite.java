package com.darshan09200.maps.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.darshan09200.maps.helper.DateConverter;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

@Entity
public class Favourite {
    @PrimaryKey
    @NonNull
    public String id;

    public String name;

    public Double latitude;
    public Double longitude;

    @TypeConverters({DateConverter.class})
    public Date updatedAt;

    public LatLng getCoordinate(){
        return new LatLng(latitude, longitude);
    }

    public void setCoordinate(LatLng coordinate){
        latitude = coordinate.latitude;
        longitude = coordinate.longitude;
    }
}

