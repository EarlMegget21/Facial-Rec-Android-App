package com.epsi.mspr.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.epsi.mspr.R;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

public class StartActivity extends AppCompatActivity {

    @BindView(R.id.buttonStartStart)
    TextView buttonStartStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        ButterKnife.bind(this); //to inject the View objects

        buttonStartStart.setOnClickListener(view -> {
            Intent createIntent = new Intent((Context) StartActivity.this, TakePictureActivity.class);
            startActivity(createIntent);
        });
    }

}
