package com.mainpackage;

import com.innerdata.Locations;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class InstrumentationMethods {

    private static List<InstrumentationClass.Metadata> processMetadata(String message){
        List<InstrumentationClass.Metadata> temp = new ArrayList<>();
        for (InstrumentationClass.Metadata metadata : InstrumentationClass.Metadata.values()) {
            if (message.contains(metadata.name())){
                temp.add(metadata);
            }
        }
        return temp;
    }

    static void ifInstrumentation(String className, String methodName, MethodDeclaration method, List<Locations> locations){
        method.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(IfStmt n, Void arg) {
                super.visit(n, arg);
                execute(n, className, methodName, locations);
            }
        }, null);
    }

    static void thenInstrumentation(String className, String methodName, MethodDeclaration method, List<Locations> locations){
        method.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(IfStmt n, Void arg) {
                super.visit(n, arg);
                Statement thenStmt = n.getThenStmt();
                thenElseExecute(thenStmt, className, methodName, locations);
            }
        }, null);
    }

    static void elseInstrumentation(String className, String methodName, MethodDeclaration method, List<Locations> locations){
        method.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(IfStmt n, Void arg) {
                super.visit(n, arg);
                Optional<Statement> elseBlock = n.getElseStmt();
                if (elseBlock.isPresent()) {
                    Statement elseStmt = elseBlock.get();
                    thenElseExecute(elseStmt, className, methodName, locations);
                }
            }
        }, null);
    }

    static void switchInstrumentation(String className, String methodName, MethodDeclaration method, List<Locations> locations) {
        method.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(SwitchStmt n, Void arg) {
                super.visit(n, arg);
                execute(n, className, methodName, locations);
            }
        }, null);
    }

    /*static void caseInstrumentation(String className, String methodName, MethodDeclaration method, List<InnerData.Locations> locations){
        method.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(SwitchStmt n, Void arg) {
                super.visit(n, arg);
                entryExecute(n, className, methodName, locations);
            }
        }, null);
    }*/

    static void whileInstrumentation(String className, String methodName, MethodDeclaration method, List<Locations> locations){
        method.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(WhileStmt n, Void arg) {
                super.visit(n, arg);
                execute(n, className, methodName, locations);
            }
        }, null);
    }

    static void doWhileInstrumentation(String className, String methodName, MethodDeclaration method, List<Locations> locations){
        method.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(DoStmt n, Void arg) {
                super.visit(n, arg);
                execute(n, className, methodName, locations);
            }
        }, null);
    }

    static void forInstrumentation(String className, String methodName, MethodDeclaration method, List<Locations> locations){
        method.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ForStmt n, Void arg) {
                super.visit(n, arg);
                execute(n, className, methodName, locations);
            }
        }, null);
    }

    static void forEachInstrumentation(String className, String methodName, MethodDeclaration method, List<Locations> locations){
        method.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ForEachStmt n, Void arg) {
                super.visit(n, arg);
                execute(n, className, methodName, locations);
            }
        }, null);
    }

    private static void execute(Statement n, String className, String methodName, List<Locations> locations){
        Optional<Node> parentNode = n.getParentNode();
        if (parentNode.isPresent()) {
            BlockStmt parent = (BlockStmt) parentNode.get();
            BlockStmt replacement = new BlockStmt(new NodeList<>(n));
            for (Locations location : locations) {
                List<InstrumentationClass.Metadata> currentMetadata = processMetadata(location.getMessage());
                if (currentMetadata.contains(InstrumentationClass.Metadata.CLASS)) location.setArg(className, InstrumentationClass.Metadata.CLASS.name());
                if (currentMetadata.contains(InstrumentationClass.Metadata.METHOD)) location.setArg(methodName, InstrumentationClass.Metadata.METHOD.name());
                if (currentMetadata.contains(InstrumentationClass.Metadata.ITEM)) location.setArg(n.getClass().getSimpleName(), InstrumentationClass.Metadata.ITEM.name());
                switch (location.getLocation()) {
                    case BEFORE:
                        if (currentMetadata.contains(InstrumentationClass.Metadata.LOCATION))
                            location.setArg("Entering to", InstrumentationClass.Metadata.LOCATION.name());
                        replacement.addStatement(0, JavaParser.parseStatement(location.getMessage()));
                        break;
                    case AFTER_RETURN:
                        if (currentMetadata.contains(InstrumentationClass.Metadata.LOCATION))
                            location.setArg("Successful exit from", InstrumentationClass.Metadata.LOCATION.name());
                        Optional<TryStmt> currentReturnTry = replacement.findFirst(TryStmt.class, tryStmt -> tryStmt.getTryBlock().getStatements().contains(n));
                        if (currentReturnTry.isPresent()){
                            TryStmt tryBlock = currentReturnTry.get();
                            BlockStmt tryStmt = tryBlock.getTryBlock();
                            tryStmt.addStatement(JavaParser.parseStatement(location.getMessage()));
                            replacement.replace(tryBlock.getTryBlock(), tryStmt);
                        }
                        else replacement.addStatement(JavaParser.parseStatement(location.getMessage()));
                        break;
                    case AFTER_THROWING:
                        if (currentMetadata.contains(InstrumentationClass.Metadata.LOCATION))
                            location.setArg("Exit with exception from", InstrumentationClass.Metadata.LOCATION.name());
                        Optional<TryStmt> currentThrowTry = replacement.findFirst(TryStmt.class, tryStmt -> tryStmt.getTryBlock().getStatements().contains(n));
                        if (currentThrowTry.isPresent()){
                            TryStmt tryBlock = currentThrowTry.get();
                            Optional<BlockStmt> tempFinally = tryBlock.getFinallyBlock();
                            if (tempFinally.isPresent()) {
                                TryStmt tempTry = new TryStmt(tryBlock.getTryBlock(), tryBlock.getCatchClauses(), tryBlock.getFinallyBlock().get());
                                NodeList<CatchClause> catchClauses = tempTry.getCatchClauses();
                                if (catchClauses.size() != 0) {
                                    catchClauses.add(new CatchClause(JavaParser.parseParameter("Throwable ex"), new BlockStmt()));
                                    CatchClause lastCatch = catchClauses.get(catchClauses.size() - 1);
                                    if (currentMetadata.contains(InstrumentationClass.Metadata.EXCEPTION))
                                        location.setArg(lastCatch.getParameter().getTypeAsString(), InstrumentationClass.Metadata.EXCEPTION.name());
                                    if (currentMetadata.contains(InstrumentationClass.Metadata.VARIABLE))
                                        location.setArg(lastCatch.getParameter().getNameAsString(), InstrumentationClass.Metadata.VARIABLE.name());
                                    lastCatch.setBody(new BlockStmt(new NodeList<>(JavaParser.parseStatement(location.getMessage()))));
                                } else {
                                    tempTry.setCatchClauses(new NodeList<>(new CatchClause(JavaParser.parseParameter("Throwable ex"), new BlockStmt())));
                                    CatchClause onlyCatch = tempTry.getCatchClauses().get(0);
                                    if (currentMetadata.contains(InstrumentationClass.Metadata.EXCEPTION))
                                        location.setArg(onlyCatch.getParameter().getTypeAsString(), InstrumentationClass.Metadata.EXCEPTION.name());
                                    if (currentMetadata.contains(InstrumentationClass.Metadata.VARIABLE))
                                        location.setArg(onlyCatch.getParameter().getNameAsString(), InstrumentationClass.Metadata.VARIABLE.name());
                                    onlyCatch.setBody(new BlockStmt(new NodeList<>(JavaParser.parseStatement(location.getMessage()))));
                                }
                                replacement.replace(tryBlock, tempTry);
                            }
                        }
                        else {
                            TryStmt afterTry = new TryStmt(new BlockStmt(new NodeList<>(n)), new NodeList<>(
                                    new CatchClause(JavaParser.parseParameter("Throwable ex"), new BlockStmt())),
                                    new BlockStmt());
                            CatchClause onlyCatch = afterTry.getCatchClauses().get(0);
                            if (currentMetadata.contains(InstrumentationClass.Metadata.EXCEPTION))
                                location.setArg(onlyCatch.getParameter().getTypeAsString(), InstrumentationClass.Metadata.EXCEPTION.name());
                            if (currentMetadata.contains(InstrumentationClass.Metadata.VARIABLE))
                                location.setArg(onlyCatch.getParameter().getNameAsString(), InstrumentationClass.Metadata.VARIABLE.name());
                            onlyCatch.setBody(new BlockStmt(new NodeList<>(JavaParser.parseStatement(location.getMessage()))));
                            replacement.replace(n, afterTry);
                        }
                        break;
                    case AFTER:
                        if (currentMetadata.contains(InstrumentationClass.Metadata.LOCATION))
                            location.setArg("Exiting from", InstrumentationClass.Metadata.LOCATION.name());
                        Optional<TryStmt> currentAfterTry = replacement.findFirst(TryStmt.class, tryStmt -> tryStmt.getTryBlock().getStatements().contains(n));
                        if (currentAfterTry.isPresent()){
                            TryStmt tryBlock = currentAfterTry.get();
                            if (tryBlock.getFinallyBlock().isPresent()) {
                                BlockStmt finallyBlock = tryBlock.getFinallyBlock().get();
                                finallyBlock.addStatement(JavaParser.parseStatement(location.getMessage()));
                                replacement.replace(tryBlock.getFinallyBlock().get(), finallyBlock);
                            }
                            else {
                                TryStmt newTry = new TryStmt(tryBlock.getTryBlock(), tryBlock.getCatchClauses(), new BlockStmt());
                                newTry.setFinallyBlock(JavaParser.parseBlock(location.getMessage()));
                                replacement.replace(tryBlock, newTry);
                            }
                        }
                        else {
                            TryStmt afterTry = new TryStmt(new BlockStmt(new NodeList<>(n)),
                                    new NodeList<>(), new BlockStmt(new NodeList<>(JavaParser.parseStatement(location.getMessage()))));
                            replacement.replace(n, afterTry);
                        }
                        break;
                }
            }
            parent.replace(n, replacement);
        }
    }

    private static void thenElseExecute(Statement n, String className, String methodName, List<Locations> locations){
        BlockStmt replacement = new BlockStmt(new NodeList<>(n));
        for (Locations location : locations) {
            List<InstrumentationClass.Metadata> currentMetadata = processMetadata(location.getMessage());
            if (currentMetadata.contains(InstrumentationClass.Metadata.CLASS)) location.setArg(className, InstrumentationClass.Metadata.CLASS.name());
            if (currentMetadata.contains(InstrumentationClass.Metadata.METHOD)) location.setArg(methodName, InstrumentationClass.Metadata.METHOD.name());
            if (currentMetadata.contains(InstrumentationClass.Metadata.ITEM)) location.setArg(n.getClass().getSimpleName(), InstrumentationClass.Metadata.ITEM.name());
            switch (location.getLocation()) {
                case BEFORE:
                    if (currentMetadata.contains(InstrumentationClass.Metadata.LOCATION))
                        location.setArg("Entering to", InstrumentationClass.Metadata.LOCATION.name());
                    replacement.addStatement(0, JavaParser.parseStatement(location.getMessage()));
                    break;
                case AFTER_RETURN:
                    if (currentMetadata.contains(InstrumentationClass.Metadata.LOCATION))
                        location.setArg("Successful exit from", InstrumentationClass.Metadata.LOCATION.name());
                    Optional<TryStmt> currentReturnTry = replacement.findFirst(TryStmt.class, tryStmt -> tryStmt.getTryBlock().getStatements().contains(n));
                    if (currentReturnTry.isPresent()){
                        TryStmt tryBlock = currentReturnTry.get();
                        BlockStmt tryStmt = tryBlock.getTryBlock();
                        tryStmt.addStatement(JavaParser.parseStatement(location.getMessage()));
                        replacement.replace(tryBlock.getTryBlock(), tryStmt);
                    }
                    else replacement.addStatement(JavaParser.parseStatement(location.getMessage()));
                    break;
                case AFTER_THROWING:
                    if (currentMetadata.contains(InstrumentationClass.Metadata.LOCATION))
                        location.setArg("Exiting from", InstrumentationClass.Metadata.LOCATION.name());
                    Optional<TryStmt> currentThrowTry = replacement.findFirst(TryStmt.class, tryStmt -> tryStmt.getTryBlock().getStatements().contains(n));
                    if (currentThrowTry.isPresent()){
                        TryStmt tryBlock = currentThrowTry.get();
                        Optional<BlockStmt> tempFinally = tryBlock.getFinallyBlock();
                        if (tempFinally.isPresent()) {
                            TryStmt tempTry = new TryStmt(tryBlock.getTryBlock(), tryBlock.getCatchClauses(), tryBlock.getFinallyBlock().get());
                            NodeList<CatchClause> catchClauses = tempTry.getCatchClauses();
                            if (catchClauses.size() != 0) {
                                catchClauses.add(new CatchClause(JavaParser.parseParameter("Throwable ex"), new BlockStmt()));
                                CatchClause lastCatch = catchClauses.get(catchClauses.size() - 1);
                                if (currentMetadata.contains(InstrumentationClass.Metadata.EXCEPTION))
                                    location.setArg(lastCatch.getParameter().getTypeAsString(), InstrumentationClass.Metadata.EXCEPTION.name());
                                if (currentMetadata.contains(InstrumentationClass.Metadata.VARIABLE))
                                    location.setArg(lastCatch.getParameter().getNameAsString(), InstrumentationClass.Metadata.VARIABLE.name());
                                lastCatch.setBody(new BlockStmt(new NodeList<>(JavaParser.parseStatement(location.getMessage()))));

                            } else {
                                tempTry.setCatchClauses(new NodeList<>(new CatchClause(JavaParser.parseParameter("Throwable ex"), new BlockStmt())));
                                CatchClause onlyCatch = tempTry.getCatchClauses().get(0);
                                if (currentMetadata.contains(InstrumentationClass.Metadata.EXCEPTION))
                                    location.setArg(onlyCatch.getParameter().getTypeAsString(), InstrumentationClass.Metadata.EXCEPTION.name());
                                if (currentMetadata.contains(InstrumentationClass.Metadata.VARIABLE))
                                    location.setArg(onlyCatch.getParameter().getNameAsString(), InstrumentationClass.Metadata.VARIABLE.name());
                                onlyCatch.setBody(new BlockStmt(new NodeList<>(JavaParser.parseStatement(location.getMessage()))));
                            }
                            replacement.replace(tryBlock, tempTry);
                        }
                    }
                    else {
                        TryStmt afterTry = new TryStmt(new BlockStmt(new NodeList<>(n)), new NodeList<>(
                                new CatchClause(JavaParser.parseParameter("Throwable ex"), new BlockStmt())),
                                new BlockStmt());
                        CatchClause onlyCatch = afterTry.getCatchClauses().get(0);
                        if (currentMetadata.contains(InstrumentationClass.Metadata.EXCEPTION))
                            location.setArg(onlyCatch.getParameter().getTypeAsString(), InstrumentationClass.Metadata.EXCEPTION.name());
                        if (currentMetadata.contains(InstrumentationClass.Metadata.VARIABLE))
                            location.setArg(onlyCatch.getParameter().getNameAsString(), InstrumentationClass.Metadata.VARIABLE.name());
                        onlyCatch.setBody(new BlockStmt(new NodeList<>(JavaParser.parseStatement(location.getMessage()))));
                        replacement.replace(n, afterTry);
                    }
                    break;
                case AFTER:
                    if (currentMetadata.contains(InstrumentationClass.Metadata.LOCATION))
                        location.setArg("Exiting from", InstrumentationClass.Metadata.LOCATION.name());
                    Optional<TryStmt> currentAfterTry = replacement.findFirst(TryStmt.class, tryStmt -> tryStmt.getTryBlock().getStatements().contains(n));
                    if (currentAfterTry.isPresent()){
                        TryStmt tryBlock = currentAfterTry.get();
                        if (tryBlock.getFinallyBlock().isPresent()) {
                            BlockStmt finallyBlock = tryBlock.getFinallyBlock().get();
                            finallyBlock.addStatement(JavaParser.parseStatement(location.getMessage()));
                            replacement.replace(tryBlock.getFinallyBlock().get(), finallyBlock);
                        }
                        else {
                            TryStmt newTry = new TryStmt(tryBlock.getTryBlock(), tryBlock.getCatchClauses(), new BlockStmt());
                            newTry.setFinallyBlock(JavaParser.parseBlock(location.getMessage()));
                            replacement.replace(tryBlock, newTry);
                        }
                    }
                    else {
                        TryStmt afterTry = new TryStmt(new BlockStmt(new NodeList<>(n)),
                                new NodeList<>(), new BlockStmt(new NodeList<>(JavaParser.parseStatement(location.getMessage()))));
                        replacement.replace(n, afterTry);
                    }
                    break;
            }
        }
        n.replace(n, replacement);
    }

    /*private static void entryExecute(SwitchStmt n, String className, String methodName, List<InnerData.Locations> locations) {
        Optional<Node> parentNode = n.getParentNode();
        if (parentNode.isPresent()) {
            BlockStmt parent = (BlockStmt) parentNode.get();
            BlockStmt replacement = new BlockStmt(new NodeList<>(n));
            for (SwitchEntry entry : replacement.getStatement(0).asSwitchStmt().getEntries()){
                for (Locations location : locations) {
                    List<InstrumentationClass.Metadata> currentMetadata = processMetadata(location.getMessage());
                    if (currentMetadata.contains(InstrumentationClass.Metadata.CLASS))
                        location.setArg(className, InstrumentationClass.Metadata.CLASS.name());
                    if (currentMetadata.contains(InstrumentationClass.Metadata.METHOD))
                        location.setArg(methodName, InstrumentationClass.Metadata.METHOD.name());
                    if (currentMetadata.contains(InstrumentationClass.Metadata.ITEM))
                        location.setArg(n.getClass().getSimpleName(), InstrumentationClass.Metadata.ITEM.name());
                    switch (location.getLocation()) {
                        case BEFORE:
                            if (currentMetadata.contains(InstrumentationClass.Metadata.LOCATION))
                                location.setArg("Entering to", InstrumentationClass.Metadata.LOCATION.name());
                            entry.addStatement(0, JavaParser.parseStatement(location.getMessage()));
                            break;
                        case AFTER_RETURN:
                            if (currentMetadata.contains(InstrumentationClass.Metadata.LOCATION))
                                location.setArg("Successful exit from", InstrumentationClass.Metadata.LOCATION.name());
                            Optional<TryStmt> currentReturnTry = entry.findFirst(TryStmt.class, tryStmt -> tryStmt.getTryBlock().getStatements().containsAll(entry.getStatements()));
                            if (currentReturnTry.isPresent()) {
                                TryStmt tryBlock = currentReturnTry.get();
                                BlockStmt tryStmt = tryBlock.getTryBlock();
                                tryStmt.addStatement(JavaParser.parseStatement(location.getMessage()));
                                entry.replace(tryBlock.getTryBlock(), tryStmt);
                            } else entry.addStatement(entry.getStatements().size() - 1, JavaParser.parseStatement(location.getMessage()));
                            break;
                        case AFTER_THROWING:
                            if (currentMetadata.contains(InstrumentationClass.Metadata.LOCATION))
                                location.setArg("Exit with exception from", InstrumentationClass.Metadata.LOCATION.name());
                            Optional<TryStmt> currentThrowTry = entry.findFirst(TryStmt.class, tryStmt -> tryStmt.getTryBlock().getStatements().containsAll(entry.getStatements()));
                            if (currentThrowTry.isPresent()) {
                                TryStmt tryBlock = currentThrowTry.get();
                                Optional<BlockStmt> tempFinally = tryBlock.getFinallyBlock();
                                if (tempFinally.isPresent()) {
                                    TryStmt tempTry = new TryStmt(tryBlock.getTryBlock(), tryBlock.getCatchClauses(), tryBlock.getFinallyBlock().get());
                                    NodeList<CatchClause> catchClauses = tempTry.getCatchClauses();
                                    if (catchClauses.size() != 0) {
                                        catchClauses.add(new CatchClause(JavaParser.parseParameter("Throwable ex"), new BlockStmt()));
                                        CatchClause lastCatch = catchClauses.get(catchClauses.size() - 1);
                                        if (currentMetadata.contains(InstrumentationClass.Metadata.EXCEPTION))
                                            location.setArg(lastCatch.getParameter().getTypeAsString(), InstrumentationClass.Metadata.EXCEPTION.name());
                                        if (currentMetadata.contains(InstrumentationClass.Metadata.VARIABLE))
                                            location.setArg(lastCatch.getParameter().getNameAsString(), InstrumentationClass.Metadata.VARIABLE.name());
                                        lastCatch.setBody(new BlockStmt(new NodeList<>(JavaParser.parseStatement(location.getMessage()))));
                                    } else {
                                        tempTry.setCatchClauses(new NodeList<>(new CatchClause(JavaParser.parseParameter("Throwable ex"), new BlockStmt())));
                                        CatchClause onlyCatch = tempTry.getCatchClauses().get(0);
                                        if (currentMetadata.contains(InstrumentationClass.Metadata.EXCEPTION))
                                            location.setArg(onlyCatch.getParameter().getTypeAsString(), InstrumentationClass.Metadata.EXCEPTION.name());
                                        if (currentMetadata.contains(InstrumentationClass.Metadata.VARIABLE))
                                            location.setArg(onlyCatch.getParameter().getNameAsString(), InstrumentationClass.Metadata.VARIABLE.name());
                                        onlyCatch.setBody(new BlockStmt(new NodeList<>(JavaParser.parseStatement(location.getMessage()))));
                                    }
                                    entry.replace(tryBlock, tempTry);
                                }
                            } else {
                                NodeList<Statement> newStmts = entry.getStatements();
                                newStmts.removeLast();
                                TryStmt afterTry = new TryStmt(new BlockStmt(new NodeList<>(newStmts)), new NodeList<>(
                                        new CatchClause(JavaParser.parseParameter("Throwable ex"), new BlockStmt())),
                                        new BlockStmt());
                                CatchClause onlyCatch = afterTry.getCatchClauses().get(0);
                                if (currentMetadata.contains(InstrumentationClass.Metadata.EXCEPTION))
                                    location.setArg(onlyCatch.getParameter().getTypeAsString(), InstrumentationClass.Metadata.EXCEPTION.name());
                                if (currentMetadata.contains(InstrumentationClass.Metadata.VARIABLE))
                                    location.setArg(onlyCatch.getParameter().getNameAsString(), InstrumentationClass.Metadata.VARIABLE.name());
                                onlyCatch.setBody(new BlockStmt(new NodeList<>(JavaParser.parseStatement(location.getMessage()))));
                                entry.replace(entry.getStatement(0), afterTry);
                            }
                            break;
                        case AFTER:
                            if (currentMetadata.contains(InstrumentationClass.Metadata.LOCATION))
                                location.setArg("Exiting from", InstrumentationClass.Metadata.LOCATION.name());
                            Optional<TryStmt> currentAfterTry = entry.findFirst(TryStmt.class, tryStmt -> tryStmt.getTryBlock().getStatements().containsAll(entry.getStatements()));
                            if (currentAfterTry.isPresent()) {
                                TryStmt tryBlock = currentAfterTry.get();
                                if (tryBlock.getFinallyBlock().isPresent()) {
                                    BlockStmt finallyBlock = tryBlock.getFinallyBlock().get();
                                    finallyBlock.addStatement(JavaParser.parseStatement(location.getMessage()));
                                    entry.replace(tryBlock.getFinallyBlock().get(), finallyBlock);
                                } else {
                                    TryStmt newTry = new TryStmt(tryBlock.getTryBlock(), tryBlock.getCatchClauses(), new BlockStmt());
                                    newTry.setFinallyBlock(JavaParser.parseBlock(location.getMessage()));
                                    entry.replace(tryBlock, newTry);
                                }
                            } else {
                                NodeList<Statement> newStmts = entry.getStatements();
                                newStmts.removeLast();
                                TryStmt afterTry = new TryStmt(new BlockStmt(new NodeList<>(newStmts)),
                                        new NodeList<>(), new BlockStmt(new NodeList<>(JavaParser.parseStatement(location.getMessage()))));
                                entry.replace(entry.getStatement(0), afterTry);
                            }
                            break;
                    }
                }
            }
            parent.replace(n, replacement);
        }
    }*/
}
