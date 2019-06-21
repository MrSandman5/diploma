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
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.util.logging.ConsoleHandler;

public class ModifiedMain {

    private static final Logger instrumentationLogger = Logger.getLogger(ModifiedMain.class.getSimpleName());

    private static final Handler instrumentationHandler = new ConsoleHandler();

    public static void main(String[] args) {
        instrumentationHandler.setLevel(Level.ALL);
        instrumentationLogger.addHandler(instrumentationHandler);
        instrumentationLogger.setLevel(Level.ALL);
        int max = 1000, after = 0;
        boolean flag = false;
        while (true) {
            String restUri = "https://youtrack.jetbrains.net/rest/issue/byproject/KT?filter=Bug+%23Submitted&after=" + after + "&max=" + max;
            Client client = Client.create();
            WebResource webResource = client.resource(restUri);
            ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);
            String output = response.getEntity(String.class);
            {
                instrumentationLogger.log(Level.FINE, "Entering to IfStmt block in Main.java class, in main method.");
                if ("".equals(StringUtils.substringBetween(output, "<issues>", "</issues>"))) {
                    flag = true;
                    break;
                }
                instrumentationLogger.log(Level.FINE, "Successful exit from IfStmt block in Main.java class, in main method.");
            }
            BufferedWriter bw;
            try {
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("C:\\Users\\saf-s\\Desktop\\Work_and_projects\\YouTrackIssuesGetter.project\\BugIssues.xml"), StandardCharsets.UTF_8));
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
            {
                try {
                    for (Issue issue : issues.getIssueList()) {
                        tmpField = new Field();
                        fieldList = new ArrayList<>();
                        tmpIssue = new Issue();
                        String kotlinDesc = "";
                        {
                            try {
                                for (Field field : issue.getFieldList()) {
                                    String desc = field.getValue();
                                    {
                                        instrumentationLogger.log(Level.FINE, "Entering to IfStmt block in Main.java class, in main method.");
                                        if (desc.contains("```kotlin")) {
                                            kotlinDesc = StringUtils.substringBetween(desc, "```kotlin", "```");
                                            tmpField.setName(field.getName());
                                            tmpField.setValue(kotlinDesc);
                                        }
                                        instrumentationLogger.log(Level.FINE, "Successful exit from IfStmt block in Main.java class, in main method.");
                                    }
                                }
                            } catch (Throwable ex) {
                                instrumentationLogger.log(Level.SEVER, "Exit with exception from ForEachStmt block in Main.java class, in main method, with Throwable exception.", ex);
                            } finally {
                                instrumentationLogger.log(Level.FINE, "Exiting from ForEachStmt block in Main.java class, in main method.");
                            }
                        }
                        {
                            instrumentationLogger.log(Level.FINE, "Entering to IfStmt block in Main.java class, in main method.");
                            if ("".equals(kotlinDesc))
                                continue;
                            instrumentationLogger.log(Level.FINE, "Successful exit from IfStmt block in Main.java class, in main method.");
                        }
                        fieldList.add(tmpField);
                        tmpIssue.setId(issue.getId());
                        tmpIssue.setEntityId(issue.getEntityId());
                        tmpIssue.setFieldList(fieldList);
                        issueList.add(tmpIssue);
                    }
                } catch (Throwable ex) {
                    instrumentationLogger.log(Level.SEVER, "Exit with exception from ForEachStmt block in Main.java class, in main method, with Throwable exception.", ex);
                } finally {
                    instrumentationLogger.log(Level.FINE, "Exiting from ForEachStmt block in Main.java class, in main method.");
                }
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
