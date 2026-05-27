package ec.edu.utpl.computacion.proava.mundialsimulador.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;

public class ConnectionManager {

    private static final Properties properties = new Properties();

    static {
        try (
            InputStream input = ConnectionManager.class
                .getClassLoader()
                .getResourceAsStream("database.properties")
        ) {
            if (input == null) {
                throw new IllegalStateException(
                    "No se encontró database.properties en el classpath"
                );
            }
            properties.load(input);
            Class.forName(properties.getProperty("db.driver"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ConnectionManager() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            properties.getProperty("db.url"),
            properties.getProperty("db.user"),
            properties.getProperty("db.password")
        );
    }
}
