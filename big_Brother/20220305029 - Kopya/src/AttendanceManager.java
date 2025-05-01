// AttendanceManager.java
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AttendanceManager {
    private final List<AttendanceRecord> list = new ArrayList<>();

    // Giriş kaydını belleğe ve DB'ye ekler
    public void recordEntry(Account a) {
        LocalDate d = LocalDate.now();
        LocalTime t = LocalTime.now().withSecond(0).withNano(0);
        a.setActive(true);
        AttendanceRecord rec = new AttendanceRecord(a, d, t);
        list.add(rec);

        String sql = "INSERT INTO attendance(user_id, date, entry_time) VALUES(?,?,?)";
        try (Connection c = DatabaseConnector.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, a.getId());
            s.setObject(2, d);
            s.setObject(3, t);
            s.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Çıkış kaydını belleğe ve DB'ye günceller
    public void recordExit(Account a) {
        LocalDate d = LocalDate.now();
        LocalTime t = LocalTime.now().withSecond(0).withNano(0);
        a.setActive(false);
        for (AttendanceRecord r : list) {
            if (r.getWho().getId() == a.getId() && r.getDate().equals(d) && r.getExit() == null) {
                r.setExit(t);
                break;
            }
        }

        String sql = "UPDATE attendance SET exit_time=? WHERE user_id=? AND date=? AND exit_time IS NULL";
        try (Connection c = DatabaseConnector.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setObject(1, t);
            s.setInt(2, a.getId());
            s.setObject(3, d);
            s.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Bellekteki günlük kayıtlar
    public List<AttendanceRecord> getDailyRecords(LocalDate date) {
        List<AttendanceRecord> result = new ArrayList<>();
        for (AttendanceRecord r : list) {
            if (r.getDate().equals(date)) {
                result.add(r);
            }
        }
        return List.copyOf(result);
    }

    // DB'den günlük kayıtları yükler
    public List<AttendanceRecord> loadDailyFromDb(LocalDate day, List<Account> allAccounts) {
        List<AttendanceRecord> result = new ArrayList<>();
        String sql = "SELECT user_id, date, entry_time, exit_time FROM attendance WHERE date = ?";
        try (Connection c = DatabaseConnector.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, day);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int uid = rs.getInt("user_id");
                    LocalDate d = rs.getObject("date", LocalDate.class);
                    LocalTime in = rs.getObject("entry_time", LocalTime.class);
                    LocalTime out = rs.getObject("exit_time", LocalTime.class);
                    for (Account acc : allAccounts) {
                        if (acc.getId() == uid) {
                            AttendanceRecord rec = new AttendanceRecord(acc, d, in);
                            rec.setExit(out);
                            result.add(rec);
                            break;
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    // O güne devamsız olan hesaplar
    public List<Account> getAbsentees(List<Account> all, LocalDate day) {
        List<Account> abs = new ArrayList<>();
        for (Account a : all) {
            boolean seen = false;
            for (AttendanceRecord r : list) {
                if (r.getWho().getId() == a.getId() && r.getDate().equals(day)) {
                    seen = true;
                    break;
                }
            }
            if (!seen) abs.add(a);
        }
        return List.copyOf(abs);
    }

    // Yeni kullanıcı ekleme
    public int addUserToDb(String name, String password) {
        int newId = 1;
        String maxSql = "SELECT MAX(`ıd`) FROM personnel";
        try (Connection c = DatabaseConnector.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(maxSql)) {
            if (rs.next()) {
                newId = rs.getInt(1) + 1;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return -1;
        }
        String insertSql = "INSERT INTO personnel(`ıd`,`name`,`password`) VALUES(?,?,?)";
        try (Connection c = DatabaseConnector.getConnection();
             PreparedStatement ps = c.prepareStatement(insertSql)) {
            ps.setInt(1, newId);
            ps.setString(2, name);
            ps.setString(3, password);
            ps.executeUpdate();
            return newId;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    // Kullanıcı silme
    public boolean removeUserFromDb(String name) {
        String deleteSql = "DELETE FROM personnel WHERE `name` = ?";
        try (Connection c = DatabaseConnector.getConnection();
             PreparedStatement ps = c.prepareStatement(deleteSql)) {
            ps.setString(1, name);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
