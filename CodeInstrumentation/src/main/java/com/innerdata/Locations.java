package com.innerdata;

import com.mainpackage.InstrumentationClass;

public class Locations {

    private InstrumentationClass.Locations location;
    private String message;

    Locations(InstrumentationClass.Locations location, String message) {
        this.location = location;
        this.message = message;
    }

    public InstrumentationClass.Locations getLocation() {
        return location;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Set the value of the current argument and change message.
     *
     * @param arg
     *     possible object is
     *     {@link String }
     * @param metadata
     *     possible object is
     *     {@link String }
     */
    public void setArg(String arg, String metadata) {
        StringBuilder sb = new StringBuilder(message);
        sb.replace(sb.indexOf(metadata), sb.indexOf(metadata) + metadata.length(), arg);
        message = sb.toString();
    }

}
