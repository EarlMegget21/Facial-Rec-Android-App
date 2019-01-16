package com.epsi.mspr.services;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.util.SparseArray;

import com.epsi.mspr.models.DisplayedModel;
import com.epsi.mspr.models.IDCard;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.epsi.mspr.application.MainApplication.app;

@Singleton
public class ExtractionService {

    /**
     * To detect the face on the ID card picture
     */
    private FaceDetector faceDetector;

    /**
     * To detect information on the ID card picture
     */
    private TextRecognizer textRecognizer;

    @Inject
    public ExtractionService() {
        this.faceDetector = new FaceDetector.Builder(app())
                .setTrackingEnabled(false) //more accurate for simple images, for a sequence of images (video) better to set it true
                .build();
        this.textRecognizer = new TextRecognizer.Builder(app()).build(); // get the text recognizer

        /*if (textRecognizer != null) { // peut être faire ça à la destruction du service (peut être pas besoin car c'est un Singleton donc il se detruit à l'arrêt de l'app) sinon construire les detector dans les viewmodel qui en ont besoin
            textRecognizer.release(); //to release used ressources
        }
        if (faceDetector != null) {
            faceDetector.release(); //to release used ressources
        }*/
    }

    /**
     * Method to extract needed ID card info (first name, last name, card number) from the id card picture asynchronously
     * @param frame representing the id crd picture
     * @return livedata containing an object containing an error if the card number was unreadable or if the detection failed or the ID card info otherwise
     */
    @SuppressLint("CheckResult")
    public LiveData<DisplayedModel<IDCard>> extractText(Frame frame){
        MutableLiveData<DisplayedModel<IDCard>> liveData = new MutableLiveData<>();

        Single.create(emitter -> {
            if (!textRecognizer.isOperational()) { //if the recognizer is not operational
                emitter.onError(new Throwable());
            } else {
                SparseArray<TextBlock> blocks = textRecognizer.detect(frame); // get every text detected (long method)

                int firstCharIndexToRemove; // used in the loop to store the first index of the string to remove
                String stringToRemove, // used in the loop to store the string to remove
                        foundValue, // used in the loop to store each line of text gotten from the recognizer
                        stringToKeep; // used in the loop to store each parsed string to keep
                Matcher regexMatcher; // used to get only the numerical values from a string thanks to regex
                String foundNumber = ""; // string
                IDCard card = new IDCard(null, null, 0);

                for (int i = 0; i < blocks.size(); i++) { // for each line of text, parse the line to extract useful values
                    foundValue = blocks.valueAt(i).getValue();
                    // remove unwanted chars
                    foundValue = foundValue.replace("(s", ""); // have to parse the 's' with another char for "Prenom(s):" because sometimes, special chars might not be recognized so we assure that we don't remove a 's' belonging to the first name
                    foundValue = foundValue.replace("s)", "");
                    foundValue = foundValue.replace("s:", "");
                    foundValue = foundValue.replace("(", "");
                    foundValue = foundValue.replace(")", "");
                    foundValue = foundValue.replace("'", "");
                    foundValue = foundValue.replace(",", " ");
                    foundValue = foundValue.replace(".", " ");

                    if (foundValue.contains("Nom")) { //if the line contains a last name
                        firstCharIndexToRemove = foundValue.lastIndexOf(":");
                        stringToKeep = foundValue.substring(firstCharIndexToRemove > 0 ? firstCharIndexToRemove + 1 : 0); // try to remove every chars before the ':' included (that's why +1)

                        firstCharIndexToRemove = stringToKeep.lastIndexOf(";");
                        stringToKeep = stringToKeep.substring(firstCharIndexToRemove > 0 ? firstCharIndexToRemove + 1 : 0); // try to remove every chars before the ';' included (that's why +1)

                        stringToRemove = "Nom";
                        firstCharIndexToRemove = stringToKeep.lastIndexOf(stringToRemove);
                        stringToKeep = stringToKeep.substring(firstCharIndexToRemove > 0 ? firstCharIndexToRemove + stringToRemove.length() : 0); // try to remove every chars before the stringToRemove included (that's why + .length) if both previous methods failed because no ':'/';' were found

                        stringToKeep = stringToKeep.toLowerCase(); // convert the rest to lower case
                        stringToKeep = stringToKeep.trim(); // remove every space char around

                        stringToKeep = stringToKeep.split(" ")[0]; // only get the first word found if spaces (to hide recognition problems)

                        card.setLastName(stringToKeep); //set the last name
                    }

                    foundValue = foundValue.toLowerCase(); // convert the rest to lower case to reduce parse problems, if we do this before, we could be confused with "nom" and "prenom" so it is the uppercased "N" that makes the difference
                    foundValue = foundValue.replace("é", "e"); // remove accents to reduce parse problems

                    if (foundValue.contains("prenom")) { //if the line contains a first name
                        firstCharIndexToRemove = foundValue.lastIndexOf(":");
                        stringToKeep = foundValue.substring(firstCharIndexToRemove > 0 ? firstCharIndexToRemove + 1 : 0); // try to remove every chars before the ':' included (that's why +1)

                        firstCharIndexToRemove = stringToKeep.lastIndexOf(";");
                        stringToKeep = stringToKeep.substring(firstCharIndexToRemove > 0 ? firstCharIndexToRemove + 1 : 0); // try to remove every chars before the ';' included (that's why +1)

                        stringToRemove = "prenom";
                        firstCharIndexToRemove = stringToKeep.lastIndexOf(stringToRemove);
                        stringToKeep = stringToKeep.substring(firstCharIndexToRemove > 0 ? firstCharIndexToRemove + stringToRemove.length() : 0); // try to remove every chars before the stringToRemove included (that's why + .length) if both previous methods failed because no ':'/';' were found

                        stringToKeep = stringToKeep.toLowerCase(); // convert the rest to lower case
                        stringToKeep = stringToKeep.trim(); // convert the rest to lower case

                        stringToKeep = stringToKeep.split(" ")[0]; // only get the first word found if spaces (to hide recognition problems)

                        card.setFirstName(stringToKeep); //set the first name
                    }

                    regexMatcher = Pattern.compile("\\d+").matcher(foundValue); // set the regex matcher to get only the numerical resultingText
                    foundNumber = card.getIDNumber() == 0 && regexMatcher.find() ? regexMatcher.group() : foundNumber; // if the card number hasn't been retrieved yet and if there is a numerical value in the text, we extract this number as a string

                    if (foundNumber.length() == 12) { //if the numerical value found contains 12 characters (size of a id card number)
                        card.setIDNumber(Long.parseLong(foundNumber)); // set the card number
                    }

                    if (card.getFirstName() != null && card.getLastName() != null && card.getIDNumber() > 0) { // if we've set every wanted fields, it's useless to continue looping
                        break;
                    }
                }

                if (card.getIDNumber() == 0) { // if we have not set the card number, it means that the detection was not accurate enough, ask to try again
                    emitter.onError(new Throwable());
                    return; // stop here
                }

                //ping success when I have a value
                emitter.onSuccess(card);
            }
        })
                .subscribeOn(Schedulers.io()) // to execute on a worker thread
                .observeOn(AndroidSchedulers.mainThread()) // to execute the response on the main thread and manipulate the view
                .subscribe(
                        card -> {
                            liveData.setValue(new DisplayedModel<>((IDCard) card, null, DisplayedModel.NO_ERROR)); // notify the livedata returned with the face
                        },
                        error ->{
                            // notify the livedata returned with an error
                            liveData.setValue(new DisplayedModel<>(null, "Une erreur est survenue lors de la récupération du visage sur la carte d'identité. Veuillez réessayer avec une photo plus nette.", DisplayedModel.OTHER));
                        });

        return liveData; //return a livedata to listen
    }

