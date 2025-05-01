// AuthWindow.java
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AuthWindow extends JFrame {
    public AuthWindow(List<Account> users, AttendanceManager mgr) {
        super("Giriş Yap");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(300, 180);
        setLocationRelativeTo(null);

        setLayout(new GridLayout(3, 2, 5, 5));
        JTextField tfUser     = new JTextField();
        JPasswordField pfPass = new JPasswordField();
        JButton btnLogin      = new JButton("Giriş");

        add(new JLabel("Kullanıcı:")); add(tfUser);
        add(new JLabel("Şifre:"));     add(pfPass);
        add(new JLabel());             add(btnLogin);

        btnLogin.addActionListener(e -> {
            String u = tfUser.getText().trim();
            String p = new String(pfPass.getPassword());
            for (Account acct : users) {
                if (acct.getUsername().equals(u) && acct.checkPassword(p)) {
                    dispose();
                    new DashboardWindow(acct, users, mgr);
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Geçersiz kullanıcı/şifre");
        });

        setVisible(true);
    }
}
