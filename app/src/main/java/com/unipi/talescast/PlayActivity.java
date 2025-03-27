package com.unipi.talescast;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class PlayActivity extends AppCompatActivity {
    CardModel cardSelected;
    TextToSpeech narrator;
    Button playButton;
    boolean stop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_play);
        narrator = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    narrator.setLanguage(new Locale("el", "GR"));
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cardSelected = (CardModel) getIntent().getSerializableExtra("card");

        if (cardSelected != null) {
            TextView titleText = findViewById(R.id.titleText);
            titleText.setText(cardSelected.title);

            // Example: Decode the image
            ImageView imageView = findViewById(R.id.taleImage);
            byte[] bytes = Base64.decode(cardSelected.image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imageView.setImageBitmap(bitmap);
        } else {

        }
        playButton = findViewById(R.id.playButton);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (stop)
                    narrator.speak(cardSelected.story, TextToSpeech.QUEUE_FLUSH, null);
                else
                    narrator.stop();
                stop = !stop;
            }
        });

    }
}