package com.darshan09200.maps;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.darshan09200.maps.databinding.FragmentPermissionBinding;

public class PermissionFragment extends Fragment {

    FragmentPermissionBinding binding;
    boolean locationPermissionGranted = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPermissionBinding.inflate(inflater, container, false);

        binding.settingsButton.setOnClickListener(v -> {
            final Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:"+ getActivity().getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            getActivity().startActivity(intent);
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateLocationUi();
    }

    void updateLocationUi(){
        String message = "";
        if (locationPermissionGranted) {
            message = "Permission Granted";
        } else {
            message = "Permission Denied";
        }
        binding.permissionMessage.setText(message);
    }


}