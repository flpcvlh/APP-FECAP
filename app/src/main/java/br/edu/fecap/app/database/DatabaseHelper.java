package br.edu.fecap.app.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHelper {
    private static final String HOST = "fecap-app-db.mysql.database.azure.com";
    private static final String DATABASE = "fecapapp";
    private static final String USER = "luizfelipefecap17";
    private static final String PASSWORD = "Fecap@2010";
    private static final String URL = "jdbc:mysql://" + HOST + ":3306/" + DATABASE +
            "?useSSL=true&verifyServerCertificate=false&requireSSL=true";

    private static DatabaseHelper instance;

    // Singleton para evitar múltiplas instâncias
    public static synchronized DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    // Método modificado para forçar o modo offline
    public Connection getConnection() throws SQLException {
        // Lançar uma exceção específica para identificar o modo offline
        throw new SQLException("MODO_OFFLINE");

        /* Código original - mantido para referência futura
        try {
            // Carrega o driver JDBC
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL não encontrado", e);
        }
        */
    }

    // Método para fechar conexão - mantido para compatibilidade
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}