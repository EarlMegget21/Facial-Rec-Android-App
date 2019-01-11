package com.epsi.mspr.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.epsi.mspr.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class TakePictureActivity extends AppCompatActivity {

    /**
     * request id
     */
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_id);

        if (ContextCompat.checkSelfPermission(TakePictureActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) { // Permission has not yet been granted
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) { // if there is a camera
                if (ActivityCompat.shouldShowRequestPermissionRationale(TakePictureActivity.this, Manifest.permission.CAMERA)) {
                    //In this case, the user rejected the first time, so we're showing an explanation to convict him then we retry
                    //after that, while he's rejeting the prompt without checking the "Don't ask again", we will run this and it'll ask
                    //him if he agree. Else, we run this but requestPermission will return reject automatically without asking him.
                    print(TakePictureActivity.this, "You rejected the permission"); //it doesn't appear because it is overridden by the next toast (convert it to modal box)
                    requestPermission(); // request the permission
                } else {
                    requestPermission(); // No explanation needed; request the permission
                }
            } else { //no camera
                print(TakePictureActivity.this, "Your device is not compatible");
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
                    print(TakePictureActivity.this, "Too bad...");
                }
                break;
            }
        }
    }

    /**
     * Method to take a photo
     */
    public void takePhoto() {
        ContentValues values = new ContentValues();
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values); //to save the taken picture on the file system

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) { // if there's an app to take a photo
            startActivityForResult(takePictureIntent, MY_PERMISSIONS_REQUEST_CAMERA); //we take the photo
        } else {
            print(TakePictureActivity.this, "You don't have any app to take the photo...");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA && resultCode == RESULT_OK) { //if we respond to the photo request
            // on doit sauvegarder l'image dans le système de fichiers plutôt que de récupérer la bitmap comme ça data.getExtras().get("data"); car les Parcelable ont une taille max, la qualité était donc diminuée donc texte illlisible

            Intent photoIntent = new Intent(TakePictureActivity.this, VerifyIDActivity.class);
            photoIntent.putExtra(VerifyIDActivity.PHOTO, imageUri); // Add the photo uri to the extras for the Intent.
            startActivity(photoIntent); //to display the photo
        }
    }

    /**
     * Method to request permissions
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(
                TakePictureActivity.this,
                new String[]{
                        Manifest.permission.CAMERA, //to take a picture
                        Manifest.permission.WRITE_EXTERNAL_STORAGE // to save it on the FileSystem
                },
                MY_PERMISSIONS_REQUEST_CAMERA
        );
    }

    /**
     * Method to print a message in a Toast
     * @param activity the context where to print the Toast
     * @param message string to write in the Toast
     */
    public static void print(Context activity, String message) {
        Toast myToast = Toast.makeText(
                activity,
                message,
                Toast.LENGTH_SHORT
        );
        myToast.show();
    }
}
