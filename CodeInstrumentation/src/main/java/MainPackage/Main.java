package MainPackage;

import XMLParsing.*;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import org.apache.commons.cli.*;

import javax.xml.bind.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class Main {

    private static String INPUT_FILE_PATH;
    private static String RULES_FILE_PATH;
    static final String LOGGER_NAME = "instrumentationLogger";
    static final String HANDLER_NAME = "instrumentationHandler";

    public static void main(String[] args) {
        parseCommandLine(args);
        File inputFile = new File(INPUT_FILE_PATH), rulesFile = new File(RULES_FILE_PATH);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Settings.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setEventHandler(event -> false);
            Settings settings = (Settings) unmarshaller.unmarshal(rulesFile);
            Contexts contexts = settings.getContexts();
            Rules rules = settings.getRules();
            rules.getRule().forEach(rule -> contexts.getContext().forEach(context -> {
                if (rule.getWorkspace().equals(context.getName())){
                    context.getMethod().forEach(method -> {
                        String methodName = method.substring(method.lastIndexOf(".") + 1);
                        String fileName = method.substring(0, method.lastIndexOf("."));
                        if ("*".equals(fileName)) {
                            processClasses(inputFile, ".java", methodName , rule, true);
                        }
                        else processClasses(inputFile, fileName + ".java", methodName , rule, false);
                    });
                }
            }));
        } catch (JAXBException e) {
            e.printStackTrace();
       }
    }

    private static void parseCommandLine(String[] args){
        Options options = new Options();
        Option input = new Option( "i", "input", true, "input file name");
        input.setRequired(true);
        options.addOption(input);

        Option rules = new Option("r", "rules", true, "rules file name");
        rules.setRequired(true);
        options.addOption(rules);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }

        INPUT_FILE_PATH = cmd.getOptionValue("input");
        RULES_FILE_PATH = cmd.getOptionValue("rules");
    }

    private static void processClasses(File inputFile, String classname, String method, Rule rule, boolean check){
        List<File> sourceFiles = searchFiles(classname, inputFile, check);
        for (File sourceFile : sourceFiles) {
            String sourceFileName = sourceFile.getName();
            String content = getFileContent(sourceFile.getAbsolutePath());
            CompilationUnit comUnit = JavaParser.parse(content);
            String newFileName = "Modified" + sourceFileName.substring(0, sourceFileName.lastIndexOf("."));
            prepareFile(comUnit, newFileName);
            InstrumentationClass bm = new InstrumentationClass(comUnit, method, rule, sourceFileName);
            comUnit = bm.doInstrumentation();
            comUnit.getTypes().forEach(type -> {
                if (type.isClassOrInterfaceDeclaration()) type.setName(newFileName);
            });
            String modifiedFile = sourceFile.getAbsolutePath().substring(0, sourceFile.getAbsolutePath().lastIndexOf("\\")) + "\\Modified" + sourceFileName;
            try {
                Files.write(new File(modifiedFile).toPath(), comUnit.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<File> searchFiles(String filename, File dirname, boolean check){
        List<File> fileList = new ArrayList<>();
        String dir = dirname.getAbsolutePath();
        try (Stream<Path> stream = Files.find(Paths.get(dir), 25,
                (path, attr) -> check ? path.getFileName().toString().endsWith(filename) : path.getFileName().toString().equals(filename))) {
            stream.forEach(name -> fileList.add(name.toFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileList;
    }

    private static String getFileContent(String filepath){
        StringBuilder sb = new StringBuilder();
        try(Stream<String> stream = Files.lines( Paths.get(filepath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> sb.append(s.trim()).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private static void prepareFile(CompilationUnit comUnit, String newFileName){
        NodeList<ImportDeclaration> imports = comUnit.getImports();
        if (!imports.contains(new ImportDeclaration("java.util.logging.Logger", false, false)))
            imports.add(new ImportDeclaration("java.util.logging.Logger", false, false));
        if (!imports.contains(new ImportDeclaration("java.util.logging.Level", false, false)))
            imports.add(new ImportDeclaration("java.util.logging.Level", false, false));
        if (!imports.contains(new ImportDeclaration("java.util.logging.Handler", false, false)))
            imports.add(new ImportDeclaration("java.util.logging.Handler", false, false));
        if (!imports.contains(new ImportDeclaration("java.util.logging.ConsoleHandler", false, false)))
            imports.add(new ImportDeclaration("java.util.logging.ConsoleHandler", false, false));
        comUnit.getTypes().forEach(type -> {
            String loggerValue = "Logger.getLogger(" + newFileName + ".class.getSimpleName())";
            String handlerValue = "new ConsoleHandler()";
            FieldDeclaration fd = new FieldDeclaration(new NodeList<>(Modifier.privateModifier(), Modifier.staticModifier(), Modifier.finalModifier()),
                    new VariableDeclarator(JavaParser.parseClassOrInterfaceType(Logger.class.getSimpleName()), LOGGER_NAME)
                            .setInitializer(loggerValue));
            if (!type.containsWithin(fd)) type.getMembers().add(0, fd);
            fd = new FieldDeclaration(new NodeList<>(Modifier.privateModifier(), Modifier.staticModifier(), Modifier.finalModifier()),
                    new VariableDeclarator(JavaParser.parseClassOrInterfaceType(Handler.class.getSimpleName()), HANDLER_NAME)
                            .setInitializer(handlerValue));
            if (!type.containsWithin(fd)) type.getMembers().add(1, fd);
        });
    }

}
