package ru.practicum.shareit.testutil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Utils {

    public static String loadJsonFromFile(String filePath) {
        try (InputStream is = ClassLoader.getSystemResourceAsStream(filePath)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error reading contents of file " + filePath);
        }
    }
}
