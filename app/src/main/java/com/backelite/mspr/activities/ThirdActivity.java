package com.backelite.mspr.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;

import com.backelite.mspr.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ThirdActivity extends AppCompatActivity{

    @BindView(R.id.imageView)
    ImageView mImageView; //on les met en attribut pour être accessibles dans les Listener sinon il faudrait les déclarer en final car en final la variable a un scope plus large

    public static final String PHOTO = "photo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        ButterKnife.bind(this);

        Bitmap bitmap = getIntent().getParcelableExtra(PHOTO);

        mImageView.setImageBitmap(bitmap);

    }


}
