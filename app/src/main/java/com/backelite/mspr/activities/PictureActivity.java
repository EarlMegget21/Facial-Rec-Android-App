package com.backelite.mspr.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.backelite.mspr.R;

public class PictureActivity extends AppCompatActivity {

    /**
     * request id
     */
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        if (ContextCompat.checkSelfPermission(PictureActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) { // Permission has not yet been granted
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) { // if there is a camera
                if (ActivityCompat.shouldShowRequestPermissionRationale(PictureActivity.this, Manifest.permission.CAMERA)) {
                    //In this case, the user rejected the first time, so we're showing an explanation to convict him then we retry
                    //after that, while he's rejeting the prompt without checking the "Don't ask again", we will run this and it'll ask
                    //him if he agree. Else, we run this but requestPermission will return reject automatically without asking him.
                    print("You rejected the permission"); //it doesn't appear because it is overridden by the next toast (convert it to modal box)
                    requestPermission(); // request the permission
                } else {
                    requestPermission(); // No explanation needed; request the permission
                }
            } else { //no camera
                print("Your device is not compatible");
            }
        } else { // Permission has already been granted before
            takePhoto();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty so we check if it's not empty and if it was granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // permission was granted
                    takePhoto();
                } else { // permission denied
                    print("Too bad...");
                }
            }
        }
    }

    public void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) { // if there's an app to take a photo
            startActivityForResult(takePictureIntent, MY_PERMISSIONS_REQUEST_CAMERA); //we take the photo
        } else {
            print("You don't have any app to take the photo...");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA && resultCode == RESULT_OK) { //if we respond to the photo request

            Intent photoIntent = new Intent((Context) PictureActivity.this, ThirdActivity.class);

            //get the photo
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data"); //Bitmap implements Parcelable so we can pass it to the Intent

            // Add the photo to the extras for the Intent.
            photoIntent.putExtra(ThirdActivity.PHOTO, imageBitmap); //key->value
//TODO: trouver un moyen pour prendre une photo et pas une vidéo
            startActivity(photoIntent); //to display the photo
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                PictureActivity.this,
                new String[]{Manifest.permission.CAMERA},
                MY_PERMISSIONS_REQUEST_CAMERA
        );
    }

    public void print(String message) {
        Toast myToast = Toast.makeText(
                (Context) PictureActivity.this,
                message,
                Toast.LENGTH_SHORT
        );
        myToast.show();
    }
}
