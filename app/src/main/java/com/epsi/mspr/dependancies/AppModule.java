package com.epsi.mspr.dependancies;

import com.epsi.mspr.services.FacePlusPlusAPIService;
import com.epsi.mspr.services.FirebaseAPIService;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Here we build every Singleton whom we don't have access to the source code,
 * in those ones we add @Singleton to the class and @Inject to the constructor.
 */
@Module // attached to the app lifecycle
public class AppModule {

    /**
     * Allows to inject the OkHttpClient in many Service without have to rebuild it every time.
     */
    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient() { //on peut pas le virer d'ici car on n'a pas accès au constructeur de Gson pour lui mettre @Inject (on ne peut pas l'exporter dans un module apart)
        return new OkHttpClient.Builder().addNetworkInterceptor(new StethoInterceptor()).build();
    }

    /**
     * Allows to inject the Retrofit Builder in many Service without have to rebuild it every time.
     */
    @Provides
    @Singleton
    public Retrofit.Builder provideRetrofitBuilder() {
        return new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create()); // to parse JSON
    }

    /**
     * Allows to create the Api service implementation thanks to Retrofit and inject it in many classes without have to rebuild it every time.
     */
    @Provides
    @Singleton
    public FirebaseAPIService provideFirebaseApiService(Retrofit.Builder builder, OkHttpClient okHttpClient) {
        return builder.baseUrl("https://mspr-3d59a.firebaseio.com/") // API domain
                .client(okHttpClient)
                .build()
                .create(FirebaseAPIService.class); // create the service implementation
    }

    /**
     * Allows to create the Api service implementation thanks to Retrofit and inject it in many classes without have to rebuild it every time.
     */
    @Provides
    @Singleton
    public FacePlusPlusAPIService provideFacePlusPlusAPIService(Retrofit.Builder builder, OkHttpClient okHttpClient) {
        return builder.baseUrl("https://api-us.faceplusplus.com/facepp/v3/") // API domain
                .client(okHttpClient)
                .build()
                .create(FacePlusPlusAPIService.class); // create the service implementation
    }
}
