package com.epsi.mspr.archi.repositories;

import android.annotation.SuppressLint;
import android.util.Log;

import com.epsi.mspr.models.DisplayedModel;
import com.epsi.mspr.models.IDCard;
import com.epsi.mspr.services.FirebaseAPIService;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Repository requesting the Firebase REST API with Retrofit
 */
public class IDRepositoryFireBase extends IDRepository {

    /**
     * Service used to make http requests to the REST API thanks to Retrofit
     */
    private FirebaseAPIService firebaseApi;

    @Inject
    public IDRepositoryFireBase(FirebaseAPIService service) {
        super();
        firebaseApi = service;
    }

    @SuppressLint("CheckResult")
    @Override
    public void fetchIDCard(long cardNumber) {
        firebaseApi.getIDCard(cardNumber) //to execute the request in another thread (Exception otherwise)
                .subscribeOn(Schedulers.io()) //obligé sinon nous rejette avec Exception
                .observeOn(AndroidSchedulers.mainThread()) //to treat the response in the UI thread allowing us to touch the view
                .subscribe(
                        value -> {
                            getMediatorLiveData().setValue(new DisplayedModel<>(value, null, DisplayedModel.NO_ERROR)); //on met à jour le LiveData
                            getMediatorLiveData().setValue(null); // the observer have to unsubscribe after getting the value to do not be notified by the null
                        },
                        error -> {
                            // error case
                            if(error instanceof NullPointerException) {
                                getMediatorLiveData().setValue(new DisplayedModel<>(null, "Utilisateur non trouvé", DisplayedModel.NOT_FOUND)); //on met à jour le LiveData
                                getMediatorLiveData().setValue(null); // the observer have to unsubscribe after getting the value to do not be notified by the null
                            }else{
                                getMediatorLiveData().setValue(new DisplayedModel<>(null, "Utilisateur non trouvé", DisplayedModel.OTHER)); //on met à jour le LiveData
                            }
                            });
    }

    @SuppressLint("CheckResult")
    @Override
    public void insert(IDCard card) {
        firebaseApi.addIDCard(card, card.getIDNumber())
                .subscribeOn(Schedulers.io()) //to execute the request in another thread (Exception otherwise)
                .observeOn(AndroidSchedulers.mainThread()) //to treat the response in the UI thread allowing us to touch the view
                .subscribe(
                        value -> {
                            Log.i("teeest", "Success to write value " + value);
                        },
                        error -> {
                            // error case TODO: traiter le cas d'erreur
                            Log.i("teeest", "Failed to write value " + error);
                        });
    }
}
