package com.epsi.mspr.archi.view_models;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Bitmap;

import com.epsi.mspr.application.MainApplication;
import com.epsi.mspr.archi.repositories.IDRepository;
import com.epsi.mspr.models.DisplayedModel;
import com.epsi.mspr.models.IDCard;
import com.epsi.mspr.services.CompareService;
import com.epsi.mspr.services.ExtractionService;

import java.util.concurrent.Semaphore;

import javax.inject.Inject;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class VerifyIDViewModel extends AndroidViewModel {

    /**
     * Repository to save and retrieve card info about previous visitors
     */
    @Inject
    IDRepository repository;

    /**
     * Service used to extract text data and face picture from the ID card picture
     */
    @Inject
    ExtractionService service;

    /**
     * Service used to compare face pictures
     */
    @Inject
    CompareService compareService;

    /**
     * To wait until both face pictures are setted before trying to compare them.
     */
    private Semaphore comparisonSyncronization = new Semaphore(0);

    /**
     * Face picture extracted from the id Card.
     */
    private Bitmap idCardFacePicture;

    /**
     * Face picture taken in reality.
     */
    private Bitmap realFacePicture;


    /**
     * The ID card information to save.
     */
    private IDCard idCardToSave;

    /**
     * LiveData to listen to treat the response if the id card exists.
     */
    private MediatorLiveData<DisplayedModel<IDCard>> idCard = new MediatorLiveData<>();

    /**
     * LiveData to listen to treat the response of the picture text extraction.
     */
    private MediatorLiveData<DisplayedModel<IDCard>> textLiveData = new MediatorLiveData<>();

    /**
     * LiveData to listen to treat the response of the picture face extraction.
     */
    private MediatorLiveData<DisplayedModel<Bitmap>> faceLiveData = new MediatorLiveData<>();

    /**
     * LiveData to listen to treat the response of the faces comparison.
     */
    private MediatorLiveData<DisplayedModel<Boolean>> comparisonLiveData = new MediatorLiveData<>();

    /**
     * Object that refers every Single (to perform async task) and able to stop them.
     */
    private CompositeDisposable disposables = new CompositeDisposable();

    public VerifyIDViewModel(Application application) {
        super(application);
        MainApplication.getApplication().getAppComponent().inject(this); // to inject the repository

        LiveData<DisplayedModel<IDCard>> idCardLiveData = repository.getIDCard();
        this.idCard.addSource(idCardLiveData, idcard -> { // observe the repository livedata containing ID card info
            if (idcard != null) { // if we receive a null value, we don't treat it, otherwise, we unsubscribe and we notify the view
                this.idCard.removeSource(idCardLiveData);
                this.idCard.setValue(idcard);
            }
        });
    }

    /**
     * To let the view associated to subscribe to the livedata with read only access.
     * @return livedata containing an object containing an error or the ID card retrieved from the repository
     */
    public LiveData<DisplayedModel<IDCard>> getIDCardViewState() {
        return idCard;
    }

    /**
     * To let the view associated to subscribe to the livedata with read only access.
     * @return livedata containing an object with an error or the ID card info
     */
    public LiveData<DisplayedModel<IDCard>> getTextLiveData() {
        return textLiveData;
    }

    /**
     * To let the view associated to subscribe to the livedata with read only access.
     * @return livedata containing an object with an error or the face picture
     */
    public LiveData<DisplayedModel<Bitmap>> getFaceLiveData() {
        return faceLiveData;
    }

    /**
     * To let the view associated to subscribe to the livedata with read only access.
     * @return livedata containing an object containing an error if the face comparison failed or a boolean indicating the result otherwise
     */
    public LiveData<DisplayedModel<Boolean>> getComparisonLiveData() {
        return comparisonLiveData;
    }

    /**
     * Method to verify if the actual ID card number exists thanks to the repository.
     */
    public void verifyIDCard() {
        repository.fetchIDCard(idCardToSave.getIDNumber());
    }

    /**
     * Setter
     */
    public void setIdCardToSave(IDCard idCardToSave) {
        this.idCardToSave = idCardToSave;
    }

    /**
     * Method to add a new ID card thanks to the repository.
     */
    public void insert() {
        repository.insert(new IDCard(idCardToSave.getLastName(), idCardToSave.getFirstName(), idCardToSave.getIDNumber()));
    }

    /**
     * Method that extract asynchronously text from the id card picture.
     */
    public void extractText(Bitmap bitmap) {
        service.createFrame(bitmap);
        disposables.add(service.extractText()
                .subscribe(
                        idCard -> {
                            idCardToSave = idCard; // will be saved if the user doesn't exists yet (next method ping an error in the livedata)
                            textLiveData.setValue(new DisplayedModel<>(idCard, null, DisplayedModel.NO_ERROR)); // notify the livedata returned with the face
                        },
                        error -> {
                            textLiveData.setValue(new DisplayedModel<>(null, "La récupération du text a echoué.", DisplayedModel.OTHER));
                        }
                ));
    }

    /**
     * Method that extract asynchronously face picture from the id card picture.
     * @param bitmap picture as a bitmap to create a new one around the face
     */
    @SuppressLint("CheckResult")
    public void extractFace(Bitmap bitmap) {
        disposables.add(service.extractFace(bitmap)
                .subscribe(
                        face -> {
                            faceLiveData.setValue(new DisplayedModel<>(face, null, DisplayedModel.NO_ERROR)); // notify the livedata returned with the face
                        },
                        error -> {
                            faceLiveData.setValue(new DisplayedModel<>(null, "La récupération du visage a echoué.", DisplayedModel.OTHER));
                        }
                ));
    }

    /**
     * To set the face extracted from the card picture.
     * @param idCardFace face picture
     */
    public void setIdCardFacePicture(Bitmap idCardFace) {
        comparisonSyncronization.release(); // to let the faces comparison begin.
        idCardFacePicture = idCardFace;
    }

    /**
     * To set the face snapped.
     * @param realFace face picture
     */
    public void setRealFacePicture(Bitmap realFace) {
        comparisonSyncronization.release(); // to let the faces comparison begin.
        realFacePicture = realFace;
    }

    /**
     * Method to compare face pictures asynchronously.
     */
    public void compareFaces() {
        disposables.add(
                Single.create((SingleOnSubscribe<Boolean>) emitter -> {
                    comparisonSyncronization.acquire(2); // wait until all permits are available (both picture are set)
                    emitter.onSuccess(Boolean.TRUE);
                })
                        .subscribeOn(Schedulers.io()) // to wait all permits on a different thread and do not block the view during this moment
                        .flatMap(bool -> compareService.compareFaces(idCardFacePicture, realFacePicture).firstOrError()) // trigger another asynchronous task
                        .subscribe(
                                match -> {
                                    comparisonLiveData.setValue(new DisplayedModel<>(match, null, DisplayedModel.NO_ERROR)); //on met à jour le LiveData
                                },
                                error -> {
                                    // error case
                                    comparisonLiveData.setValue(new DisplayedModel<>(null, "La comparaison des visages a echoué.", DisplayedModel.OTHER)); //on met à jour le LiveData
                                })
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
