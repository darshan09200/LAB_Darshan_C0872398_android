package com.darshan09200.maps;

import android.app.AlertDialog;
import android.app.Dialog;
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
import com.darshan09200.maps.databinding.FragmentBottomsheetBinding;
import com.darshan09200.maps.helper.SwipeHelper;
import com.darshan09200.maps.helper.SwipeUnderlayButtonClickListener;
import com.darshan09200.maps.model.Favourite;
import com.darshan09200.maps.model.FavouriteViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class BottomSheetFragment extends BottomSheetDialogFragment {

    private BottomSheetDialog dialog;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private FragmentBottomsheetBinding binding;
    private FavouriteViewModel favouriteViewModel;
    private final List<Favourite> favourites = new ArrayList<>();

    private SwipeHelper swipeHelper;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        favouriteViewModel = new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication())
                .create(FavouriteViewModel.class);
        favouriteViewModel.getAllFavourites().observe(this, favourites -> {
            this.favourites.clear();
            this.favourites.addAll(favourites);
            if (binding != null) {
                binding.favouriteList.getAdapter().notifyDataSetChanged();
            }
        });

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBottomsheetBinding.inflate(inflater, container, false);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        binding.favouriteList.setLayoutManager(layoutManager);
        binding.favouriteList.setAdapter(new FavouriteAdapter(favourites));

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
                        BottomSheetFragment.this::deleteFavourite));
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
        Favourite favourite = favourites.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Are you sure you want to delete from favourite?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            Favourite deletedFavourite = favourite;
            favouriteViewModel.delete(deletedFavourite);
            binding.favouriteList.getAdapter().notifyItemRemoved(position);
            Snackbar.make(getDialog().getWindow().getDecorView(), deletedFavourite.name + " is deleted!", Snackbar.LENGTH_LONG)
                    .setAction("Undo", v -> favouriteViewModel.insert(deletedFavourite)).show();
        });
        builder.setNegativeButton("No", (dialog, which) -> binding.favouriteList.getAdapter().notifyItemChanged(position));
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
