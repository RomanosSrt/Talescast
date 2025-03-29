package com.unipi.talescast;

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
    TextView titleText,lyrics;
    SeekBar seekBar;
    boolean stop = false;
    int playFrom;
    String[] storyTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_play);
        seekBar = findViewById(R.id.seekBar);
        playButton = findViewById(R.id.playButton);
        titleText = findViewById(R.id.titleText);
        lyrics = findViewById(R.id.storyText);
        cardSelected = (CardModel) getIntent().getSerializableExtra("card");


        if (cardSelected != null) {
            storyTable = cardSelected.story.trim().split("(?<=[:,.?!])\\s+");
            titleText.setText(cardSelected.title);
            lyrics.setText(cardSelected.story);
            seekBar.setMax(storyTable.length-1);
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
                if (seekBar.getProgress() == seekBar.getMax())
                    seekBar.setProgress(0);

                if (!stop) {
                    narrate();
                }
                else {
                    narrator.stop();
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
            public void onDone(String utteranceId) {

            }

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
                                new ForegroundColorSpan(Color.BLACK),
                                0,
                                spannable.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                        lyrics.setText(spannable);
                    }, 5000);
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
        playFrom = seekBar.getProgress();
        for (int i = playFrom; i < storyTable.length; i++) {
            String id = "sentence_" + i;
            narrator.speak(storyTable[i], TextToSpeech.QUEUE_ADD, null, id);
        }
    }
}