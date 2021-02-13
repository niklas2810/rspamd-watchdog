package com.niklasarndt.rspamwatchdog.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;

public class BuildConstants {

    public static String NAME;
    public static String DESCRIPTION;
    public static String VERSION;
    public static String TARGET_JDK;
    public static String TIMESTAMP;
    public static String URL;

    static {
        try {
            Properties properties = new Properties();
            properties.load(BuildConstants.class.getClassLoader()
                    .getResourceAsStream("build.properties"));

            NAME = properties.getProperty("build.name");
            DESCRIPTION = properties.getProperty("build.description");
            VERSION = properties.getProperty("build.version");
            TARGET_JDK = properties.getProperty("build.targetJdk");
            TIMESTAMP = properties.getProperty("build.timestamp");
            URL = properties.getProperty("build.url");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Set null fields to UNKNOWN
        for (Field field : BuildConstants.class.getDeclaredFields()) {
            try {
                if (field.getType().isAssignableFrom(String.class)
                        && field.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC)
                        && field.get(null) == null) {
                    field.set(null, "UNKNOWN");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private BuildConstants() {
    }
}