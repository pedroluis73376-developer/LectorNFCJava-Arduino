/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arras3888;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class ConexionMySQL {

    // Librería de MySQL
    public String driver = "com.mysql.jdbc.Driver";

    // Nombre de la base de datos
    public String database = "arras";

    // Host
    public String hostname = "localhost";

    // Puerto
    public String port = "3306";
    
    // Ruta de nuestra base de datos (desactivamos el uso de SSL con "?useSSL=false")
    public String url = "jdbc:mysql://" + hostname + "/"  + database + "?";

    // Nombre de usuario
    public String username = "user=root&";

    // Clave de usuario
    public String password = "password=";

    public Connection conectarMySQL() {
        Connection conn = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url + username + password);
            if(conn!=null){
           // Main.consola.append("\nConexion con la base de datos Establecida");
            
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Se ha perdido la conexion con la base de datos", "Error de Conexion", JOptionPane.WARNING_MESSAGE);
        }
        
        return conn;
        
    }

}