// AttendanceRecord.java
import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceRecord {
    private final Account who;
    private final LocalDate date;
    private final LocalTime entry;
    private LocalTime exit;

    public AttendanceRecord(Account who, LocalDate date, LocalTime entry) {
        this.who   = who;
        this.date  = date;
        this.entry = entry;
        this.exit  = null;
    }

    public Account getWho()     { return who; }
    public LocalDate getDate()  { return date; }
    public LocalTime getEntry() { return entry; }
    public LocalTime getExit()  { return exit; }
    public void setExit(LocalTime t) { this.exit = t; }

    @Override
    public String toString() {
        // getName() yerine getUsername() kullanıyoruz
        return String.format("%s @ %s  giriş=%s  çıkış=%s",
                who.getUsername(), date, entry,
                exit == null ? "--" : exit);
    }
}
