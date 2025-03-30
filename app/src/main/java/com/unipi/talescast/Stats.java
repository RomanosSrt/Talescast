package com.unipi.talescast;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Stats extends AppCompatActivity {

    private TextView lastPlayedText;
    private BarChart playCountChart, readCountChart;
    private final FirebaseAuth creds = FirebaseAuth.getInstance();
    private final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
    private String userId;
    private StatsModel userData = new StatsModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stats);
        ProgressBar loadingSpinner = findViewById(R.id.loadingSpinner);
        ScrollView contentScroll = findViewById(R.id.contentScroll);

        loadingSpinner.setVisibility(View.VISIBLE);
        contentScroll.setVisibility(View.GONE);
/*        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/

        if (creds.getCurrentUser() != null)
            userId = creds.getCurrentUser().getUid();
        else {
            Toast.makeText(this, "Something went wrong with your credentials.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Stats.this, MainActivity.class);
            startActivity(intent);
        }

        userRef.child("user_"+userId).addListenerForSingleValueEvent (new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userData = snapshot.getValue(StatsModel.class);
                loadingSpinner.setVisibility(View.GONE);
                contentScroll.setVisibility(View.VISIBLE);
                showMockStats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        lastPlayedText = findViewById(R.id.lastPlayedText);
        playCountChart = findViewById(R.id.playCountChart);
        readCountChart = findViewById(R.id.readCountChart);


    }

    private void showMockStats() {
        if (userData == null || userData.getListenedTales() == null) return;

        configChart(userData.getListenedTales(), "Listened tales", "#177bbb", playCountChart);
        configChart(userData.getReadTales(), "Read tales", "#cc8405", readCountChart);
        // Update the last played text
        lastPlayedText.setText("Last Played: " + userData.getLastPlayed());
    }

    private void setupChart(BarChart chart, String[] labels) {
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setGranularityEnabled(true);
        chart.getXAxis().setDrawGridLines(false);

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setTextSize(14f);
        chart.setFitBars(true);
        chart.invalidate(); // refresh
    }


    private void configChart(Map<String, TaleStat> map, String label, String color, BarChart chart) {
        //userData.getListenedTales();

        List<BarEntry> Entries = new ArrayList<>();
        List<String> taleLabels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, TaleStat> entry : map.entrySet()) {
            String title = entry.getKey();
            int count = entry.getValue().getCount();

            taleLabels.add(title);
            Entries.add(new BarEntry(index, count));
            index++;
        }
        String[] labelsArray = taleLabels.toArray(new String[0]);

        BarDataSet playDataSet = new BarDataSet(Entries, label);
        playDataSet.setColor(Color.parseColor(color));

        BarData playData = new BarData(playDataSet);
        playData.setBarWidth(0.4f);
        chart.setData(playData);

        setupChart(chart, labelsArray);
    }
}