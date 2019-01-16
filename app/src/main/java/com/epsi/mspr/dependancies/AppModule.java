package com.epsi.mspr.dependancies;

import com.epsi.mspr.services.FirebaseAPIService;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module //attaché au cycle de vie de l'application
public class AppModule { //ici on déclare toutes nos classes en singleton

    //permet de pouvoir injecter le Retrofit Builder dans plusieurs HttpService sans avoir à le reconstruire pour chaque classe
    @Provides
    @Singleton
    public FirebaseAPIService provideTwitterService() { //on peut pas le virer d'ici car on n'a pas accès au constructeur de Gson pour lui mettre @Inject (on ne peut pas l'exporter dans un module apart)

        //on le sauvegarde pas car sera utilisé uniquement pour créer le Retrofit permettant de créer le FirebaseAPIService qu'on sauvegarde
        OkHttpClient okHttpClient = new OkHttpClient.Builder().addNetworkInterceptor(new StethoInterceptor()).build();

        //on le sauvegarde pas car sera utilisé uniquement pour créer le FirebaseAPIService qu'on sauvegarde
        return new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create()) //pour parser le JSON;
                .baseUrl("https://mspr-3d59a.firebaseio.com/") //domaine de l'API
                .client(okHttpClient)
                .build()
                .create(FirebaseAPIService.class); //on créer le service pour l'URL des tweets
    }
}
