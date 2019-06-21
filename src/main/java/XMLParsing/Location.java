package XMLParsing;

import javax.xml.bind.annotation.*;

/**
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "place",
        "message"
})
@XmlRootElement(name = "location")
public class Location {

    @XmlAttribute
    private String place;
    @XmlElement
    private String message;

    /**
     * Gets the value of the message property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the value of the message property.
     *
     * @param message
     *     possible object is
     *     {@link String }
     *
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the value of the place attribute.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPlace() {
        return place;
    }

}