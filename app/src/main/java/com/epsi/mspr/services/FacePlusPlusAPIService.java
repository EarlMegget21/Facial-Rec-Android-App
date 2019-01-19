package com.epsi.mspr.services;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Service to compare two pictures via the FacePlusPlus API
 */
public interface FacePlusPlusAPIService {

    /**
     * To request the FacePlusPlus REST API to compare two pictures
     * @return observable containing the request response as string
     */
    @POST("compare")
    @Multipart
    Observable<Response > comparePictures(
            @Part("api_key") RequestBody api_key,
            @Part("api_secret") RequestBody api_secret,
            @Part MultipartBody.Part image_file1,
            @Part MultipartBody.Part image_file2
    );

    class Response{
        private float confidence;

        public float getConfidence() {
            return confidence;
        }
    }
}
