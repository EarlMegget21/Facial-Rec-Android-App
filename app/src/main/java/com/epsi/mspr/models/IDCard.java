package com.epsi.mspr.models;

/**
 * Class that holds relevant information on the ID card
 */
public class IDCard {

    /**
     * Last name found on the ID card
     */
    private String lastName;

    /**
     * First name found on the ID card
     */
    private String firstName;

    /**
     * Number identifying the ID card
     */
    private int IDNumber;

    public String getLastName() {
        return lastName;
    }

    public int getIDNumber() {
        return IDNumber;
    }

    public String getFirstName() {
        return firstName;
    }
}
