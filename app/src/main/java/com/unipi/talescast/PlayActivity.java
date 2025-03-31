package com.unipi.talescast;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PlayActivity extends AppCompatActivity {
    private CardModel cardSelected;
    private TextToSpeech narrator;
    private TextView lyrics, year;
    private SeekBar seekBar;
    private boolean stop = false;
    private String[] storyTable;
    private final DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    private final FirebaseAuth creds = FirebaseAuth.getInstance();
    private String userId;
    private final Map<String, Object> data = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_play);
        seekBar = findViewById(R.id.seekBar);
        ImageButton playButton = findViewById(R.id.playButton);
        TextView titleText = findViewById(R.id.titleText);
        lyrics = findViewById(R.id.storyText);
        cardSelected = (CardModel) getIntent().getSerializableExtra("card");

        if (creds.getCurrentUser() != null)
            userId = creds.getCurrentUser().getUid();
        else {
            Toast.makeText(this, getString(R.string.credentials_error), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(PlayActivity.this, MainActivity.class);
            startActivity(intent);
        }



        int orientation = getResources().getConfiguration().orientation;
        if (cardSelected != null) {
            storyTable = cardSelected.story.trim().split("(?<=[:;,.?!])\\s+");
            titleText.setText(cardSelected.title);
            lyrics.setText(cardSelected.story);
            seekBar.setMax(storyTable.length-1);
            setImage(cardSelected.image);
            String buffer = getString(R.string.year_written) + cardSelected.year;
            registerEvent(cardSelected.title, "readTales");
            if (orientation != Configuration.ORIENTATION_LANDSCAPE) {
                year = findViewById(R.id.yeartextView);
                year.setText(buffer);
            }
        } else {
            Toast.makeText(this, getString(R.string.tale_error), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(PlayActivity.this, Stories.class);
            startActivity(intent);
        }

        narrator = new TextToSpeech(this, i -> {
            if (i != TextToSpeech.ERROR) {
                narrator.setLanguage(new Locale("el", "GR"));
            }
        });


        playButton.setOnClickListener(view -> {
            if (seekBar.getProgress() == seekBar.getMax())
                seekBar.setProgress(0);
            if (!stop) {
                narrate();
                playButton.setBackground(ContextCompat.getDrawable(this, R.drawable.pause));
            }
            else {
                narrator.stop();
                playButton.setBackground(ContextCompat.getDrawable(this, R.drawable.play));
            }
            stop = !stop;
        });

        narrator.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                int index = Integer.parseInt(utteranceId.split("_")[1]);
                runOnUiThread(() -> {
                    seekBar.setProgress(index);
                    int findLyrics = lyrics.getText().toString().indexOf(storyTable[index]);
                    SpannableString spannable = new SpannableString(lyrics.getText());
                    spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#aaaaaa")), 0, lyrics.getText().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new StyleSpan(Typeface.NORMAL), 0, lyrics.getText().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FFFFFF")), findLyrics, findLyrics+storyTable[index].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new StyleSpan(Typeface.BOLD), findLyrics, findLyrics+storyTable[index].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    lyrics.setText(spannable);
                });
            }

            @Override
            public void onDone(String utteranceId) {}

            @Override
            public void onError(String utteranceId) {}
        });



        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i == seekBar.getMax()) {
                    new Handler().postDelayed(() -> {
                        Spannable spannable = new SpannableString(lyrics.getText());
                        spannable.setSpan(
                                new ForegroundColorSpan(Color.parseColor("#aaaaaa")),
                                0,
                                spannable.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                        lyrics.setText(spannable);
                    }, 5000);
                    registerEvent(cardSelected.title, "listenedTales");
                    db.child("users").child("user_"+userId).child("lastPlayed").setValue(cardSelected.title);
                    playButton.setBackground(ContextCompat.getDrawable(PlayActivity.this, R.drawable.play));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                narrator.stop();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (stop)
                    narrate();

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        narrator.stop();
        narrator.shutdown();
    }

    @Override
    protected void onStop() {
        super.onStop();
        narrator.stop();
        narrator.shutdown();
    }

    private void setImage(String b64){
        ImageView imageView = findViewById(R.id.taleImage);
        byte[] bytes = Base64.decode(b64, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        imageView.setImageBitmap(bitmap);
    }

    private void narrate() {
        int playFrom = seekBar.getProgress();
        for (int i = playFrom; i < storyTable.length; i++) {
            String id = "sentence_" + i;
            narrator.speak(storyTable[i], TextToSpeech.QUEUE_ADD, null, id);
        }
    }

    private void registerEvent(String taleTitle, String eventName) {
        data.put("count", ServerValue.increment(1));
        db.child("users").child("user_"+userId).child(eventName).child(taleTitle).updateChildren(data);
    }
}