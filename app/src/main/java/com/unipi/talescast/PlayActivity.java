package com.unipi.talescast;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class PlayActivity extends AppCompatActivity {
    CardModel cardSelected;
    TextToSpeech narrator;
    Button playButton;
    SeekBar seekBar;
    boolean stop = false;
    int playFrom;
    Handler handler = new Handler();
    Runnable progressLoop;
    String[] storyTable;
    int progress = 0;
    int maxProgress;
    final int INTERVAL = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_play);
        seekBar = findViewById(R.id.seekBar);
        playButton = findViewById(R.id.playButton);
        TextView titleText = findViewById(R.id.titleText);
        cardSelected = (CardModel) getIntent().getSerializableExtra("card");


        if (cardSelected != null) {
            storyTable = cardSelected.story.trim().split("(?<=[,.?!])\\s+");
            titleText.setText(cardSelected.title);
            seekBar.setMax(storyTable.length);
            maxProgress = seekBar.getMax();
            setImage(cardSelected.image);
        } else {
            finish();
        }

        narrator = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    narrator.setLanguage(new Locale("el", "GR"));
                }
            }
        });


        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!stop) {
                    narrate();
                }
                else {
                    narrator.stop();
                    narrator.shutdown();
                }

                stop = !stop;
            }
        });

        narrator.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                int index = Integer.parseInt(utteranceId.split("_")[1]);
                runOnUiThread(() -> {
                    seekBar.setProgress(index);
                    titleText.setText(String.valueOf(index + 1));
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

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                narrator.stop();
                narrator.shutdown();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                playFrom = seekBar.getProgress();
//                int continueFrom = cardSelected.story.indexOf(storyTable[playFrom]);
                String restOfTheStory = cardSelected.story.substring(playFrom);
                if (!stop) {
//                    trackTale();
                    narrator.speak(restOfTheStory, TextToSpeech.QUEUE_FLUSH, null);
                }
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
        playFrom = seekBar.getProgress();
        for (int i = playFrom; i < storyTable.length; i++) {
            String id = "sentence_" + i;
            narrator.speak(storyTable[i], TextToSpeech.QUEUE_ADD, null, id);
        }
    }
}