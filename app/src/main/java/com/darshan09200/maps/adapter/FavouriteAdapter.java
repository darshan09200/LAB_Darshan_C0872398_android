package com.darshan09200.maps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.darshan09200.maps.databinding.FavouriteItemBinding;
import com.darshan09200.maps.model.Favourite;

import java.util.List;

public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.ViewHolder> {

    private final List<Favourite> favourites;
    private final OnItemClickListener onItemClickListener;

    public FavouriteAdapter(List<Favourite> favourites, OnItemClickListener onItemClickListener) {
        this.favourites = favourites;
        this.onItemClickListener = onItemClickListener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(FavouriteItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        Favourite favourite = favourites.get(position);

        viewHolder.binding.favouriteName.setText((favourite.getName()));
    }

    @Override
    public int getItemCount() {
        return favourites.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        FavouriteItemBinding binding;

        public ViewHolder(@NonNull FavouriteItemBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
            binding.getRoot().setOnClickListener(v -> {
                onItemClickListener.onItemClick(getAdapterPosition());
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
