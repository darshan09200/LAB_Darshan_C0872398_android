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
    private String id;

    private String name;

    private Double latitude;
    private Double longitude;

    @TypeConverters({DateConverter.class})
    private Date updatedAt;

    public LatLng getCoordinate() {
        return new LatLng(latitude, longitude);
    }

    public void setCoordinate(LatLng coordinate) {
        latitude = coordinate.latitude;
        longitude = coordinate.longitude;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.replace("\n", " ");
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}

