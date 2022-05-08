package com.mygdx.game.codesponge;

import java.util.ArrayList;
import java.util.Arrays;

public class VariableInstance extends Instance{
    private String type;

    public VariableInstance(String type, String name, ArrayList<String> modifiers) {
        this.type = type;
        this.name = name;
        this.modifiers = modifiers;
    }

    public VariableInstance(String type, String name, String accessLevel, ArrayList<String> modifiers) {
        this.type = type;
        this.name = name;
        this.accessLevel = accessLevel;
        this.modifiers = modifiers;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return "\n VariableInstance: " +
                accessLevel +
                " " +
                type +
                " " +
                name +
                " " +
                Arrays.toString(modifiers.toArray());
    }
}
