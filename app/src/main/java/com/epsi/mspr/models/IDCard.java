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
    private long IDNumber;

    public IDCard(String lastName, String firstName, long IDNumber) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.IDNumber = IDNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public long getIDNumber() {
        return IDNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setIDNumber(long IDNumber) {
        this.IDNumber = IDNumber;
    }
}
