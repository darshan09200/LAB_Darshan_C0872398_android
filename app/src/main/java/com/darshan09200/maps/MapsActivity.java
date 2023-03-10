package com.darshan09200.maps;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.darshan09200.maps.databinding.ActivityMapsBinding;
import com.darshan09200.maps.model.Favourite;
import com.darshan09200.maps.model.FavouriteViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MapsActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 2;

    private static final String PERMISSION_FRAGMENT = "permissions";
    private static final String MAPS_FRAGMENT = "maps";
    private final List<Favourite> favourites = new ArrayList<>();
    FavouriteBottomSheetFragment favouriteBottomSheetFragment = new FavouriteBottomSheetFragment();
    private ActivityMapsBinding binding;
    private boolean locationPermissionGranted = false;
    private FavouriteViewModel favouriteViewModel;
    ActivityResultLauncher<Intent> autocompleteActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Place place = Autocomplete.getPlaceFromIntent(data);
                        if (place.getLatLng() != null) {
                            Favourite favourite = new Favourite();
                            favourite.setId(place.getId());
                            favourite.setCoordinate(place.getLatLng());
                            favourite.setName(place.getName());
                            favourite.setUpdatedAt(new Date());

                            MapsFragment mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentByTag(MAPS_FRAGMENT);
                            if (mapsFragment != null) {
                                mapsFragment.addMarker(favourite.getCoordinate(), favourite.getName(), null);
                                favourite.setUpdatedAt(new Date());
                                mapsFragment.zoomAt(favourite.getCoordinate());
                            }
                            favouriteViewModel.insert(favourite);
                        }
                    }
                }
            });
    private int selectedMapType = 0;

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

    void addObserver() {
        favouriteViewModel.getAllFavourites().observe(this, favourites -> {
            System.out.println("updated");
            this.favourites.clear();
            this.favourites.addAll(favourites);
            System.out.println(favourites.size());
            updateAllMarkers();
        });
    }

    private void updateLocationUI() {
//        Fragment permissionFragment = getSupportFragmentManager().findFragmentByTag(PERMISSION_FRAGMENT);
        Fragment mapsFragment = getSupportFragmentManager().findFragmentByTag(MAPS_FRAGMENT);
//        if (!locationPermissionGranted) {
//            if(permissionFragment == null || !permissionFragment.isVisible()) {
//                PermissionFragment fragment = new PermissionFragment();
//                fragment.locationPermissionGranted = locationPermissionGranted;
//                getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.map_container, fragment, PERMISSION_FRAGMENT)
//                        .commit();
//            }
//
//        } else
        if (mapsFragment == null || !mapsFragment.isVisible()) {
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

            MapsFragment mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentByTag(MAPS_FRAGMENT);
            if (mapsFragment != null) {
                mapsFragment.onPermissionGranted();
            }
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

                MapsFragment mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentByTag(MAPS_FRAGMENT);
                if (mapsFragment != null) {
                    mapsFragment.onPermissionGranted();
                }
            }
        } else {
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
            favouriteBottomSheetFragment.show(getSupportFragmentManager(), favouriteBottomSheetFragment.getTag());
            return true;
        } else if (id == R.id.search) {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

            // Start the autocomplete intent.
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this);
            autocompleteActivityResult.launch(intent);

            return true;
        } else if (id == R.id.layer) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Choose an animal");

            String[] mapTypes = {"Normal", "Satellite", "Terrain"};
            AtomicInteger selectedIndex = new AtomicInteger(selectedMapType);
            builder.setSingleChoiceItems(mapTypes, selectedMapType, (dialog, which) -> {
                selectedIndex.set(which);
            });
            builder.setPositiveButton("OK", (dialog, which) -> {
                selectedMapType = selectedIndex.get();
                MapsFragment mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentByTag(MAPS_FRAGMENT);
                if (mapsFragment != null) {
                    mapsFragment.setMapType(selectedMapType);
                }
            });
            builder.setNegativeButton("Cancel", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    void addToFavourite(Favourite favourite) {
        favouriteViewModel.insert(favourite);
    }

    void permissionDeclinedFallbackZoom() {
        if (favourites.size() > 0) {
            zoomAt(favourites.get(0).getCoordinate());
        }
    }

    void zoomAt(LatLng latLng) {
        if (favouriteBottomSheetFragment.isVisible()) {
            favouriteBottomSheetFragment.dismiss();
        }

        MapsFragment mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentByTag(MAPS_FRAGMENT);
        if (mapsFragment != null) {
            mapsFragment.zoomAt(latLng);
        }
    }

    void updateAllMarkers() {
        MapsFragment mapsFragment = (MapsFragment) getSupportFragmentManager().findFragmentByTag(MAPS_FRAGMENT);
        if (mapsFragment != null) {
            mapsFragment.clearMap();
            for (Favourite favourite : favourites) {
                mapsFragment.addMarker(favourite.getCoordinate(), favourite.getName(), null);
            }
        }
    }
}