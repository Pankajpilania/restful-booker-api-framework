package config;

import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("CRITICAL: application.properties file not found in src/main/resources/");
            }
            properties.load(input);
        } catch (Exception e) {
            throw new RuntimeException("CRITICAL: Failed to load application.properties file", e);
        }
    }

    public static String getBaseUrl() {
        return getPropertyOrThrow("base.url");
    }

    public static String getUsername() {
        return getPropertyOrThrow("auth.username");
    }

    public static String getPassword() {
        return getPropertyOrThrow("auth.password");
    }

    // Helper method to ensure we never pass 'null' to our API clients
    private static String getPropertyOrThrow(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("CRITICAL: Property '" + key + "' is missing or empty in application.properties");
        }
        return value;
    }
}
