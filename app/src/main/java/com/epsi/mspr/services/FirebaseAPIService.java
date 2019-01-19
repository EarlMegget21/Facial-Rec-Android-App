package com.epsi.mspr.services;

import com.epsi.mspr.models.IDCard;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Service to download or upload lines from/on the Firebase database via the API
 */
public interface FirebaseAPIService {

    /**
     * To request the Firebase REST API to search an ID card in the database
     * @param idCardNumber number to search
     * @return observable containing the id card retrieved or null otherwise
     */
    @GET("idCards/{idCardNumber}.json")
    Observable<IDCard> getIDCard(
            @Path("idCardNumber") long idCardNumber
    );

    /**
     * To request the Firebase REST API to insert a new ID card in the database.
     * @param card to insert
     * @param idCardNumber to have it as the key in the JSON format
     * @return observable containing the request response as string
     */
    @PUT("idCards/{idCardNumber}.json")
    Observable<String> addIDCard(
            @Body IDCard card,
            @Path("idCardNumber") long idCardNumber
    );
}
