package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DBPropertyUtil {

    public static String getConnectionString(String propertyFileName) {
        Properties properties = new Properties();
        String connectionString = null;

        try (FileInputStream fis = new FileInputStream("resources/" + propertyFileName)) {
            properties.load(fis);

            String url = properties.getProperty("db.url");
            String username = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");

            connectionString = "url=" + url + ";username=" + username + ";password=" + password;

        } catch (IOException e) {
            System.err.println("Error loading properties file: " + e.getMessage());
        }

        return connectionString;
    }
}
