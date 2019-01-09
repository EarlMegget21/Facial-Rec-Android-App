package com.backelite.mspr.models;

public class DisplayedModel<T> {

    private T successObject;

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
