package com.example.learncolor;

public class ColorChoice {
    private final String name;
    private final int color;

    public ColorChoice(String name, int color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }
}
