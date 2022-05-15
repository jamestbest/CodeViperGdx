package com.mygdx.game.codesponge;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.mygdx.game.DocsQuickstart;
import com.mygdx.game.MainScreen;
import com.sun.tools.javac.Main;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodeSponge {

    //rewrite to add a classes information to the google doc and then
    //any inner classes are extracted and added to a list

    static List<String> classAccessStructure = List.of(new String[]{"public", "default"});
    static List<String> classNonAccessStructure = List.of(new String[]{"final", "abstract", "static"});
    //AMC = (A)ttribute, (M)ethods, and (C)onstructors
    static List<String> AMCAccessStructure = List.of(new String[]{"public", "private", "protected", "default"});
    static List<String> AMCNonAccessStructure = List.of(new String[]{"static", "final", "abstract", "transient", "synchronized", "volatile"});

    public CodeSponge() {

    }

    static ArrayList<ClassInstance> classes = new ArrayList<>();

    static ArrayList<ImportInstance> imports = new ArrayList<>();

    static boolean isInMethod = false;
    static int bracketLevel = 0;

    static int classCount = -1;

    static boolean isInner = false;

    public static String fragmentCode(String code, Settings settings, MainScreen mainScreen) {
        String[] lines = code.split("\n");
        return fragmentCode(lines, settings, mainScreen);
    }

    public static String fragmentCode(String[] lines, Settings settings, MainScreen mainScreen) {
        StringBuilder multilineStore = new StringBuilder();

        String innerTo = "";

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String stripped = line.strip();

            if (stripped.equals(".setIndex(table.getStartIndex()).setSegmentId(\"\")).setRowIndex(rowLocation).setColumnIndex(columnLocation)")){
                System.out.println("");
            }

            stripped = removeQuotes(stripped);

            if (stripped.equals("public static class test{")) {
                System.out.println("");
            }

            String type = getType(stripped);

            String lineTot = stripped + " ";

            if (!type.equals("multiline")) {
                lineTot = multilineStore.append(stripped).toString();
            }

//            System.out.println("\n line Tot: " + lineTot);
            switch (type) {
                case "class" -> {
                    System.out.println(" class " +  lineTot);
                    ClassInstance CI = (getClassInstance(lineTot));
                    CI.setImports(getImportList(lines));

                    if (isInner){
                        CI.setInnerTo(innerTo);
                    }else{
                        innerTo = CI.getName();
                    }

                    bracketLevel++;

                    classes.add(CI);
                    classCount++;
                }
                case "variable" -> {
                    System.out.println(" variable " +  lineTot);
                    VariableInstance VI = getVariableInstance(lineTot);
                    classes.get(classCount).variables.add(VI);
                }
                case "method" -> {
                    System.out.println(" method " +  lineTot);
                    isInMethod = true;
                    bracketLevel++;
                    if (classCount == 1){
                        System.out.println("");
                    }
                    MethodInstance MI = getMethodInstance(lineTot);
                    classes.get(classCount).methods.add(MI);
                }
                case "multiline" -> {
                    System.out.println(" multiline " +  lineTot);
                    multilineStore.append(lineTot);
                }
                default -> System.out.println(bracketLevel + " " + type + "  : " +  lineTot);
            }
            
            if (!type.equals("multiline")) {
                multilineStore = new StringBuilder();
            }
        }

        mainScreen.updateCurrentAction("creating document");
//        try {
//            return DocsQuickstart.createFullDoc(classes, settings);
//        } catch (IOException | GeneralSecurityException e) {
//            e.printStackTrace();
//            mainScreen.updateCurrentAction("error whilst creating document");
//        }
        return "";
    }

    public static String[] findInnerStrings(int endIndex, String[] lines){
        if (endIndex == -1){
            return new String[0];
        }

        return Arrays.copyOfRange(lines, 0, endIndex);
    }

    public static int findInnerEndIndex(String[] lines){
        int bracketLevel = 0;
        int endIndex = -1;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String stripped = line.strip();

            if (stripped.contains("{")){
                bracketLevel++;
            }
            if (stripped.contains("}")){
                bracketLevel--;
            }

            if (bracketLevel == 0){
                endIndex = i;
            }
        }

        return endIndex;
    }

    public static String removeQuotes(String line){
        int quoteStart = line.indexOf("\"");
        int quoteEnd = line.lastIndexOf("\"");

        if (quoteStart != -1 && quoteEnd != -1){
            return line.substring(0, quoteStart) +
                    line.substring(quoteEnd + 1);
        }
        return line;
    }

    public static ArrayList<ImportInstance> getImportList(String[] codeLines) {
        for (String line : codeLines) {
            if (line.startsWith("import")) {
                String lineWithoutImport = line.replace("import", "").strip();
                ImportInstance II = new ImportInstance(lineWithoutImport.split("\\."));
                imports.add(II);
            }
        }
        return imports;
    }

    public static VariableInstance getVariableInstance(String line) {
        String[] parts = line.split(" ");
        String accessLevel = "";
        ArrayList<String> modifiers = new ArrayList<>();
        String name = "none";
        String type = "null";

        boolean gotType = false;

        for (String part : parts) {
            if (part.equals("=")){
                break;
            }

            if (gotType) {
                name = part;
                continue;
            }

            if (AMCAccessStructure.contains(part)) {
                accessLevel = part;
            }
            else if (AMCNonAccessStructure.contains(part)) {
                modifiers.add(part);
            }
            else {
                type = part;
                gotType = true;
            }
        }
        if (name.equals("null")) {
            return null;
        }
        return new VariableInstance(type, name, accessLevel, modifiers);
    }

    public static MethodInstance getMethodInstance(String line) {
        String[] mainParts = line.split("\\(");

        String[] parts = mainParts[0].split(" ");

        boolean isConstructor = parts.length <= 2;

        String[] paramParts = mainParts[1].split("\\)");

        String accessLevel = "null";
        ArrayList<String> modifiers = new ArrayList<>();
        String name = "null";
        String returnType = "none";
        ArrayList<VariableInstance> parameters = new ArrayList<>();

        ArrayList<String> exceptions = new ArrayList<>();

        boolean gotReturnType = false;

        for (String part : parts) {
            if (part.equals("{") || part.equals(")")) {
                break;
            }

            if (gotReturnType) {
                name = part.replace("(", "");

                System.out.println(Arrays.toString(paramParts));

                parameters = getParameters(paramParts[0]);

                if (paramParts.length > 1) {
                    exceptions.add(paramParts[1].strip().replace("throws", "").replace("{", ""));
                }
                continue;
            }

            if (AMCAccessStructure.contains(part)) {
                accessLevel = part;
            }
            else if (AMCNonAccessStructure.contains(part)) {
                modifiers.add(part);
            }
            else {
                if (isConstructor){
                    name = part;
                    break;
                }
                returnType = part;
                gotReturnType = true;
            }
        }

        if (classes.get(classCount).getName().equals(name)){
            isConstructor = true;
        }

        return new MethodInstance(accessLevel, returnType, name, parameters, modifiers, exceptions, isConstructor);
    }

    public static ArrayList<VariableInstance> getParameters(String line) {
        ArrayList<VariableInstance> parameters = new ArrayList<>();
        String[] parts = line.split(",");
        for (String part : parts) {
            parameters.add(getVariableInstance(part.strip()));
        }
        return parameters;
    }

    public static ClassInstance getClassInstance(String line) {
        String[] parts = line.split(" ");

        String accessLevel = "default";
        ArrayList<String> modifiers = new ArrayList<>();
        String name = "null";

        boolean inExtends = false;
        String extendsClass = null;

        boolean inImplements = false;
        ArrayList<String> implementsClasses = new ArrayList<>();

        for (String part : parts) {
            if (part.equals("{")) {
                break;
            }

            if (part.equals("class")){
                continue;
            }

            if (part.equals("extends")){
                inExtends = true;
                continue;
            }

            if (inExtends){
                extendsClass = part.replace("{", "");
                inExtends = false;
                continue;
            }

            if (part.equals("implements")){
                inImplements = true;
                continue;
            }

            if (inImplements){
                implementsClasses.add(part.replace(",", ""));
            }

            if (classAccessStructure.contains(part)) {
                accessLevel = part;
            }
            else if (classNonAccessStructure.contains(part)) {
                modifiers.add(part);
            }
            else {
                if (name.equals("null")) {
                    name = part.replace("{", "");
                }
            }
        }

        return new ClassInstance(name, accessLevel, modifiers, extendsClass, implementsClasses);
    }

    public static ArrayList<Integer> getIndexOfAllNonAccessStructures(List<String> possibleStructures, String line) {
        List<String> lineSplitList = List.of(line.strip().split(" "));
        ArrayList<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < possibleStructures.size(); i++) {
            String possibleStructure = possibleStructures.get(i);
            if (lineSplitList.contains(possibleStructure)) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    public static int getIndexOfAny(List<String> possible, List<String> lineSplit){
        for (int i = 0; i < possible.size(); i++) {
            if (lineSplit.contains(possible.get(i))) {
                return i;
            }
        }

        return -1;
    }

    public static String getType(String line) {
        if (line.equals("} else {")){
            System.out.println("else");
        }

        if (isInMethod){
            if (bracketLevel == 1 && line.contains("class ")){
                isInner = true;
                return "class";
            }

            if (line.contains("{") && line.contains("}")){
                return "bracketDuplex";
            }

            if (line.contains("{")){
                bracketLevel++;
                return "statement";
            }
            if (line.contains("}")){
                bracketLevel--;
                if (bracketLevel == 1){
                    isInMethod = false;
                    return "methodEnd";
                }
                return "statementEnd";
            }
            if (bracketLevel > 1){
                return "inStatement";
            }
            return "inMethod";
        }
        if (line.startsWith("//") || line.startsWith("/*") || line.startsWith("*/") || line.startsWith("/**") || line.startsWith("*")){
            return "comment";
        }
        else if (line.startsWith("@")){
            return "annotation";
        }
        else if (line.startsWith("package ")) {
            return "package";
        }
        else if (line.startsWith("import ")) {
            return "import";
        }
        else if (line.contains(" class ")) {
            if (bracketLevel != 0){
                isInner = true;
            }
            return "class";
        }
        else if (line.endsWith(";")){
            return "variable";
        }
        else if (line.contains("try")){
            return "try";
        }
        else if (line.contains("{")) {
            return "method";
        }
        else if (line.equals("")){
            return "empty";
        }
        else {
            return "multiline";
        }
    }

    public static void printClasses() {
        for (ClassInstance CI : classes) {
            System.out.println(CI.toString());
        }
    }

    public static class Settings{
        private boolean showConstructors;
        private boolean showExceptions;

        public Settings(boolean showConstructors, boolean showExceptions){
            this.showConstructors = showConstructors;
            this.showExceptions = showExceptions;
        }

        public Settings(){
            this.showConstructors = false;
            this.showExceptions = false;
        }

        public boolean isShowConstructors() {
            return showConstructors;
        }

        public void setShowConstructors(boolean showConstructors) {
            this.showConstructors = showConstructors;
        }

        public boolean isShowExceptions() {
            return showExceptions;
        }

        public void setShowExceptions(boolean showExceptions) {
            this.showExceptions = showExceptions;
        }
    }
}
