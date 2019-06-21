import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        int max = 1000, after = 0;
        boolean flag = false;
        while (true) {
            String restUri = "https://youtrack.jetbrains.net/rest/issue/byproject/KT?filter=Bug+%23Submitted&after=" + after + "&max=" + max;
            Client client = Client.create();
            WebResource webResource = client.resource(restUri);
            ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);

            String output = response.getEntity(String.class);
            if ("".equals(StringUtils.substringBetween(output, "<issues>", "</issues>"))) {
                flag = true;
                break;
            }
            BufferedWriter bw;
            try {
                bw = new BufferedWriter
                        (new OutputStreamWriter(new FileOutputStream("C:\\Users\\saf-s\\Desktop\\Work_and_projects\\YouTrackIssuesGetter.project\\BugIssues.xml")
                                , StandardCharsets.UTF_8));
                bw.write(output + "\n");
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            after += 1000;
        }
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Issues.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            File XMLfile = new File("C:\\Users\\saf-s\\Desktop\\Work_and_projects\\YouTrackIssuesGetter.project\\BugIssues.xml");

            Issues issues = (Issues) jaxbUnmarshaller.unmarshal(XMLfile);
            Issues bugIssues = new Issues();
            Field tmpField;
            List<Field> fieldList;
            Issue tmpIssue;
            List<Issue> issueList = new ArrayList<>();
            for (Issue issue : issues.getIssueList()) {
                tmpField = new Field();
                fieldList = new ArrayList<>();
                tmpIssue = new Issue();
                String kotlinDesc = "";
                for (Field field : issue.getFieldList()) {

                    String desc = field.getValue();
                    if (desc.contains("```kotlin")) {
                        kotlinDesc = StringUtils.substringBetween(desc, "```kotlin", "```");
                        tmpField.setName(field.getName());
                        tmpField.setValue(kotlinDesc);
                    }
                }
                if ("".equals(kotlinDesc)) continue;
                fieldList.add(tmpField);
                tmpIssue.setId(issue.getId());
                tmpIssue.setEntityId(issue.getEntityId());
                tmpIssue.setFieldList(fieldList);
                issueList.add(tmpIssue);
            }
            bugIssues.setIssueList(issueList);

            Marshaller jaxbmarshaller = jaxbContext.createMarshaller();
            jaxbmarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            File finalFile = new File("C:\\Users\\saf-s\\Desktop\\Work_and_projects\\YouTrackIssuesGetter.project\\Issues.xml");

            jaxbmarshaller.marshal(bugIssues, finalFile);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
