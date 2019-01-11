package com.epsi.mspr.dependancies;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ContextModule { //ici on a exporté la création du contexte dans ce module (donc enlevé de AppModule) car on pourra juste copier-coller cette classe dans d'autres projets

    private final Context context;

    public ContextModule(Context context) { //on doit lui mettre un constructeur et lui mettre le contexte en attribut car Dagger ne peut pas nous le fournir
        this.context = context;
    }

    @Provides
    @Singleton
    public Context provideContext(){ //du coup on renvoie le contexte en attribut
        return context;
    }

}
