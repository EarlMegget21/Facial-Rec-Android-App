package com.epsi.mspr.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.epsi.mspr.R;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

public class StartActivity extends AppCompatActivity {

    @BindView(R.id.buttonStart)
    TextView buttonStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        ButterKnife.bind(this); //to inject the View objects

        buttonStart.setOnClickListener(view -> {
            Intent createIntent = new Intent(StartActivity.this, VerifyIDActivity.class);
            startActivity(createIntent);
        });
    }

}
