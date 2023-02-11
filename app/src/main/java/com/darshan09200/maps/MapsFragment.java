package com.darshan09200.maps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.darshan09200.maps.databinding.FragmentMapsBinding;
import com.darshan09200.maps.model.Favourite;
import com.darshan09200.maps.model.FavouriteViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
            googleMap.setOnMarkerDragListener(MapsFragment.this);

            ((MapsActivity) getActivity()).updateAllMarkers();


            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ((MapsActivity) getActivity()).permissionDeclinedFallbackZoom();
                return;
            }

            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setTiltGesturesEnabled(false);

            mClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if (userLocation == null)
                        zoomAt(latLng);
                    userLocation = latLng;
                }
            });

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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMapsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
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
        MarkerOptions options = new MarkerOptions().position(latLng)
                .title(title)
                .draggable(true);
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
}
