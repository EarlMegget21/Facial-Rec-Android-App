package com.epsi.mspr.services;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.SparseArray;

import com.epsi.mspr.models.IDCard;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.epsi.mspr.application.MainApplication.getApplication;

@Singleton
public class ExtractionService {

    /**
     * To detect the face on the ID card picture
     */
//    private FaceDetector faceDetector;

    /**
     * To detect information on the ID card picture
     */
    private TextRecognizer textRecognizer;

    /**
     * Representing the picture and useable by the API
     */
    private Frame frame;

    @Inject
    public ExtractionService() {
        /*this.faceDetector = new FaceDetector.Builder(getApplication())
                .setTrackingEnabled(false) //more accurate for simple images, for a sequence of images (video) better to set it true
                .build();*/
        this.textRecognizer = new TextRecognizer.Builder(getApplication()).build(); // get the text recognizer

        /*if (textRecognizer != null) { // peut être faire ça à la destruction du service (peut être pas besoin car c'est un Singleton donc il se detruit à l'arrêt de l'getApplication) sinon construire les detector dans les viewmodel qui en ont besoin
            textRecognizer.release(); //to release used ressources
        }
        if (faceDetector != null) {
            faceDetector.release(); //to release used ressources
        }*/
    }

    /**
     * To initialize the Frame object.
     * @param bitmap picture to extract
     */
    public void createFrame(Bitmap bitmap) {
        frame = new Frame.Builder().setBitmap(bitmap).build(); // create a Frame to be used by both detectors
    }

    /**
     * Method to extract needed ID card info (first name, last name, card number) from the id card picture asynchronously.
     * @return livedata containing an object containing an error if the card number was unreadable or if the detection failed or the ID card info otherwise
     */
    @SuppressLint("CheckResult")
    public Single<IDCard> extractText() {
        return Single.create((SingleOnSubscribe<IDCard>) emitter -> {
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
                .observeOn(AndroidSchedulers.mainThread()); // to execute the response on the main thread and manipulate the view
    }

    /**
     * Method to extract face picture from the id card picture asynchronously (Single version).
     * @param bitmap original bitmap used to create a new one with the face
     * @return livedata containing an object containing an error if the face detection failed or the new picture with only the face otherwise
     */
    @SuppressLint("CheckResult")
    public Single<Bitmap> extractFace(Bitmap bitmap) {
        return Single.create((SingleOnSubscribe<Bitmap>) emitter -> {
            int nbFaces = 1,
                    count = 0;
            Bitmap configuredBitmap = bitmap.copy(Bitmap.Config.RGB_565, true); // the bitmap has to be in 565 to beh analysable
            android.media.FaceDetector faceDetector1 = new android.media.FaceDetector(configuredBitmap.getWidth(), configuredBitmap.getHeight(), nbFaces);
            android.media.FaceDetector.Face[] faces = new android.media.FaceDetector.Face[nbFaces];
            PointF eyescenter = new PointF();

            try {
                count = faceDetector1.findFaces(configuredBitmap, faces);
            } catch (Exception e) {
                emitter.onError(new Throwable());
            }

            if (count > 0) { // check if we detect any faces
                faces[0].getMidPoint(eyescenter);
                float eyesdist = faces[0].eyesDistance();
                int width = (int) eyesdist * 3,
                        height = (int) (eyesdist * 4.5),
                        upLeftX = (int) (eyescenter.x - (width / 2)),
                        upLeftY = (int) (eyescenter.y - (height / 2));

                if(upLeftY<0){
                    upLeftY=0;
                }
                if(upLeftX<0){
                    upLeftX=0;
                }

                int faceSize = upLeftY + height;
                if(faceSize >bitmap.getHeight()){
                    int diff = faceSize -bitmap.getHeight();
                    height-=diff;
                }

                Bitmap faceBitmap = Bitmap.createBitmap(bitmap, upLeftX, upLeftY, width, height); // create a bitmap containing the face

                emitter.onSuccess(faceBitmap); // ping success when I have a value
            } else {
                emitter.onError(new Throwable());
            }
        })
                .subscribeOn(Schedulers.io()) // to execute on a worker thread
                .observeOn(AndroidSchedulers.mainThread()); // to execute the response on the main thread and manipulate the view
    }

    /**
     * Method to extract face picture from the id card picture asynchronously
     *
     * @param bitmap original bitmap used to create a new one with the face
     * @return livedata containing an object containing an error if the face detection failed or the new picture with only the face otherwise
     * @deprecated Very long...
     */
    /* @SuppressLint("CheckResult")
    @Deprecated
    public Single<Bitmap> extractFaceVisionAPI(Bitmap bitmap) {
//        MutableLiveData<DisplayedModel<Bitmap>> liveData = new MutableLiveData<>();

        return Single.create((SingleOnSubscribe<Bitmap>) emitter -> {
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
                .observeOn(AndroidSchedulers.mainThread()); // to execute the response on the main thread and manipulate the view


//        return liveData; //return a livedata to listen
    }*/
}
