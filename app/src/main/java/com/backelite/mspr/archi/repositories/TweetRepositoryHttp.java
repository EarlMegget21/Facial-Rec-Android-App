package com.backelite.mspr.archi.repositories;

public class TweetRepositoryHttp extends TweetRepository {

//    private TwitterService service;
//
//    @Inject
//    public TweetRepositoryHttp(TwitterService twitterService) {
//        super();
//        service = twitterService;
//    }
//
//    @Override
//    public void fetchTweets() {
//        service.listRepos() //on lance la requête
//                .subscribeOn(Schedulers.io()) //obligé sinon nous rejette avec Exception
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(
//                        /**
//                         * Si succès alors on passe la liste des tweets au fragment en la
//                         * transformant en ArrayList car Parcelable
//                         * @param call requête
//                         * @param response réponse
//                         */
//                        value -> {
////                            DisplayedModel<List<Tweet>> valueReturned = new DisplayedModel<>(value, null);
////                            getMediatorLiveData().setValue(valueReturned); //on met à jour le LiveData
//                        },
//                        error -> {
//                            // error case
////                            DisplayedModel<List<Tweet>> errorReturned = new DisplayedModel<>(null, error.getMessage());
////                            getMediatorLiveData().setValue(errorReturned);
//                        });
//    }

//    @Override
//    public void insert (Tweet tweet) {
//        fait rien ici
//    }
}
