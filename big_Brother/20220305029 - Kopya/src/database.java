import java.sql.*;

public class database {
        // 1) Bağlantı ayarları
        private static final String URL =
                "jdbc:mysql://127.0.0.1:3306/20220305029"
                        + "?useSSL=false"
                        + "&serverTimezone=UTC"
                        + "&characterEncoding=UTF-8";
        private static final String USER = "root";
        private static final String PASS = "";  // XAMPP’de varsayılan boş şifre

        public static void main(String[] args) {
                // (A) Driver'ı yükle (opsiyonel, JDBC4+ ile gerek kalmaz ama hatayı erken yakalamak için ekliyoruz)
                try {
                        Class.forName("com.mysql.cj.jdbc.Driver");
                } catch (ClassNotFoundException e) {
                        System.err.println("Driver bulunamadı!");
                        e.printStackTrace();
                        return;
                }

                // (B) Bağlantıyı aç ve metotları çalıştır
                try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                        System.out.println("✅ Veritabanına bağlandı.");

                        // Tabloları listele
                        listManagers(conn);
                        listPersonnel(conn);

                        // Örnek: manager girişi doğrulama
                        boolean validManager = validateLogin(
                                conn,
                                "manager",      // tablo adı
                                "ali",          // test kullanıcı adı
                                "kılıc123"      // test şifre
                        );
                        System.out.println("Manager login geçerli mi? " + validManager);

                        // Örnek: personnel girişi doğrulama
                        boolean validPersonnel = validateLogin(
                                conn,
                                "personnel",
                                "caner",
                                "kurt123"
                        );
                        System.out.println("Personnel login geçerli mi? " + validPersonnel);

                } catch (SQLException ex) {
                        System.err.println("❌ Bağlantı/SQL hatası:");
                        ex.printStackTrace();
                }
        }

        /**
         * manager tablosundaki tüm kayıtları yazdırır.
         */
        public static void listManagers(Connection conn) throws SQLException {
                String sql = "SELECT `ıd` AS id, `name`, `password` FROM manager";
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery(sql)) {
                        System.out.println("\n== Manager Tablosu ==");
                        while (rs.next()) {
                                int id   = rs.getInt("id");
                                String name = rs.getString("name");
                                String pw   = rs.getString("password");
                                System.out.printf("ID=%d, Name=%s, Password=%s%n", id, name, pw);
                        }
                }
        }

        /**
         * personnel tablosundaki tüm kayıtları yazdırır.
         */
        public static void listPersonnel(Connection conn) throws SQLException {
                String sql = "SELECT `ıd` AS id, `name`, `password` FROM personnel";
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery(sql)) {
                        System.out.println("\n== Personnel Tablosu ==");
                        while (rs.next()) {
                                int id   = rs.getInt("id");
                                String name = rs.getString("name");
                                String pw   = rs.getString("password");
                                System.out.printf("ID=%d, Name=%s, Password=%s%n", id, name, pw);
                        }
                }
        }

        /**
         * Verilen tablo adında name/password eşleşmesi var mı diye kontrol eder.
         */
        public static boolean validateLogin(
                Connection conn,
                String table,
                String userName,
                String password
        ) throws SQLException {
                // Parametreli sorgu ile SQL injection'dan koruruz.
                String sql = String.format(
                        "SELECT COUNT(*) FROM `%s` WHERE `name` = ? AND `password` = ?",
                        table
                );
                try (PreparedStatement pst = conn.prepareStatement(sql)) {
                        pst.setString(1, userName);
                        pst.setString(2, password);
                        try (ResultSet rs = pst.executeQuery()) {
                                rs.next();
                                return rs.getInt(1) > 0;
                        }
                }
        }
}
