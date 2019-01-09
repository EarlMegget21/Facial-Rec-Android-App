package com.backelite.mspr.archi.view_models;

import android.app.Application;

import com.backelite.mspr.archi.repositories.TweetRepository;

import javax.inject.Inject;

import androidx.lifecycle.AndroidViewModel;

import static com.backelite.mspr.application.MainApplication.app;

public class TweetListViewModel extends AndroidViewModel {

    @Inject
    TweetRepository repository;

//    private MediatorLiveData<TweetListViewState> tweetsList = new MediatorLiveData<>();

    public TweetListViewModel(Application application) {
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
