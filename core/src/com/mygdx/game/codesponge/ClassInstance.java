package com.mygdx.game.codesponge;

import java.util.ArrayList;
import java.util.Arrays;

public class ClassInstance extends Instance implements Comparable<ClassInstance>, Cloneable {
    public ArrayList<MethodInstance> methods;
    public ArrayList<VariableInstance> variables;

    private String extendedClass;
    private ArrayList<String> implementedInterfaces;
    private String innerTo;

    public ArrayList<ImportInstance> imports;

    public ClassInstance(String name, String accessLevel, ArrayList<String> modifiers, String extendedClass, ArrayList<String> implementedInterfaces) {
        this.name = name;
        this.accessLevel = accessLevel;
        this.modifiers = modifiers;
        this.extendedClass = extendedClass;
        this.implementedInterfaces = implementedInterfaces;
        setup();
    }

    public void setup(){
        methods = new ArrayList<>();
        variables = new ArrayList<>();
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<ImportInstance> getImports() {
        return imports;
    }

    public void setImports(ArrayList<ImportInstance> imports) {
        this.imports = imports;
    }

    public String toString(){
        String s = "";
        s += "Class: " + name + "\n";
        s += "Access Level: " + accessLevel + "\n";
        s += "Modifiers: " + modifiers + "\n";
        s += "Variables: " + Arrays.toString(variables.toArray()) + "\n";
        s += "Methods: " + methods.toString() + "\n";
        s += "Imports: " + imports.toString() + "\n";
        return s;
    }

    public String getExtendedClass() {
        return extendedClass;
    }

    public void setExtendedClass(String extendedClass) {
        this.extendedClass = extendedClass;
    }

    public String getInnerTo() {
        return innerTo;
    }

    public void setInnerTo(String innerTo) {
        this.innerTo = innerTo;
    }

    public ArrayList<String> getImplementedInterfaces() {
        return implementedInterfaces;
    }

    public void setImplementedInterfaces(ArrayList<String> implementedInterfaces) {
        this.implementedInterfaces = implementedInterfaces;
    }

    @Override
    public int compareTo(ClassInstance o) {
        return 0;
    }

    @Override
    public ClassInstance clone() {
        try {
            ClassInstance clone = (ClassInstance) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
