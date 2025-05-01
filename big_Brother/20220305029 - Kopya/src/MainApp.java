/*
 * MainApp.java
 * - Programı başlatır ve veritabanından kullanıcıları yükleyerek oturum açma penceresini başlatır
 */
import javax.swing.SwingUtilities;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MainApp {
    public static void main(String[] args) {
        // Dinamik kullanıcı listesi
        List<Account> users = new ArrayList<>();
        AttendanceManager mgr = new AttendanceManager();

        // Veritabanından personel kullanıcıları yükle
        try (Connection c = DatabaseConnector.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT `ıd`, `name`, `password` FROM personnel"
             )) {
            while (rs.next()) {
                users.add(new Staff(
                        rs.getInt("ıd"),
                        rs.getString("name"),
                        rs.getString("password")
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Veritabanından yönetici kullanıcıları yükle
        try (Connection c = DatabaseConnector.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT `ıd`, `name`, `password` FROM manager"
             )) {
            while (rs.next()) {
                users.add(new Administrator(
                        rs.getInt("ıd"),
                        rs.getString("name"),
                        rs.getString("password")
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Oturum açma penceresini başlat
        SwingUtilities.invokeLater(() -> new AuthWindow(users, mgr));
    }
}
