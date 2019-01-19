package com.epsi.mspr.activities;

import android.content.Intent;
import android.os.Bundle;

import com.epsi.mspr.R;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Runnable task= () -> {
            Intent startIntent = new Intent(SplashActivity.this, StartActivity.class);

            startActivity(startIntent);

            SplashActivity.this.finish(); // to avoid the return button to come back here
        };

        findViewById(R.id.result_icon).postDelayed(task, 5000); //pour que le thread se termine à OnDestroy sinon (avec un Handler) il faudra l'arrêter à la mains
    }
}
