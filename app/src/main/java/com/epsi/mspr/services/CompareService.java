package com.epsi.mspr.services;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static com.epsi.mspr.application.MainApplication.getApplication;

@Singleton
public class CompareService {

    /**
     * Indicate the accuracy percentage where two faces are considered as the same person.
     */
    private static final int ACCURACY_PERCENTAGE = 75;

    /**
     * Service used to make http requests to the REST API thanks to Retrofit.
     */
    private FacePlusPlusAPIService facePlusPlusApi;

    @Inject
    public CompareService(FacePlusPlusAPIService service) {
        facePlusPlusApi = service;
    }

    /**
     * Method to compare face pictures asynchronously.
     * @param idFace   representing the picture extracted from the id card
     * @param realFace representing the real picture taken
     * @return observable containing an object containing an error if the face comparison failed or a boolean indicating the result otherwise
     */
    @SuppressLint("CheckResult")
    public Observable<Boolean> compareFaces(Bitmap idFace, Bitmap realFace) {

        File file1 = new File(getApplication().getCacheDir(), "file1");
        OutputStream os;
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file1);
            os = new BufferedOutputStream(fos);
            idFace.compress(Bitmap.CompressFormat.JPEG, 10, os);
            os.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        MultipartBody.Part image1 = MultipartBody.Part.createFormData(
                "image_file1",
                file1.getName(),
                RequestBody.create(MediaType.parse("image"), file1));

        int width = realFace.getWidth();
        int height = realFace.getHeight();
        realFace = Bitmap.createBitmap(realFace, 0, 0, width > 4096 ? 4096 : width, height > 4096 ? 4096 : height);
        File file2 = new File(getApplication().getCacheDir(), "file2");
        try {
            fos = new FileOutputStream(file2);
            os = new BufferedOutputStream(fos);
            realFace.compress(Bitmap.CompressFormat.JPEG, 10, os);
            os.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        MultipartBody.Part image2 = MultipartBody.Part.createFormData(
                "image_file2",
                file2.getName(),
                RequestBody.create(MediaType.parse("image"), file2));

        return facePlusPlusApi.comparePictures(
                RequestBody.create(MediaType.parse("text/plain"), "A-L0jyGpFLO518qKJDIbghezCfUkCKO4"),
                RequestBody.create(MediaType.parse("text/plain"), "mb9WpGPLplwb91M7SalxSbdN2GEhxlzI"),
                image1, image2
        )
                .subscribeOn(Schedulers.io()) // to execute the request in another thread (Exception otherwise)
                .observeOn(AndroidSchedulers.mainThread()) //to treat the response in the UI thread allowing us to touch the view
                .map(response -> response.getConfidence() > ACCURACY_PERCENTAGE); // to convert the response as a boolean (true if it match, false otherwise)
    }
}
