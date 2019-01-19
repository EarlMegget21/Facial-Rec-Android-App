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

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;

public class VerifyIDActivity extends AppCompatActivity {

    @BindView(R.id.result_icon)
    ImageView resultLogo;
    @BindView(R.id.result_message)
    TextView resultMessage;
    @BindView(R.id.loading_icon)
    ProgressBar loadingLogo;
    @BindView(R.id.loadgin_text)
    TextView loadingText;

    /**
     * Request id to take a picture of the ID card.
     */
    public static final int MY_PERMISSION_REQUEST_CAMERA_ID_PICTURE = 1;

    /**
     * Id of the activity to ask to take a second picture
     */
    public static final int ID_CONFIRM_ACTIVITY_FACE_PICTURE = 2;

    /**
     * Id of the camera activity to take a picture of the face.
     */
    public static final int ID_CAMERA_ACTIVITY_FACE_PICTURE = 3;

    /**
     * The URI of the image on the file system
     */
    private Uri imageUri;

    /**
     * View model to do some actions.
     */
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
            takePhoto(MY_PERMISSION_REQUEST_CAMERA_ID_PICTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CAMERA_ID_PICTURE: {
                // If request is cancelled, the result arrays are empty so we check if it's not empty and if it was granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // permission was granted
                    takePhoto(MY_PERMISSION_REQUEST_CAMERA_ID_PICTURE);
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
    public void takePhoto(int photoCode) {
        // we have to save the image on the file system instead of get the bitmap with data.getExtras().get("data"); because Parcelable objects have a max size, in fact quality was lower and the text unreadable
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues()); // to save the taken picture on the file system
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); // to save the URI picture at the given field
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) { // if there's an app to take a photo
            startActivityForResult(takePictureIntent, photoCode); // we take the photo
        } else {
            tryAgain("You don't have any app to take the photo...");
        }
    }

    /**
     * When the picture is taken, we extract the information and check it.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_PERMISSION_REQUEST_CAMERA_ID_PICTURE && resultCode == RESULT_OK) { // if we respond to the id card photo request
            checkInformation(imageUri); // we extract information and the face from the id card picture
        } else if (requestCode == ID_CAMERA_ACTIVITY_FACE_PICTURE && resultCode == RESULT_OK) { // if we respond to the face photo request
            try {
                viewModel.setRealFacePicture(MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri)); // get the picture from the URI where we saved it
                getContentResolver().delete(imageUri, null, null); // delete the image after we've created the Bitmap because it is not useful anymore
                compareFaces(); // we compare both faces
            } catch (IOException e) {
                e.printStackTrace();
                tryAgain("Une erreur est survenue lors de la prise de la photo.");
            }
        } else if (requestCode == ID_CONFIRM_ACTIVITY_FACE_PICTURE && resultCode == RESULT_OK) { // if we respond to the activity that asks to take a face photo
            takePhoto(ID_CAMERA_ACTIVITY_FACE_PICTURE); // open the camera to take a face photo
        } else { // otherwise it means that we return from taking a photo so we return to the first screen for a new scan
            tryAgain("Annulation.");
        }
    }

    /**
     * Method to extract face picture and text information from the ID card picture
     * and verify if the ID card number already exists.
     * @param imageUri ID card picture URI
     */
    @SuppressLint("CheckResult")
    public void checkInformation(Uri imageUri) {
        viewModel.getIDCardViewState().observe(this, idCardViewState -> { // subscribe to the livedata containing the result of the get request
            if (idCardViewState.getErrorCode() == DisplayedModel.NOT_FOUND) { // if the user was not found in the database, we display a message and ask to take a second picture to verify the validity of the ID card
                startActivityForResult(new Intent(VerifyIDActivity.this, FacePictureActivity.class), ID_CONFIRM_ACTIVITY_FACE_PICTURE); // start the activity to inform the user and ask him to take a second picture
            } else if (idCardViewState.getErrorCode() == DisplayedModel.NO_ERROR) { // if there was no error, we display that the access is granted
                showResult(true);
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
            tryAgain("Une erreur est survenue lors de la prise de la photo.");
            return;
        }

        /* Text extraction */
        viewModel.getTextLiveData() // subscribe to treat the response
                .observe(this,
                        card -> {
                            if (card.getErrorCode() == DisplayedModel.NO_ERROR) {
                                viewModel.verifyIDCard(); // verify if this card number exists in the database, the livedata will be notified with the result
                            } else {
                                tryAgain(card.getErrorMessage());
                            }
                        });
        viewModel.extractText(picture); // trigger the extraction process

        /* Face detection */
        viewModel.getFaceLiveData() // subscribe to treat the response
                .observe(this,
                        faceFound -> {
                            if (faceFound.getErrorCode() == DisplayedModel.NO_ERROR) {
                                viewModel.setIdCardFacePicture(faceFound.getSuccessObject()); // set the retrieved face picture
                            } else {
                                tryAgain(faceFound.getErrorMessage());
                            }
                        });
        viewModel.extractFace(picture); // trigger the extraction process
    }

    /**
     * Method to compare both face pictures to indicate if the visitor is the actual owner of the given ID card.
     */
    public void compareFaces() {
        viewModel.getComparisonLiveData() // subscribe to treat the response
                .observe(this,
                face -> {
                    if (face.getErrorCode() == DisplayedModel.NO_ERROR) { // if there is no error
                        if (face.getSuccessObject()) { // if the two faces match
                            viewModel.insert(); // insert the new visitor's information
                            showResult(true);
                        } else {
                            showResult(false);
                        }
                    } else {
                        tryAgain(face.getErrorMessage());
                    }
                });
        viewModel.compareFaces(); // trigger the comparison process
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
                MY_PERMISSION_REQUEST_CAMERA_ID_PICTURE
        );
    }

    @SuppressLint("NewApi")
    private void showResult(boolean isGranted){
        loadingLogo.setVisibility(View.INVISIBLE); // hide the loading icon
        loadingText.setVisibility(View.INVISIBLE); // hide the loading text
        if(isGranted){
            resultLogo.setImageDrawable(getDrawable(R.drawable.granted));
            resultMessage.setText("Accès autorisé."); // display a message
        }else{
            resultLogo.setImageDrawable(getDrawable(R.drawable.denied));
            resultMessage.setText("Accès refusé."); // display a message
        }
    }

    /**
     * Method to print a message in a Toast.
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
     * Returns to the start activity and displays an error message in a Toast.
     *
     * @param message to display
     */
    private void tryAgain(String message) {
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
