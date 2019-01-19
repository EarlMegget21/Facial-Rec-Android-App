package com.epsi.mspr.application;

import android.app.Application;

import com.epsi.mspr.dependancies.AppComponent;
import com.epsi.mspr.dependancies.DaggerAppComponent;
import com.facebook.stetho.Stetho;

public class MainApplication extends Application {

    private AppComponent appComponent; //on stock le component (injecteur) dans l'Application pour être récupérable de partout

    private static MainApplication INSTANCE; //pour pouvoir être récupéré ailleurs

    public static void setInstance(MainApplication instance) {
        MainApplication.INSTANCE = instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setInstance(this);

        Stetho.initializeWithDefaults(this); //pour utiliser Setho (visualisation des appels https dans une fenêtre et pleins d'autres choses: vues, etc.. comme les dev tools en Web)

        appComponent = DaggerAppComponent.builder().build(); //le DaggerAppComponent est la classe implémentant AppComponent qui est généré par Dagger une fois qu'on a build le projet (il nous créé aussi toutes les methodes, getter, setter, etc..)
    }

    public static MainApplication getApplication() {
        return INSTANCE;
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
