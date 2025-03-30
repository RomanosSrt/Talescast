package com.unipi.talescast;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    Context context;
    List<CardModel> cardList;
    OnItemClickListener listener;

    public CardAdapter(Context context, List<CardModel> cardList, OnItemClickListener listener) {
        this.context = context;
        this.cardList = cardList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tale_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        CardModel item = cardList.get(position);
        holder.title.setText(item.title);
        if (item.image != null && !item.image.isEmpty()) {
            byte[] imageBytes = Base64.decode(item.image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.image.setImageBitmap(bitmap);
        } else {
            holder.image.setImageResource(R.drawable.placeholder);
        }
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.itemImage);
            title = itemView.findViewById(R.id.itemTitle);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(CardModel item);
    }
}

