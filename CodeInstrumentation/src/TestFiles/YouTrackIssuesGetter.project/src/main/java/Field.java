import javax.xml.bind.annotation.*;

@XmlRootElement (name = "field")
@XmlAccessorType(XmlAccessType.FIELD)
public class Field {

    @XmlAttribute(name = "name")
    private String name;

    @XmlElement(name = "value")
    private String value;

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