    /**
     * Method to extract face picture from the id card picture asynchronously
     * @param frame representing the id crd picture
     * @param bitmap original bitmap used to create a new one with the face
     * @return livedata containing an object containing an error if the face detection failed or the new picture with only the face otherwise
     */
    @SuppressLint("CheckResult")
    public LiveData<DisplayedModel<Bitmap>> extractFace (Frame frame, Bitmap bitmap){
        MutableLiveData<DisplayedModel<Bitmap>> liveData = new MutableLiveData<>();

        Single.create(emitter -> {
            if (!faceDetector.isOperational()) { //if the detector is not operational
                emitter.onError(new Throwable());
            } else {
                SparseArray<Face> faces = faceDetector.detect(frame); // detect every faces on the picture (long method)

                if (faces.size() > 0) { //if there are faces detected
                    Face face = faces.valueAt(0); // get the first found face
                    Bitmap faceBitmap = Bitmap.createBitmap(bitmap, Math.round(face.getPosition().x), Math.round(face.getPosition().y), Math.round(face.getWidth()), Math.round(face.getHeight())); // create a bitmap containing the face

                    //ping success when I have a value
                    emitter.onSuccess(faceBitmap);
                } else {
                    emitter.onError(new Throwable());
                }
            }
        })
                .subscribeOn(Schedulers.io()) // to execute on a worker thread
                .observeOn(AndroidSchedulers.mainThread()) // to execute the response on the main thread and manipulate the view
                .subscribe(
                        faceFound -> {
                            liveData.setValue(new DisplayedModel<>((Bitmap) faceFound, null, DisplayedModel.NO_ERROR)); // notify the livedata returned with the face
                        },
                        error->{
                            // notify the livedata returned with an error
                            liveData.setValue(new DisplayedModel<>(null, "Une erreur est survenue lors de la récupération des informations textuelles sur la carte d'identité. Veuillez réessayer avec une photo plus nette.", DisplayedModel.OTHER));
                        });

        return liveData; //return a livedata to listen
    }
}
