package com.epsi.mspr.archi.repositories;

import com.epsi.mspr.models.DisplayedModel;
import com.epsi.mspr.models.IDCard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

/**
 * Class used to manipulate ID card data
 */
public abstract class IDRepository extends BaseRepository{

    /**
     * Livedata containing the ID card asked
     */
    private MediatorLiveData<DisplayedModel<IDCard>> idCard = new MediatorLiveData<>();

    /**
     * To let other class to observe the livedata with a read-only access
     * @return livedata containing an object with an error or the ID card
     */
    public LiveData<DisplayedModel<IDCard>> getIDCard() {
        return idCard; //on renvoie le LiveData pour qu'il soit observé, les observers seront notifiés après le setValue()
    }

    /**
     * To let repository implementations to observe the livedata with a write access
     * @return livedata containing an object with an error or the ID card
     */
    protected MediatorLiveData<DisplayedModel<IDCard>> getMediatorLiveData() {
        return idCard;
    }

    /**
     * Method to retrieve an ID card thanks to its number. The Live data will be notified.
     * @param cardNumber to search among existing ID cards
     */
    public abstract void fetchIDCard(long cardNumber);

    /**
     * Method to save an ID card for future search.
     * @param card information to save within an object
     */
    public abstract void insert (IDCard card);
}
