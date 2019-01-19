package com.epsi.mspr.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.epsi.mspr.R;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FacePictureActivity extends AppCompatActivity {

    @BindView(R.id.buttonStart)
    TextView buttonStart;
    @BindView(R.id.actionInstruction)
    TextView actionInstruction;
    @BindView(R.id.orientationInstruction)
    TextView orientationInstruction;
    @BindView(R.id.orientationArrow)
    ImageView orientationArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        ButterKnife.bind(this); //to inject the View objects

        actionInstruction.setText("Visiteur non connu, veuillez prendre une photo de son visage afin de vérifier l'authenticité de sa carte d'identité.");
        orientationInstruction.setText("Orienter le visage dans ce sens en portrait");
        orientationArrow.setImageResource(R.drawable.up_arrow);

        buttonStart.setOnClickListener(view -> {
            setResult(RESULT_OK);
            finish();
        });
    }
}
