package com.epsi.mspr.activities;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.TextView;

import com.epsi.mspr.R;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

public class VerifyIDActivity extends AppCompatActivity {

    @BindView(R.id.imageView)
    ImageView mImageView;
    @BindView(R.id.infos)
    TextView infos;

    public static final String PHOTO = "photo";

    /**
     * To detect the face on the ID card picture
     */
    private FaceDetector faceDetector;

    /**
     * To detect information on the ID card picture
     */
    private TextRecognizer textRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_id);

        ButterKnife.bind(this);

        Uri imageUri = getIntent().getParcelableExtra(PHOTO); // get the picture URI where we saved the picture in the previous activity

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri); // get the picture from the URI where we saved it
        } catch (IOException e) {
            e.printStackTrace();
        }
        Frame frame = new Frame.Builder().setBitmap(bitmap).build(); // create a Frame to be used by both detectors

        /* Récupération du text */
        textRecognizer = new TextRecognizer.Builder(VerifyIDActivity.this).build(); // get the text recognizer

        if (!textRecognizer.isOperational()) { //if the recognizer is not operational
            TakePictureActivity.print(VerifyIDActivity.this, "Error text recognition"); // display an error message in a Toast
        } else {
            SparseArray<TextBlock> blocks = textRecognizer.detect(frame); // get every text detected TODO: methode bloquante, l'executer dans un thread et afficher un loading

            StringBuilder resultingText = new StringBuilder(); // resulting text

            int firstCharIndexToRemove; // used in the loop to store the first index of the string to remove
            String stringToRemove, // used in the loop to store the string to remove
                    foundValue, // used in the loop to store each line of text gotten from the recognizer
                    stringToKeep; // used in the loop to store each parsed string to keep
            Matcher regexMatcher; // used to get only the numerical values from a string thanks to regex

            for (int i = 0; i < blocks.size(); i++) { // for each line of text, parse the line to extract useful values
                foundValue = blocks.valueAt(i).getValue();
                // remove unwanted chars
                foundValue = foundValue.replace("(s", ""); // have to parse the 's' with another char for "Prenom(s):" because sometimes, special chars might not be recognized so we assure that we don't remove a 's' belonging to the first name
                foundValue = foundValue.replace("s)", "");
                foundValue = foundValue.replace("s:", "");
                foundValue = foundValue.replace("(", "");
                foundValue = foundValue.replace(")", "");
                foundValue = foundValue.replace("'", "");
                foundValue = foundValue.replace("é", "e");
                foundValue = foundValue.replace(",", " ");
                foundValue = foundValue.replace(".", " ");

                if (foundValue.contains("Nom")) { //if the line contains a last name
                    firstCharIndexToRemove = foundValue.lastIndexOf(":");
                    stringToKeep = foundValue.substring(firstCharIndexToRemove>0? firstCharIndexToRemove+1 : 0); // try to remove every chars before the ':' included (that's why +1)

                    firstCharIndexToRemove = stringToKeep.lastIndexOf(";");
                    stringToKeep = stringToKeep.substring(firstCharIndexToRemove>0? firstCharIndexToRemove+1 : 0); // try to remove every chars before the ';' included (that's why +1)

                    stringToRemove = "Nom";
                    firstCharIndexToRemove = stringToKeep.lastIndexOf(stringToRemove);
                    stringToKeep = stringToKeep.substring(firstCharIndexToRemove>0?firstCharIndexToRemove + stringToRemove.length() : 0); // try to remove every chars before the stringToRemove included (that's why + .length) if both previous methods failed because no ':'/';' were found

                    stringToKeep = stringToKeep.toLowerCase(); // convert the rest to lower case
                    stringToKeep = stringToKeep.trim(); // remove every space char around

                    stringToKeep = stringToKeep.split(" ")[0]; // only get the first word found if spaces (to hide recognition problems)

                    resultingText.append(stringToKeep).append("||");
                }

                if (foundValue.contains("Prenom")) { //if the line contains a first name
                    firstCharIndexToRemove = foundValue.lastIndexOf(":");
                    stringToKeep = foundValue.substring(firstCharIndexToRemove>0? firstCharIndexToRemove+1 : 0); // try to remove every chars before the ':' included (that's why +1)

                    firstCharIndexToRemove = stringToKeep.lastIndexOf(";");
                    stringToKeep = stringToKeep.substring(firstCharIndexToRemove>0? firstCharIndexToRemove+1 : 0); // try to remove every chars before the ';' included (that's why +1)

                    stringToRemove = "Prenom";
                    firstCharIndexToRemove = stringToKeep.lastIndexOf(stringToRemove);
                    stringToKeep = stringToKeep.substring(firstCharIndexToRemove>0? firstCharIndexToRemove + stringToRemove.length() : 0); // try to remove every chars before the stringToRemove included (that's why + .length) if both previous methods failed because no ':'/';' were found

                    stringToKeep = stringToKeep.toLowerCase(); // convert the rest to lower case
                    stringToKeep = stringToKeep.trim(); // convert the rest to lower case

                    stringToKeep = stringToKeep.split(" ")[0]; // only get the first word found if spaces (to hide recognition problems)

                    resultingText.append(stringToKeep).append("||");
                }

                foundValue = foundValue.toLowerCase(); // convert the rest to lower case
                foundValue = foundValue.replace(" ", ""); // remove every space remaining

                if (foundValue.contains("cartenationaledidentiten")) { //if the line contains a ID card number
                    firstCharIndexToRemove = foundValue.lastIndexOf(":");
                    stringToKeep = foundValue.substring(firstCharIndexToRemove>0? firstCharIndexToRemove : 0); // try to remove every chars before the ':' included (that's why +1)

                    firstCharIndexToRemove = stringToKeep.lastIndexOf(";");
                    stringToKeep = stringToKeep.substring(firstCharIndexToRemove>0? firstCharIndexToRemove : 0); // try to remove every chars before the ';' included (that's why +1)

                    stringToRemove = "cartenationaledidentiten";
                    firstCharIndexToRemove = stringToKeep.lastIndexOf(stringToRemove);
                    stringToKeep = stringToKeep.substring(firstCharIndexToRemove>0? firstCharIndexToRemove + stringToRemove.length() : 0); // try to remove every chars before the stringToRemove included (that's why + .length) if both previous methods failed because no ':'/';' were found

                    regexMatcher = Pattern.compile("\\d+").matcher(stringToKeep); // get only the numerical resultingText
                    if(regexMatcher.find()) { // if there is a numerical value in the string
                        resultingText.append(regexMatcher.group()).append("||");
                    }
                }
            }
            infos.setText(resultingText.toString());
        }

        /* Récupération du visage */
        faceDetector = new FaceDetector.Builder(this)
                .setTrackingEnabled(false) //plus précis pour des images simples, pour une serie d'images consécutives (video) vaut mieux mettre true
                .build();

        if (!faceDetector.isOperational()) { //if the detector is not operational
            TakePictureActivity.print(VerifyIDActivity.this, "Error face recognition"); //display an error in a toast
        } else {
            SparseArray<Face> faces = faceDetector.detect(frame); // detect every faces on the picture
            if (faces.size() > 0) { //if there are faces detected
                Face face = faces.valueAt(0); // get the first found face
                bitmap = Bitmap.createBitmap(bitmap, Math.round(face.getPosition().x), Math.round(face.getPosition().y), Math.round(face.getWidth()), Math.round(face.getHeight())); // create a bitmap containing the face
            }
        }

        mImageView.setImageBitmap(bitmap);
        //TODO: lier un ViewModel relié au repo
        //TODO: mettre dans le repo l'appel à Firebase pour chercher si les informations textuelles existent et mettre une fonction pour enregistrer les infos textuelles
        //TODO: si les infos ne sont pas trouvées alors prendre une autre photo et comparer la nouvelle photo avec celle qu'on a extrait de la carte
        //TODO: si les images matchent, enregistrer le text sur Firebase sinon message d'erreur et revenir à la première activité
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        textRecognizer.release(); //to release used ressources
        faceDetector.release(); //to release used ressources
    }
}
