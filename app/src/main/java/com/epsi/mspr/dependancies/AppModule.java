package com.epsi.mspr.dependancies;

import com.epsi.mspr.services.FirebaseAPIService;

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
    public Retrofit.Builder provideRetrofitBuilder(){ //on peut pas le virer d'ici car on n'a pas accès au constructeur de Gson pour lui mettre @Inject (on ne peut pas l'exporter dans un module apart)
        return new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create()); //pour parser le JSON;
    }

    //permet de pouvoir injecter le Retrofit Builder dans plusieurs HttpService sans avoir à le reconstruire pour chaque classe
    @Provides
    @Singleton
    public FirebaseAPIService provideTwitterService(Retrofit.Builder builder){ //on peut pas le virer d'ici car on n'a pas accès au constructeur de Gson pour lui mettre @Inject (on ne peut pas l'exporter dans un module apart)

        //on le sauvegarde pas car sera utilisé uniquement pour créer le Retrofit permettant de créer le FirebaseAPIService qu'on sauvegarde
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        //on le sauvegarde pas car sera utilisé uniquement pour créer le FirebaseAPIService qu'on sauvegarde
        Retrofit retrofit = builder.baseUrl("https://api.twitter.com/") //domaine de l'API
                .client(okHttpClient)
                .build();

        return retrofit.create(FirebaseAPIService.class); //on créer le service pour l'URL des tweets
    }

    //pour injecter deux Retrofit différents (par exemple un pour l'API Twitter et un autre pour une autre API) on met @Name("")

}
