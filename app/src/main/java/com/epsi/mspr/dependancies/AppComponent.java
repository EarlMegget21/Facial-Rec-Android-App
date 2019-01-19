package com.epsi.mspr.dependancies;

import com.epsi.mspr.archi.view_models.VerifyIDViewModel;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {AppModule.class, BindModule.class}) //tableau des classes des modules (peut avoir plusieur modules)
@Singleton //indique que ça sera un singleton
public abstract class AppComponent { //représente l'injecteur (permet de fournir les instances quand on le spécifie). La classe héritant sera générée par Dagger lors du build. Toutes les methodes dessous seront implémentées. Les getters, setters, autres methodes seront générées

    public abstract void inject(VerifyIDViewModel verifyIDViewModel); //on doit spécifier l'Activity dans laquelle on injecte les attributs (Presenter, etc..) de l'AppModule dans les attributs portant @Inject de l'activité en question, on doit donc faire une methode par vue dans laquelle on inject

}