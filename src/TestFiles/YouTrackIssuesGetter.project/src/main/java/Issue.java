import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement (name = "issue")
@XmlAccessorType (XmlAccessType.FIELD)
public class Issue {

    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "entityId")
    private String entityId;

    @XmlElement(name = "field")
    private List<Field> fieldList;

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getEntityId() { return entityId; }

    public void setEntityId(String entityID) { this.entityId = entityID; }

    public List<Field> getFieldList() {
        return fieldList;
    }

    public void setFieldList(List<Field> fieldList) {
        this.fieldList = fieldList;
    }
}
