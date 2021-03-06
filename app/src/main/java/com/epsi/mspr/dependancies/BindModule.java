package com.epsi.mspr.dependancies;

import com.epsi.mspr.archi.repositories.IDRepository;
import com.epsi.mspr.archi.repositories.IDRepositoryFireBase;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;

@Module //au build du projet, cette classe abstraite sera implémentée
/* on a exporté le provideIDRepository dans ce module plutôt que l'AppModule car au moins si on
 passe de IDRepositoryFireBase à IDRepositoryBDD, on a pas besoin de modifier tous les paramêtres
 (on change juste le type de la classe ici alors que sinon on doit enlever FirebaseInstance dans
 l'AppModule par exemple) */
public abstract class BindModule {

    /* on change le Provide par Bind (on peut pas avoir les deux) Provide créer grâce à un
    constructeur si on Inject une classe concrète et l'injecter, Bind va récupérer l'implémentation
    de l'interface puis l'injecter (Provide pour injecter une classe, Bind pour injecter une interface)
    Ex: si on transforme VerifyIDViewModel en interface et qu'on lui créer une classe implémentant, alors
    il faudra dans ce module, créer la méthode abstraite provideMainPresenter avec en paramettre la
    classe implémentant qu'on aura créé en mettant @Bind sinon dans la MainActivity il y aura une
    erreur car on essaie d'injecter une interface. On pourrait sinon injecter directement la classe
    implémentant mais c'est pas le but.
     */
    @Binds
    @Singleton //on ne peut pas mettre @Singleton sur la classe IDRepositoryFireBase ou IDRepository et ne pas le mettre ici, Dagger ne le considèrera pas comme un Singleton
    /* méthode abstraite, au build Dagger l'implémentera et si on veut changer d'impléméntation de
     IDRepository, il suffit juste de modifier le paramêtre ici et de build */
    public abstract IDRepository provideIDRepository(IDRepositoryFireBase repo);

}
