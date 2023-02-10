package com.darshan09200.maps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.darshan09200.maps.databinding.ActivityMapsBinding;

public class MapsActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private static final String PERMISSION_FRAGMENT = "permissions";
    private static final String MAPS_FRAGMENT = "maps";

    private ActivityMapsBinding binding;
    private boolean locationPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        getLocationPermission();
        updateLocationUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateLocationUI();
    }

    private void updateLocationUI(){
        System.out.println("locationPermissionGranted"+locationPermissionGranted);
        if (!locationPermissionGranted) {
            PermissionFragment fragment = new PermissionFragment();
            fragment.locationPermissionGranted = locationPermissionGranted;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.map_container, fragment, PERMISSION_FRAGMENT)
                    .commit();

        } else {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.map_container, new MapsFragment(), MAPS_FRAGMENT)
                    .commit();
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        System.out.println("check");
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            System.out.println("granted");
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        System.out.println(requestCode);
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }
}