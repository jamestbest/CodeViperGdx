package com.mygdx.game.codesponge;

import java.util.ArrayList;
import java.util.Arrays;

public class MethodInstance extends Instance{
    private String returnType;
    private ArrayList<VariableInstance> parameters;

    private ArrayList<String> exceptions;

    private boolean isConstructor;

    public MethodInstance(String accessLevel, String returnType, String name,
                          ArrayList<VariableInstance> parameters, ArrayList<String> modifiers,
                          ArrayList<String> exceptions, boolean isConstructor) {
        this.accessLevel = accessLevel;
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.modifiers = modifiers;
        this.exceptions = exceptions;
        this.isConstructor = isConstructor;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getExceptions() {
        return exceptions;
    }

    public void setExceptions(ArrayList<String> exceptions) {
        this.exceptions = exceptions;
    }

    public ArrayList<VariableInstance> getParameters() {
        return parameters;
    }

    public void setParameters(ArrayList<VariableInstance> parameters) {
        this.parameters = parameters;
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public void setConstructor(boolean constructor) {
        isConstructor = constructor;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        String add = isConstructor ? "ConstructorInstance " : "MethodInstance ";

        sb.append("\n").append(add);
        sb.append(accessLevel);
        sb.append(" ");
        sb.append(returnType);
        sb.append(" ");
        sb.append(name);
        for (int i = 0; i < parameters.size(); i++) {
            sb.append(parameters.get(i));
            if (i < parameters.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(" ");
        sb.append(exceptions.toString());
        return sb.toString();
    }
}
