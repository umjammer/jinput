/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.java.games.input.plugin.hori;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;


/**
 * One of the four profiles of a HORI Fighting Commander OCTA: its name, what each button sends
 * and which stick the analog stick is.
 * <p>
 * This is what the key assigner edits. It knows nothing about bytes, see {@link HoriProfileMemory}
 * for that.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-10-07 nsano initial version <br>
 */
public class HoriProfile {

    /** the controller keeps 16 utf-16 chars */
    public static final int MAX_NAME_LENGTH = 16;

    private String name = "";
    private final Map<HoriButton, HoriFunction> functions = new EnumMap<>(HoriButton.class);
    private HoriStick stick = HoriStick.LS;

    /** every button sends itself */
    public HoriProfile() {
        resetButtons();
    }

    public HoriProfile(HoriProfile other) {
        this.name = other.name;
        this.functions.putAll(other.functions);
        this.stick = other.stick;
    }

    public String getName() {
        return name;
    }

    /** @throws IllegalArgumentException longer than {@link #MAX_NAME_LENGTH} */
    public void setName(String name) {
        Objects.requireNonNull(name);
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("name too long (must be at most " + MAX_NAME_LENGTH + " characters): " + name);
        }
        this.name = name;
    }

    public HoriFunction getFunction(HoriButton button) {
        return functions.get(button);
    }

    public void setFunction(HoriButton button, HoriFunction function) {
        functions.put(button, Objects.requireNonNull(function));
    }

    /** @return true when the button sends something else than itself */
    public boolean isRemapped(HoriButton button) {
        return functions.get(button) != button.defaultFunction();
    }

    /** every button back to sending itself, as hori.py's {@code map <profile> default} */
    public void resetButtons() {
        for (HoriButton b : HoriButton.values()) {
            functions.put(b, b.defaultFunction());
        }
    }

    public HoriStick getStick() {
        return stick;
    }

    public void setStick(HoriStick stick) {
        this.stick = Objects.requireNonNull(stick);
    }

    /** for saving to a file, keys are {@code name}, {@code stick} and {@code button.<button>} */
    public Properties toProperties() {
        Properties props = new Properties();
        props.setProperty("name", name);
        props.setProperty("stick", stick.name());
        for (HoriButton b : HoriButton.values()) {
            props.setProperty("button." + b.name(), functions.get(b).label);
        }
        return props;
    }

    /** missing keys are left as they are in a fresh profile */
    public static HoriProfile fromProperties(Properties props) {
        HoriProfile profile = new HoriProfile();
        profile.setName(props.getProperty("name", ""));
        profile.setStick(HoriStick.valueOf(props.getProperty("stick", HoriStick.LS.name()).toUpperCase()));
        for (HoriButton b : HoriButton.values()) {
            String f = props.getProperty("button." + b.name());
            if (f != null) profile.setFunction(b, HoriFunction.parse(f));
        }
        return profile;
    }

    /**
     * The same setting as hori.py command lines, so it can be written with that on a machine
     * where it works.
     *
     * @param profile 1..4
     */
    public List<String> toHoriPyCommands(int profile) {
        List<String> commands = new ArrayList<>();
        commands.add("hori.py name " + profile + " '" + name.replace("'", "'\\''") + "'");
        StringBuilder map = new StringBuilder("hori.py map " + profile + " default");
        for (HoriButton b : HoriButton.values()) {
            if (isRemapped(b)) map.append(' ').append(b.xboxLabel).append('=').append(functions.get(b).label);
        }
        commands.add(map.toString());
        commands.add("hori.py stick " + profile + " " + stick.name());
        return commands;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HoriProfile that)) return false;
        return name.equals(that.name) && functions.equals(that.functions) && stick == that.stick;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, functions, stick);
    }

    @Override
    public String toString() {
        return "HoriProfile{name=" + name + ", stick=" + stick + ", functions=" + functions + "}";
    }
}
