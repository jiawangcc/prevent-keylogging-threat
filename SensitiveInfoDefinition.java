package com.example.jia.css539;

public class SensitiveInfoDefinition {

    private final String prefix;
    private final int length;

    public SensitiveInfoDefinition(String prefix, int length) {
        this.prefix = prefix;
        this.length = length;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getLength() {

        return length;
    }
}
