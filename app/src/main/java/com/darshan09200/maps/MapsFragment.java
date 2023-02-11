package com.darshan09200.maps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.darshan09200.maps.api.VolleySingleton;
import com.darshan09200.maps.databinding.FragmentMapsBinding;
import com.darshan09200.maps.model.Favourite;
import com.darshan09200.maps.model.FavouriteViewModel;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MapsFragment extends Fragment implements GoogleMap.OnMarkerDragListener {
    private static final float ZOOM = 15;
    FusedLocationProviderClient mClient;
    FavouriteViewModel favouriteViewModel;
    private GoogleMap mMap;
    private FragmentMapsBinding binding;
    private LatLng userLocation;

    private Favourite toDelete;
    private boolean isCheckingGps = false;

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(final GoogleMap googleMap) {
            System.out.println("called maps ready");
            mMap = googleMap;

            googleMap.setOnPoiClickListener(pointOfInterest -> {
                Favourite favourite = new Favourite();
                favourite.id = pointOfInterest.placeId;
                favourite.setCoordinate(pointOfInterest.latLng);
                favourite.name = pointOfInterest.name;
                ((MapsActivity) getActivity()).addToFavourite(favourite);
                addMarker(pointOfInterest.latLng, pointOfInterest.name, null);
                zoomAt(pointOfInterest.latLng);
            });

            googleMap.setOnMapLongClickListener(latLng -> {
                Favourite favourite = getNearestPlace(latLng);
                addMarker(favourite.getCoordinate(), favourite.name, null);
            });

            googleMap.setOnMarkerClickListener(marker -> {
                generateDirectionDetails(marker);
                return false;
            });

            googleMap.setOnMarkerDragListener(MapsFragment.this);

            ((MapsActivity) getActivity()).updateAllMarkers();

            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setTiltGesturesEnabled(false);

            googleMap.setOnMyLocationButtonClickListener(() -> {
                userLocation = null;
                enableGPS();
                return true;
            });

            onPermissionGranted();

        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = LocationServices.getFusedLocationProviderClient(getActivity());

        favouriteViewModel = new ViewModelProvider(getActivity()).get(FavouriteViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    public void clearMap() {
        if (mMap != null) {
            mMap.clear();
        }
    }

    public void addMarker(LatLng latLng, String title, String snippet) {
        MarkerOptions options = new MarkerOptions().position(latLng).title(title).draggable(true);
        if (snippet != null) {
            options = options.snippet(snippet);
        }

        if (mMap != null) {
            mMap.addMarker(options);
        }
    }

    public void zoomAt(LatLng latLng) {
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM));
        }
    }

    public void setMapType(int mapType) {
        if (mMap != null) {
            int selectedMapType;
            switch (mapType) {
                case 2:
                    selectedMapType = GoogleMap.MAP_TYPE_TERRAIN;
                    break;
                case 1:
                    selectedMapType = GoogleMap.MAP_TYPE_HYBRID;
                    break;
                case 0:
                default:
                    selectedMapType = GoogleMap.MAP_TYPE_NORMAL;
            }
            mMap.setMapType(selectedMapType);
        }
    }

    void onPermissionGranted() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        enableGPS();
        mMap.setMyLocationEnabled(true);
        zoomToUserLocation();
    }

    void zoomToUserLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return null;
            }

            @Override
            public boolean isCancellationRequested() {
                return false;
            }
        }).addOnSuccessListener(getActivity(), location -> {
            if (location != null) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (userLocation == null) zoomAt(latLng);
                userLocation = latLng;
            }
        });
    }

    ActivityResultLauncher<IntentSenderRequest> gpsActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                if (result.getResultCode() == MapsActivity.RESULT_OK) {
                    userLocation = null;
                    zoomToUserLocation();
                }
                isCheckingGps = false;
            });


    private void enableGPS() {
        if (!isCheckingGps) {
            isCheckingGps = true;
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(5 * 1000);
            locationRequest.setFastestInterval(3 * 1000);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            Task<LocationSettingsResponse> result =
                    LocationServices.getSettingsClient(getActivity()).checkLocationSettings(builder.build());

            result.addOnCompleteListener(task -> {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    zoomToUserLocation();
                    isCheckingGps = false;

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                gpsActivityResult.launch(new IntentSenderRequest.Builder(
                                        resolvable.getResolution().getIntentSender()
                                ).setFillInIntent(new Intent())
                                        .build());
                            } catch (ClassCastException e) {
                                isCheckingGps = false;
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            isCheckingGps = false;
                            break;
                    }
                }
            });
        }
    }

    public Favourite getNearestPlace(LatLng latLng) {
        Favourite favourite = new Favourite();
        favourite.id = UUID.randomUUID().toString();
        favourite.setCoordinate(latLng);
        favourite.updatedAt = new Date();
        DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
        favourite.name = "Location: " + dateFormat.format(favourite.updatedAt);
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address firstAddress = addresses.get(0);
                String address = firstAddress.getAddressLine(0);
                if (firstAddress.getThoroughfare() != null)
                    address = firstAddress.getThoroughfare();
                favourite.name = address;
                favourite.setCoordinate(new LatLng(firstAddress.getLatitude(), firstAddress.getLongitude()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return favourite;
    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        Favourite favourite = getNearestPlace(marker.getPosition());
        marker.setPosition(favourite.getCoordinate());
        marker.setTitle(favourite.name);
        if (toDelete != null) favouriteViewModel.delete(toDelete);
        favouriteViewModel.insert(favourite);
        toDelete = null;
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {
        Favourite favourite = favouriteViewModel.getFavourite(marker.getPosition());
        if (favourite == null && !marker.getTitle().startsWith("Location:")) {
            favourite = favouriteViewModel.getFavouriteByName(marker.getTitle());
        }
        toDelete = favourite;
    }

    private String getDirectionUrl(LatLng start, LatLng end) {
        StringBuilder googleDirectionUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionUrl.append("origin=" + start.latitude + "," + start.longitude);
        googleDirectionUrl.append(("&destination=" + end.latitude + "," + end.longitude));
        googleDirectionUrl.append("&key=" + getString(com.darshan09200.maps.R.string.api_key));
        return googleDirectionUrl.toString();
    }

    private void generateDirectionDetails(Marker marker) {
        if (userLocation != null) {
            LatLng start = userLocation;
            LatLng end = marker.getPosition();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    getDirectionUrl(start, end), null,
                    response -> {
                        HashMap<String, String> details = VolleySingleton.getDirection(response);
                        marker.setSnippet("Duration: " + details.get("duration") + "; Distance:" + details.get("distance"));
                        marker.showInfoWindow();
                    },
                    null);
            VolleySingleton.getInstance(getActivity().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
        }
    }
}
