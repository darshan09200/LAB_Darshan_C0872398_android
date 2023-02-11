package com.darshan09200.maps;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.darshan09200.maps.adapter.FavouriteAdapter;
import com.darshan09200.maps.databinding.FragmentFavouriteBottomsheetBinding;
import com.darshan09200.maps.helper.SwipeHelper;
import com.darshan09200.maps.model.Favourite;
import com.darshan09200.maps.model.FavouriteViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.divider.MaterialDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class FavouriteBottomSheetFragment extends BottomSheetDialogFragment implements FavouriteAdapter.OnItemClickListener {

    private BottomSheetDialog dialog;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private FragmentFavouriteBottomsheetBinding binding;
    private FavouriteViewModel favouriteViewModel;
    private final List<Favourite> favourites = new ArrayList<>();

    private SwipeHelper swipeHelper;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        favouriteViewModel = new ViewModelProvider(getActivity()).get(FavouriteViewModel.class);
        favouriteViewModel.getAllFavourites().observe(this, favourites -> {
            System.out.println("updated bo");
            this.favourites.clear();
            this.favourites.addAll(favourites);
            if (binding != null) {
                binding.favouriteList.getAdapter().notifyDataSetChanged();
            }
            System.out.println(favourites.size());
        });

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavouriteBottomsheetBinding.inflate(inflater, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        binding.favouriteList.setLayoutManager(layoutManager);
        binding.favouriteList.setAdapter(new FavouriteAdapter(favourites, this));

        MaterialDividerItemDecoration dividerItemDecoration = new MaterialDividerItemDecoration(binding.favouriteList.getContext(),
                layoutManager.getOrientation());
        binding.favouriteList.addItemDecoration(dividerItemDecoration);

        swipeHelper = new SwipeHelper(getActivity(), 150, binding.favouriteList) {
            @Override
            protected void instantiateSwipeButton(RecyclerView.ViewHolder viewHolder, List<SwipeUnderlayButton> swipeUnderlayButtons) {
                swipeUnderlayButtons.add(new SwipeUnderlayButton(getActivity(),
                        "Delete",
                        R.drawable.delete,
                        30,
                        0,
                        Color.parseColor("#f44336"),
                        SwipeDirection.LEFT,
                        FavouriteBottomSheetFragment.this::deleteFavourite));
            }
        };
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
    }

    private void deleteFavourite(int position) {
        System.out.println("called position");
        Favourite favourite = favourites.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Are you sure you want to delete from favourite?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            favouriteViewModel.delete(favourite);
            binding.favouriteList.getAdapter().notifyItemRemoved(position);
            Toast.makeText(getActivity(), favourite.name + " is deleted!", Toast.LENGTH_LONG);
        });
        builder.setNegativeButton("No", (dialog, which) -> binding.favouriteList.getAdapter().notifyItemChanged(position));
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onItemClick(int position) {
        Favourite favourite = favourites.get(position);

        ((MapsActivity) getActivity()).zoomAt(favourite.coordinate);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);

        ((MapsActivity)getActivity()).addObserver();
    }

}
