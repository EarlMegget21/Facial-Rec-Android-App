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

    /**
     * Error code that occured
     */
    private int errorCode;

    public final static int NOT_FOUND = 1;
    public final static int OTHER = 2;
    public final static int NO_ERROR = 0;

    public DisplayedModel(T successObject, String errorMessage, int errorCode) {
        this.successObject = successObject;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public T getSuccessObject() {
        return successObject;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
