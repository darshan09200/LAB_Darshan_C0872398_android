package com.darshan09200.maps;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.darshan09200.maps.model.Favourite;
import com.darshan09200.maps.model.FavouriteViewModel;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.darshan09200.maps.databinding.ActivityMapsBinding;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MapsActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 2;

    private static final String PERMISSION_FRAGMENT = "permissions";
    private static final String MAPS_FRAGMENT = "maps";

    private ActivityMapsBinding binding;
    private boolean locationPermissionGranted = false;
    private FavouriteViewModel favouriteViewModel;
    private final List<Favourite> favourites = new ArrayList<>();

    BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
    ActivityResultLauncher<Intent> autocompleteActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Place place = Autocomplete.getPlaceFromIntent(data);
                        if (place.getLatLng() != null) {
                            Favourite favourite = new Favourite();
                            favourite.id = place.getId();
                            favourite.coordinate = place.getLatLng();
                            favourite.name = place.getName();

                            MapsFragment mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentByTag(MAPS_FRAGMENT);
                            if (mapsFragment != null) {
                                mapsFragment.addMarker(favourite.coordinate, favourite.name, "");
                                mapsFragment.zoomAt(favourite.coordinate);
                            }
                            favouriteViewModel.insert(favourite);
                        }
                    }
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        favouriteViewModel = new ViewModelProvider(this).get(FavouriteViewModel.class);

        setSupportActionBar(binding.toolbar);

        getLocationPermission();
        updateLocationUI();

        Places.initialize(getApplicationContext(), getResources().getString(R.string.api_key));
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateLocationUI();

        addObserver();
    }

    void addObserver(){
        favouriteViewModel.getAllFavourites().observe(this, favourites -> {
            System.out.println("updated");
            this.favourites.clear();
            this.favourites.addAll(favourites);
            System.out.println(favourites.size());
            updateAllMarkers();
        });
    }

    private void updateLocationUI(){
        Fragment permissionFragment = getSupportFragmentManager().findFragmentByTag(PERMISSION_FRAGMENT);
        Fragment mapsFragment = getSupportFragmentManager().findFragmentByTag(MAPS_FRAGMENT) ;
        if (!locationPermissionGranted) {
            if(permissionFragment == null || !permissionFragment.isVisible()) {
                PermissionFragment fragment = new PermissionFragment();
                fragment.locationPermissionGranted = locationPermissionGranted;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.map_container, fragment, PERMISSION_FRAGMENT)
                        .commit();
            }

        } else if (mapsFragment == null || !mapsFragment.isVisible()){
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
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
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
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else if (requestCode == AUTOCOMPLETE_REQUEST_CODE){
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.favourite) {
            bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
            return true;
        } else if (id == R.id.search){
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

            // Start the autocomplete intent.
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this);
            autocompleteActivityResult.launch(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void addToFavourite(Favourite favourite){
        favouriteViewModel.insert(favourite);
    }

    void zoomAt(LatLng latLng){
        bottomSheetFragment.dismiss();

        MapsFragment mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentByTag(MAPS_FRAGMENT);
        if(mapsFragment != null) {
            mapsFragment.zoomAt(latLng);
        }
    }

    void updateAllMarkers(){
        MapsFragment mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentByTag(MAPS_FRAGMENT);
        if(mapsFragment != null) {
            mapsFragment.clearMap();
            for (Favourite favourite : favourites) {
                mapsFragment.addMarker(favourite.coordinate, favourite.name, "");
            }
        }
    }
}