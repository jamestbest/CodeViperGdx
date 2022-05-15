package com.mygdx.game.codesponge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodeSpongeTwo {
    private static ClassInstance current_class;

    static List<String> ClassAccessLevels = List.of(new String[]{"public", "default"});
    static List<String> ClassModifiers = List.of(new String[]{"final", "abstract", "static"});
    //AMC = (A)ttribute, (M)ethods, and (C)onstructors
    static List<String> AMCAccessLevels = List.of(new String[]{"public", "private", "protected", "default"});
    static List<String> AMCModifiers = List.of(new String[]{"static", "final", "abstract", "transient", "synchronized", "volatile"});


    public static ArrayList<ClassInstance> fragment_class(String code){
        return fragment_class(code.split("\n"));
    }

    public static ArrayList<ClassInstance> fragment_class(String[] code_split) {
        ArrayList<ClassInstance> output = new ArrayList<>();

        int bracketLevel = 0;
        boolean isInComment = false;

        StringBuilder multiline_store = new StringBuilder();

        boolean isInClass = false;

        for (int i = 0; i < code_split.length; i++) {
            String code_line = code_split[i];
            String code_stripped = code_line.strip();

            //determines if the line is a comment, and if so continues to the next line
            boolean shouldEnd = false;
            if (code_stripped.startsWith("/*")) {
                isInComment = true;
            }

            //if the current line is within a comment e.g. // or /* then it should be excluded
            if (isInComment || code_stripped.startsWith("//")){
                shouldEnd = true;
            }

            //this means that if you use /* and *\ on the same line then the comment will be excluded
            //and the next line will not be considered 'isInComment'
            if (code_stripped.endsWith("*/")) {
                isInComment = false;
            }

            //if the line is a comment it is excluded
            if (shouldEnd){
                continue;
            }

            //reformats the line to remove comments and quotes, this is because when checking for
            //keywords such as class the program can get confused by words in a comment or print
            //statement
            //quotes are removed first as if there is an if(x.contains("//")) like in this program then it will be picked
            //up as a comment and not a quote first, removing the rest of the line and preventing things like { from being
            //detected
            String full_line = remove_quotes(code_stripped);
            full_line = remove_comments(full_line);

            //returns the type of the line e.g. class, method, attribute
            String type = getType(full_line, bracketLevel);

            //calculate the new bracket level, used to determine if the line is a method or attribute
            if (full_line.contains("{")) {
                bracketLevel++;
            }

            if (full_line.contains("}")) {
                bracketLevel--;
            }

            //adds the line to the multiline store, this means that if a line goes onto another line
            //it will later be treated as a single line
            multiline_store.append(full_line).append(" ");

            if (type.equals("class")){
                if (!isInClass) {
                    ClassInstance cI = create_class(full_line);
                    cI.setImports(getImportList(code_split));
                    output.add(cI);
                    current_class = cI;
                }
                ClassInstance outer_class = current_class;
                if (isInClass) {
                    int inner_end_index = getInnerEnd(code_split, i);
                    if (inner_end_index != -1) {
                        String[] inner_code = Arrays.copyOfRange(code_split, i, inner_end_index);
                        System.out.println("I have recursed");
                        ArrayList<ClassInstance> inner_classes = fragment_class(inner_code);
                        for (ClassInstance inner_class : inner_classes) {
                            inner_class.setInnerTo(outer_class.getName());
                        }
                        output.addAll(inner_classes);
                        current_class = outer_class;
                        i = inner_end_index;
                    }
                }
                isInClass = true;
            } else if (type.equals("method")) {
                try {
                    MethodInstance mI = create_method(full_line);
                    if (mI.isConstructor()){
                        current_class.number_of_constructors++;
                    }
                    current_class.methods.add(mI);
                } catch (Exception e) {
                    System.out.println("Error in method: " + full_line);
                }
            } else if (type.equals("attribute")) {
                try {
                    current_class.variables.add(create_variable(full_line));
                }catch (Exception e){
                    System.out.println("Error in attribute: " + full_line);
                }
            }
            if (!type.equals("multiline")){
                multiline_store = new StringBuilder();
            }
        }
        return output;
    }

    public static MethodInstance create_method(String line) throws Exception {
        ArrayList<String> modifiers = new ArrayList<>();
        String access_level = "";
        String return_type = "error finding return type";
        String name = "error finding method name";

        String[] line_parts = line.split("\\(");

        String[] main_part = line_parts[0].strip().split(" ");

        boolean has_return = false;
        for (String part : main_part){
            if (AMCModifiers.contains(part)){
                modifiers.add(part);
            } else if (AMCAccessLevels.contains(part)){
                access_level = part;
            } else {
                if (!has_return){
                    return_type = part;
                    has_return = true;
                }else{
                    name = part;
                }
            }
        }

        ArrayList<String> exceptions = new ArrayList<>();
        ArrayList<VariableInstance> parameters = new ArrayList<>();

        String right = line_parts[1];

        String[] right_parts = right.split("\\)");
        if (right_parts.length != 2){
            throw new Exception("This is not a method");
        }

        if (name.equals("createClassMethodsTableTemplate")){
            System.out.println("");
        }

        String[] parameters_part = right_parts[0].split(",");

        String exceptions_part = right_parts[1];
        if (exceptions_part.contains("throws")){
            exceptions_part = exceptions_part.replace("throws", "");
            String[] exceptions_ = exceptions_part.split(",");

            if (exceptions_.length > 0){
                for (String exception : exceptions_){
                    exceptions.add(exception.replace("{", "").strip());
                }
            }
        }

        for (String part : parameters_part) {
            if (part.equals("")){
                continue;
            }
            parameters.add(create_variable(part));
        }

        boolean is_constructor = false;

        if (name.equals("error finding method name") && !return_type.equals("error finding return type")){
            name = return_type;
            is_constructor = true;
        }

        return new MethodInstance(access_level, return_type, name,
                parameters, modifiers, exceptions, is_constructor);
    }

    public static VariableInstance create_variable(String line){
        String type = "error finding variable type";
        String name = "error finding variable name";
        String access_level = null;
        ArrayList<String> modifiers = new ArrayList<>();

        String[] variable_parts = line.split(" ");

        boolean found_type = false;
        for (String part : variable_parts){
            String clean_part = part.replace(";", "");

            if (AMCModifiers.contains(clean_part)){
                modifiers.add(clean_part);
            } else if (AMCAccessLevels.contains(clean_part)){
                access_level = clean_part;
            }
            else {
                if (!found_type){
                    type = clean_part;
                    found_type = true;
                }else {
                    name = clean_part;
                    break;
                }
            }
        }
        return new VariableInstance(type, name, access_level, modifiers);
    }

    public static int getInnerEnd(String[] code_split, int start_index){
        int bracketLevel = 0;
        for (int i = start_index; i < code_split.length; i++) {
            String code_line = code_split[i];
            if (code_line.contains("{")) {
                bracketLevel++;
            }if (code_line.contains("}")) {
                bracketLevel--;
            }

            if (bracketLevel == 0){
                return i;
            }
        }
        return -1;
    }

    public static ClassInstance create_class(String line){
        String[] line_split = line.split(" ");

        String access_level = "an access_level_find error has occurred";
        ArrayList<String> modifiers = new ArrayList<>();
        String name = "a find_name error has occurred";
        String extended_class = null;
        ArrayList<String> implemented_interfaces = new ArrayList<>();

        boolean foundExtends = false;
        boolean foundImplements = false;

        for (String word : line_split) {
            String clean_word = word.replace("{", "");

            if (foundExtends){
                foundExtends = false;
                extended_class = clean_word;
            }else if (foundImplements){
                implemented_interfaces.add(clean_word.replace(",", ""));
            } else if (clean_word.equals("class")){
            } else if (ClassAccessLevels.contains(clean_word)){
                access_level = clean_word;
            } else if (ClassModifiers.contains(clean_word)){
                modifiers.add(clean_word);
            } else if (clean_word.equals("extends")){
                foundExtends = true;
            } else if (clean_word.equals("implements")){
                foundImplements = true;
            } else{
                if (!word.equals("{")) {
                    name = clean_word;
                }
            }

            if (word.contains("{")){
                break;
            }
        }
        
        ClassInstance classInstance = new ClassInstance(name, access_level, modifiers,
                extended_class, implemented_interfaces);

        return classInstance;
    }

    public static String remove_quotes(String line){
        //this was originally designed to remove the quotes one by one
        //however it now just finds the first and last appearance of a quote
        //and removes everything between them
        //I don't know if this will cause problems later on
        //it was used to remove anything inside a sout
        int start_index = line.indexOf("\"");
        while (start_index != -1){
            line = line.replaceFirst("\"", "");
            int end_index = line.lastIndexOf("\"");
            if (end_index != -1){
                String f_half = line.substring(0, start_index);
                String s_half = line.substring(end_index + 1);
                line = f_half + s_half;
            }
            start_index = line.indexOf("\"");
        }
        return line;
    }

    public static String remove_comments(String line){
        //removes comments from a line, whether they be at the beginning, middle or end
        String[] possible_comment_starts = {"/**", "/*"};
        String[] possible_comment_ends = {"**/", "*/"};

        if (line.contains("//")){
            int start_index = line.indexOf("//");
            line = line.substring(0, start_index);
        }

        //removes every comment within a line and replaces it with ""
        int start_index = line.indexOf("/*");
        while (start_index != -1) {
            int end_index = line.indexOf("*/");
            if (end_index != -1) {
                line = line.substring(0, start_index) + line.substring(end_index + 2);
                //2 is the length of the string */
            }else{
                return line;
            }
            start_index = line.indexOf("/*");
        }
        return line;
    }

    public static String getType(String line, int bracketLevel){
        if (line.contains(" class ")){
            return "class";
        }

        if (line.endsWith(";")){
            if (bracketLevel == 1){
                return "attribute";
            }else{
                return "attribute_in_method";
            }
        }

        if (line.endsWith("{")){
            if (bracketLevel == 1) {
                return "method";
            }else{
                return "statement_in_method";
            }
        }

        if (line.endsWith("}")){
            return "bracket_close";
        }

        return "multiline";
    }

    public static ArrayList<ImportInstance> getImportList(String[] codeLines) {
        ArrayList<ImportInstance> imports = new ArrayList<>();
        for (String line : codeLines) {
            if (line.startsWith("import")) {
                String lineWithoutImport = line.replace("import", "").strip();
                ImportInstance II = new ImportInstance(lineWithoutImport.split("\\."));
                imports.add(II);
            }
        }
        return imports;
    }
}
