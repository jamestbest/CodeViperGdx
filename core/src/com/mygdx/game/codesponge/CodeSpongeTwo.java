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

            //calculate the new bracket level, used to determine if the line is a method or attribute
            if (code_stripped.contains("{")) {
                bracketLevel++;
            }

            if (code_stripped.contains("}")) {
                bracketLevel--;
            }

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

            System.out.println(shouldEnd + " " + code_line);

            //if the line is a comment it is excluded
            if (shouldEnd){
                continue;
            }

            //reformats the line to remove comments and quotes, this is because when checking for
            //keywords such as class the program can get confused by words in a comment or print
            //statement
            String full_line = remove_comments(code_line);
            full_line = remove_quotes(full_line);

            //returns the type of the line e.g. class, method, attribute
            String type = getType(full_line, bracketLevel);

            //adds the line to the multiline store, this means that if a line goes onto another line
            //it will later be treated as a single line
            multiline_store.append(full_line).append(" ");

            System.out.println(multiline_store);

            if (type.equals("class")){
                ClassInstance cI = create_class(full_line);
                output.add(cI);
                ClassInstance outer_class = current_class;
                current_class = cI;
                if (isInClass){
                    int inner_end_index = getInnerEnd(code_split, i);
                    if (inner_end_index != -1) {
                        String[] inner_code = Arrays.copyOfRange(code_split, i, inner_end_index);
                        System.out.println("I have recursed");
                        output.addAll(fragment_class(inner_code));
                        current_class = outer_class;
                        i = inner_end_index;
                    }
                }
                isInClass = true;
            }

            if (!type.equals("multiline")){
                multiline_store = new StringBuilder();
            }
        }
        return output;
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
        System.out.println(line);
        String[] line_split = line.split(" ");

        String access_level = "an access_level_find error has occurred";
        ArrayList<String> modifiers = new ArrayList<>();
        String name = "a find_name error has occurred";
        String extended_class = "";
        ArrayList<String> implemented_interfaces = new ArrayList<>();

        boolean foundExtends = false;
        boolean foundImplements = false;

        for (String word : line_split) {
            if (foundExtends){
                foundExtends = false;
                extended_class = name;
            }else if (foundImplements){
                implemented_interfaces.add(name);
            }

            if (word.contains("{") || word.equals("class")){
                break;
            } else if (ClassAccessLevels.contains(word)){
                access_level = word;
            } else if (ClassModifiers.contains(word)){
                modifiers.add(word);
            } else if (word.equals("extends")){
                foundExtends = true;
            } else if (word.equals("implements")){
                foundImplements = true;
            } else{
                name = word.replace("{", "");
            }
        }
        
        ClassInstance classInstance = new ClassInstance(name, access_level, modifiers,
                extended_class, implemented_interfaces);

        System.out.println("THis is the line without comments:  " + line);

        for (String word : line.split(" ")) {
            if (word.equals("{")){
                break;
            }
        }
        return null;
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
        System.out.println("This is the line without quotes: " + line);
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

        return "multiline";
    }
}