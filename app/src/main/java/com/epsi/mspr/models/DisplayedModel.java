package com.epsi.mspr.models;

/**
 * Class that holds data if success or error message if fail
 *  when retrieving data from the repository
 * @param <T> generic data
 */
public class DisplayedModel<T> {

    /**
     * Data retrieved
     */
    private T successObject;

    /**
     * Error message that occured
     */
    private String errorMessage;

    public DisplayedModel(T successObject, String errorMessage) {
        this.successObject = successObject;
        this.errorMessage = errorMessage;
    }

    public T getSuccessObject() {
        return successObject;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
