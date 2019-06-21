package com.mainpackage;

import com.innerdata.InstrumentationData;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.xmlparsing.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InstrumentationClass {

    public enum Items{
        IF,
        THEN,
        ELSE,
        SWITCH,
        CASE,
        WHILE,
        DO,
        FOR,
        FOREACH,
        ALL
    }

    public enum Locations{
        BEFORE,
        AFTER,
        AFTER_RETURN,
        AFTER_THROWING
    }

    public enum Metadata{
        LOCATION,
        ITEM,
        CLASS,
        METHOD,
        VARIABLE,
        EXCEPTION
    }

    private CompilationUnit sourceCode;
    private Rule rule;
    private String methodName;
    private String className;

    InstrumentationClass(CompilationUnit sourceCode, String methodName, Rule rule, String className) {
        this.sourceCode = sourceCode;
        this.methodName = methodName;
        this.rule = rule;
        this.className = className;
    }

    CompilationUnit doInstrumentation(){
        InstrumentationData data = new InstrumentationData(rule.getItem());
        List<MethodDeclaration> methods = "*".equals(methodName) ? processAllMethods() : processOneMethod();
        configLogger(methods);
        data.getItems().forEach(items -> sourceCode = instrument(methods, items.getType(), items.getLocation()));
        return sourceCode;
    }

    private List<MethodDeclaration> processOneMethod(){
        List<MethodDeclaration> temp = new ArrayList<>();
        sourceCode.getTypes().forEach(type -> type.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration n, Void arg){
                super.visit(n, arg);
                String currentMethod = n.getNameAsString();
                if (methodName.equals(currentMethod)) temp.add(n);
            }
        }, null));
        return temp;
    }

    private List<MethodDeclaration> processAllMethods(){
        List<MethodDeclaration> temp = new ArrayList<>();
        sourceCode.getTypes().forEach(type -> type.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration n, Void arg){
                super.visit(n, arg);
                temp.add(n);
            }
        }, null));
        return temp;
    }

    private CompilationUnit instrument(List<MethodDeclaration> methods, Items item, List<com.innerdata.Locations> locations){
        for (MethodDeclaration method : methods){
            switch (item) {
                case IF:
                    InstrumentationMethods.ifInstrumentation(className, methodName, method, locations);
                    break;
                case THEN:
                    InstrumentationMethods.thenInstrumentation(className, methodName, method, locations);
                    break;
                case ELSE:
                    InstrumentationMethods.elseInstrumentation(className, methodName, method, locations);
                    break;
                case SWITCH:
                    InstrumentationMethods.switchInstrumentation(className, methodName, method, locations);
                    break;
                case WHILE:
                    InstrumentationMethods.whileInstrumentation(className, methodName, method, locations);
                    break;
                case DO:
                    InstrumentationMethods.doWhileInstrumentation(className, methodName, method, locations);
                    break;
                case FOR:
                    InstrumentationMethods.forInstrumentation(className, methodName, method, locations);
                    break;
                case FOREACH:
                    InstrumentationMethods.forEachInstrumentation(className, methodName, method, locations);
                    break;
                /*case CASE:
                    InstrumentationMethods.caseInstrumentation(className, methodName, method, locations);
                    break;*/
            }
        }
        return sourceCode;
    }

    private void configLogger(List<MethodDeclaration> methods){
        for (MethodDeclaration method : methods) {
            Optional<BlockStmt> block = method.getBody();
            if (block.isPresent()){
                List<Statement> statements = block.get().getStatements();
                statements.add(0, JavaParser.parseStatement(Main.HANDLER_NAME + ".setLevel(Level.ALL);"));
                statements.add(1, JavaParser.parseStatement(Main.LOGGER_NAME + ".addHandler(" + Main.HANDLER_NAME + ");"));
                statements.add(2, JavaParser.parseStatement(Main.LOGGER_NAME + ".setLevel(Level.ALL);"));
            }
        }
    }

}
