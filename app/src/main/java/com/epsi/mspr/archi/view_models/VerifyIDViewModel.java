package com.epsi.mspr.archi.view_models;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import com.epsi.mspr.archi.repositories.IDRepository;
import com.epsi.mspr.models.DisplayedModel;
import com.epsi.mspr.models.IDCard;
import com.epsi.mspr.services.ExtractionService;
import com.google.android.gms.vision.Frame;

import javax.inject.Inject;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import static com.epsi.mspr.application.MainApplication.app;

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

    private MediatorLiveData<DisplayedModel<IDCard>> idCard = new MediatorLiveData<>();

    public VerifyIDViewModel(Application application) {
        super(application);
        app().getAppComponent().inject(this); // to inject the repository

        LiveData<DisplayedModel<IDCard>> idCardLiveData = repository.getIDCard();
        this.idCard.addSource(idCardLiveData, idcard -> { // observe the repository livedata containing ID card info
            if(idcard!=null) {
                this.idCard.removeSource(idCardLiveData);
                this.idCard.setValue(idcard);
            }
        });
    }

    /**
     * To let the view associated to subscribe to the livedata with read only access
     * @return livedata containing an object containing an error or the ID card retrieved from the repository
     */
    public LiveData<DisplayedModel<IDCard>> getIDCardViewState() {
        return idCard;
    }

    /**
     * Method to verify if the given ID card number exists thanks to the repository
     * @param cardNumber to check
     */
    public void verifyIDCard(long cardNumber){
        repository.fetchIDCard(cardNumber);
    }

    /**
     * Method to add a new ID card thanks to the repository
     * @param lastName of the id card owner
     * @param firstName of the id card owner
     * @param number of the id card
     */
    public void insert(String lastName, String firstName, long number){
        repository.insert(new IDCard(lastName, firstName, number));
    }

    /**
     * Method that extract asynchronously text from the id card picture
     * @param frame representing the picture and useable by the API TODO: construire la Frame dans le service vu qu'elle n'a de sens qu'avec cette API
     * @return livedata containing an object with an error or the ID card info
     */
    public LiveData<DisplayedModel<IDCard>> extractText(Frame frame){
        return service.extractText(frame);
    }

    /**
     * Method that extract asynchronously face picture from the id card picture
     * @param frame representing the picture and useable by the API TODO: construire la Frame dans le service vu qu'elle n'a de sens qu'avec cette API
     * @param bitmap picture as a bitmap to create a new one around the face TODO: contruire la nouvelle Bitmap dans la vue
     * @return livedata containing an object with an error or the face picture
     */
    public LiveData<DisplayedModel<Bitmap>> extractFace(Frame frame, Bitmap bitmap){
        return service.extractFace(frame, bitmap);
    }
}
