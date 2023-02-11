package com.darshan09200.maps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.darshan09200.maps.databinding.FragmentMapsBinding;
import com.darshan09200.maps.model.Favourite;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.FusedLocationProviderClient;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MapsFragment extends Fragment {
    private GoogleMap mMap;
    FusedLocationProviderClient mClient;
    private FragmentMapsBinding binding;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

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
            mMap = googleMap;
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setTiltGesturesEnabled(false);

            mMap.setOnPoiClickListener(pointOfInterest -> {
                Favourite favourite = new Favourite();
                favourite.id = pointOfInterest.placeId;
                favourite.coordinate = pointOfInterest.latLng;
                favourite.name = pointOfInterest.name;
                ((MapsActivity)getActivity()).addToFavourite(favourite);
                addMarker(pointOfInterest.latLng, pointOfInterest.name, "");
            });

            mClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
            });

            googleMap.setOnMapLongClickListener(latLng -> {
                Favourite favourite = getNearestPlace(latLng);
                addMarker(favourite.coordinate, favourite.name, "");
            });
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = LocationServices.getFusedLocationProviderClient(getActivity());
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

    public void clearMap(){
        if(mMap != null){
            mMap.clear();
        }
    }

    public void addMarker(LatLng latLng, String title, String snippet) {
        MarkerOptions options = new MarkerOptions().position(latLng)
                .title(title)
                .snippet(snippet)
                .draggable(true);
        if (mMap != null) {
            mMap.addMarker(options);
        }
    }

    public void zoomAt(LatLng latLng){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f));
    }

    public Favourite getNearestPlace(LatLng latLng) {
        Favourite favourite = new Favourite();
        favourite.id = UUID.randomUUID().toString();
        favourite.name = "Favourite";
        favourite.coordinate = latLng;
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address firstAddress = addresses.get(0);
                String address = firstAddress.getAddressLine(0);
                if (firstAddress.getThoroughfare() != null)
                    address = firstAddress.getThoroughfare();
                favourite.name = address;
                favourite.coordinate = new LatLng(firstAddress.getLatitude(), firstAddress.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return favourite;
    }

}
