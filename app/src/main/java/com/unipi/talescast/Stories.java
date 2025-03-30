package com.unipi.talescast;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Stories extends AppCompatActivity {
    DatabaseReference dbRef;
    RecyclerView recyclerView;
    public List<CardModel> cardItems;
    CardAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stories);

        recyclerView = findViewById(R.id.recyclerView);
        int orientation = getResources().getConfiguration().orientation;


        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        cardItems = new ArrayList<>();
        adapter = new CardAdapter(this, cardItems, item -> {
            Intent intent = new Intent(Stories.this, PlayActivity.class);
            intent.putExtra("card", item);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        dbRef = FirebaseDatabase.getInstance().getReference("cards");

        dbRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cardItems.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    CardModel item = snap.getValue(CardModel.class);
                    cardItems.add(item);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Stories.this, getString(R.string.data_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showStats(View view) {
        Intent intent = new Intent(Stories.this, Stats.class);
        startActivity(intent);
    }
}
