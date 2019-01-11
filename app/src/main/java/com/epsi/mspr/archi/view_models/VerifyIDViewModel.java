package com.epsi.mspr.archi.view_models;

import android.app.Application;

import com.epsi.mspr.archi.repositories.IDRepository;

import javax.inject.Inject;

import androidx.lifecycle.AndroidViewModel;

public class VerifyIDViewModel extends AndroidViewModel {

    @Inject
    IDRepository repository;

//    private MediatorLiveData<TweetListViewState> tweetsList = new MediatorLiveData<>();

    public VerifyIDViewModel(Application application) {
        super(application);
//        app().getAppComponent().inject(this);
//        tweetsList.addSource(repository.getTweetsList(), list -> {
//            if (list.getErrorMessage() != null) {
//                tweetsList.setValue(new TweetListViewState(list.getErrorMessage()));
//            }else{
//                tweetsList.setValue(new TweetListViewState(list.getSuccessObject()));
//            }
//        });
//        repository.fetchTweets();
    }

//    public LiveData<TweetListViewState> getTweetListViewState() {
//        return tweetsList;
//    }

}
