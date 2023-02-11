package com.darshan09200.maps.model;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.darshan09200.maps.DateConverter;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

@Entity
public class Favourite {
    @PrimaryKey
    @NonNull
    public String id;

    public String name;

    @Embedded
    public LatLng coordinate;

    @TypeConverters({DateConverter.class})
    public Date updatedAt;
}

