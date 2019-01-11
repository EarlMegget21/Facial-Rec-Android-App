package com.epsi.mspr.application;

import android.app.Application;

import com.epsi.mspr.dependancies.AppComponent;
import com.epsi.mspr.dependancies.ContextModule;
import com.epsi.mspr.dependancies.DaggerAppComponent;
import com.facebook.stetho.Stetho;

public class MainApplication extends Application {

    private AppComponent appComponent; //on stock le component (injecteur) dans l'Application pour être récupérable de partout

    private static MainApplication INSTANCE; //pour pouvoir être récupéré ailleurs

    public static MainApplication getINSTANCE() {
        return INSTANCE;
    }
    public static void setInstance(MainApplication instance) {
        MainApplication.INSTANCE = instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setInstance(this);

        Stetho.initializeWithDefaults(this); //pour utiliser Setho (visualisation des appels https dans une fenêtre et pleins d'autres choses: vues, etc.. comme les dev tools en Web)

        appComponent = DaggerAppComponent.builder() //le DaggerAppComponent est la classe implémentant AppComponent qui est généré par Dagger une fois qu'on a build le projet (il nous créé aussi toutes les methodes, getter, setter, etc..)
                .contextModule(new ContextModule(this)) //obligé de mettre ça car le contexte ne peut pas être fourni par Dagger et vu qu'on l'a exporté dans un autre module, on a viré le context de l'AppModule (avant on lui fournissait en faisant new AppModule(this)
                .build();
    }

    public static MainApplication app() {
        return INSTANCE;
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
