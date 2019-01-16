package com.epsi.mspr.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.epsi.mspr.R;
import com.epsi.mspr.archi.view_models.VerifyIDViewModel;
import com.epsi.mspr.models.DisplayedModel;
import com.epsi.mspr.models.IDCard;
import com.google.android.gms.vision.Frame;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;

public class VerifyIDActivity extends AppCompatActivity {

    @BindView(R.id.imageView)
    ImageView mImageView;
    @BindView(R.id.infos)
    TextView infos;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    /**
     * Request id
     */
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    /**
     * The URI of the image on the file system
     */
    private Uri imageUri;

    public VerifyIDViewModel viewModel;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_id);

        viewModel = ViewModelProviders.of(this).get(VerifyIDViewModel.class); // to inject the view model

        ButterKnife.bind(this); //to inject the view elements

        if (ContextCompat.checkSelfPermission(VerifyIDActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) { // Permission has not yet been granted
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) { // if there is a camera
                if (ActivityCompat.shouldShowRequestPermissionRationale(VerifyIDActivity.this, Manifest.permission.CAMERA)) {
                    //In this case, the user rejected the first time, so we're showing an explanation to convict him then we retry
                    //after that, while he's rejeting the prompt without checking the "Don't ask again", we will run this and it'll ask
                    //him if he agree. Else, we run this but requestPermission will return reject automatically without asking him.
                    print(VerifyIDActivity.this, "You rejected the permission"); //it doesn't appear because it is overridden by the next toast (convert it to modal box)
                    requestPermission(); // request the permission
                } else {
                    requestPermission(); // No explanation needed; request the permission
                }
            } else { //no camera
                print(VerifyIDActivity.this, "Your device is not compatible");
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
                    print(VerifyIDActivity.this, "Too bad...");
                }
                break;
            }
        }
    }

    /**
     * Method to take a photo.
     */
    public void takePhoto() {
        ContentValues values = new ContentValues();
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values); // to save the taken picture on the file system

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) { // if there's an app to take a photo
            startActivityForResult(takePictureIntent, MY_PERMISSIONS_REQUEST_CAMERA); // we take the photo
        } else {
            print(VerifyIDActivity.this, "You don't have any app to take the photo...");
        }

    }

    /**
     * When the picture is taken, we extract the information and check it.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA && resultCode == RESULT_OK) { //if we respond to the photo request
            // we have to save the image on the file system instead of get the bitmap with data.getExtras().get("data"); because Parcelable objects have a max size, in fact quality was lower and the text unreadable
            checkInformation(imageUri);
        }
    }

    /**
     * Method to request permissions.
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(
                VerifyIDActivity.this,
                new String[]{
                        Manifest.permission.CAMERA, //to take a picture
                        Manifest.permission.WRITE_EXTERNAL_STORAGE // to save/delete it on the FileSystem
                },
                MY_PERMISSIONS_REQUEST_CAMERA
        );
    }

    /**
     * Method to print a message in a Toast
     *
     * @param activity the context where to print the Toast
     * @param message  string to write in the Toast
     */
    public static void print(Context activity, String message) {
        Toast.makeText(
                activity,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    /**
     * Method to extract face picture and text information from the ID card picture
     * and verify if the ID card number already exists.
     *
     * @param imageUri ID card picture URI
     */
    public void checkInformation(Uri imageUri) {
        LiveData<DisplayedModel<IDCard>> idCardViewStateLiveData = viewModel.getIDCardViewState();
        idCardViewStateLiveData.observe(this, idCardViewState -> { // subscribe to the livedata containing the result of the get request
            if (idCardViewState.getErrorCode() == DisplayedModel.NOT_FOUND) { // if the user was not found in the database, we deny the access, we display a message and we take a second picture to verify the validity of the ID card
                infos.setText("Accès refusé.");
                viewModel.insert(idCardViewState.getSuccessObject().getLastName(), idCardViewState.getSuccessObject().getFirstName(), idCardViewState.getSuccessObject().getIDNumber());
            } else if (idCardViewState.getErrorCode() == DisplayedModel.NO_ERROR) { // if there was no error, we display that the access is granted
                infos.setText("Accès autorisé.");
            } else { // in other cases, the error orgin can be anything but most of the cases, it can be caused by the lack of internet connection
                tryAgain("Impossible de vérifier l'authenticité de l'utilisateur. Veuillez vérifier votre connexion.");
            }
        });

        Bitmap picture;
        try {
            picture = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri); // get the picture from the URI where we saved it
            getContentResolver().delete(imageUri, null, null); // delete the image after we've created the Bitmap because it is not useful anymore
        } catch (IOException e) {
            e.printStackTrace();
            tryAgain("An error occured, try again.");
            return;
        }

        Frame frame = new Frame.Builder().setBitmap(picture).build(); // create a Frame to be used by both detectors

        /* Text extraction */
        viewModel.extractText(frame)
                .observe(this,
                        card -> {
                            if (card.getErrorCode() == DisplayedModel.NO_ERROR) {
                                viewModel.verifyIDCard(card.getSuccessObject().getIDNumber()); // verify if this card number exists in the database, the livedata will be notified with the result
                            } else {
                                tryAgain(card.getErrorMessage());
                            }
                        });

        /* Face detection */
        viewModel.extractFace(frame, picture)
                .observe(this,
                        face -> {
                            if (face.getErrorCode() == DisplayedModel.NO_ERROR) {
                                mImageView.setImageBitmap(face.getSuccessObject());
                                progressBar.setVisibility(View.INVISIBLE);
                            } else {
                                tryAgain(face.getErrorMessage());
                            }
                        });

        //TODO: si les infos ne sont pas trouvées alors prendre une autre photo et comparer la nouvelle photo avec celle qu'on a extrait de la carte
        //TODO: si les images matchent, enregistrer le text sur Firebase sinon message d'erreur et revenir à la première activité
    }

    /**
     * Returns to the start activity and displays an error message in a Toast.
     * @param message to display
     */
    public void tryAgain(String message) {
        Intent intent = new Intent(VerifyIDActivity.this, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP); //pop every activities from the stack to avoid unwanted behaviour when clicking "previous" button
        startActivity(intent);
        print(getApplication(), message); // inform the user what's the problem
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VerifyIDActivity.this.finish(); // to avoid the return button to come back here
    }
}
