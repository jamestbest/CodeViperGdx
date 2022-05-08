package com.mygdx.game.codesponge;

import java.util.Arrays;

public class ImportInstance {
    private String[] buildPath;

    public ImportInstance(String[] buildPath) {
        this.buildPath = buildPath;
    }

    public String[] getBuildPath() {
        return buildPath;
    }

    public void setBuildPath(String[] buildPath) {
        this.buildPath = buildPath;
    }

    public String toString(){
        return Arrays.toString(buildPath);
    }
}
