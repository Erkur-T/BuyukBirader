// DashboardWindow.java
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.List;

public class DashboardWindow extends JFrame {
    private final DefaultTableModel tableModel;
    private final JList<String> absenteeList;
    private final Account currentUser;
    private final List<Account> allUsers;
    private final AttendanceManager service;

    public DashboardWindow(Account user, List<Account> users, AttendanceManager service) {
        super("Devam Kontrol – Hoşgeldin " + user.getUsername());
        this.currentUser = user;
        this.allUsers    = users;
        this.service     = service;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Üst panel: Giriş / Çıkış ve durum
        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        top.setBorder(new EmptyBorder(10, 10, 0, 10));
        JButton btnEntry = createButton("Giriş Yap");
        JButton btnExit  = createButton("Çıkış Yap");
        JLabel lblStatus = new JLabel("Durum: " + (user.isActive() ? "Aktif" : "Pasif"));
        lblStatus.setFont(lblStatus.getFont().deriveFont(Font.BOLD));
        top.add(btnEntry);
        top.add(btnExit);
        top.add(lblStatus);

        // Tablo (Günlük Kayıtlar)
        String[] cols = {"Kullanıcı", "Tarih", "Giriş", "Çıkış"};
        tableModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(BorderFactory.createTitledBorder("Günlük Kayıtlar"));

        // East panel: Devamsızlar (ve yönetici için kullanıcı yönetimi)
        JComponent eastComponent;
        if (currentUser instanceof Administrator) {
            JTabbedPane eastTabs = new JTabbedPane();

            // Devamsızlar tab
            absenteeList = new JList<>(new DefaultListModel<>());
            JScrollPane devamsizScroll = wrapWithTitle("Devamsızlar", absenteeList);
            eastTabs.addTab("Devamsızlar", devamsizScroll);

            // Kullanıcı Yönetimi tab
            eastTabs.addTab("Kullanıcı Yönetimi", createUserManagementPanel());
            eastComponent = eastTabs;
        } else {
            absenteeList = new JList<>(new DefaultListModel<>());
            eastComponent = wrapWithTitle("Devamsızlar", absenteeList);
        }
        eastComponent.setPreferredSize(new Dimension(200, 0));

        add(top, BorderLayout.NORTH);
        add(scrollTable, BorderLayout.CENTER);
        add(eastComponent, BorderLayout.EAST);

        // Buton olayları
        btnEntry.addActionListener(e -> {
            service.recordEntry(currentUser);
            currentUser.setActive(true);
            lblStatus.setText("Durum: Aktif");
            refreshData();
        });
        btnExit.addActionListener(e -> {
            service.recordExit(currentUser);
            currentUser.setActive(false);
            lblStatus.setText("Durum: Pasif");
            refreshData();
        });

        refreshData();
        setVisible(true);
    }

    private void refreshData() {
        // Tabloyu güncelle
        tableModel.setRowCount(0);
        LocalDate today = LocalDate.now();
        service.getDailyRecords(today).forEach(r -> {
            if (r.getWho().getId() == currentUser.getId() || currentUser instanceof Administrator) {
                tableModel.addRow(new Object[]{
                        r.getWho().getUsername(),
                        r.getDate(),
                        r.getEntry(),
                        r.getExit() != null ? r.getExit() : ""
                });
            }
        });
        // Devamsızları güncelle
        DefaultListModel<String> dlm = new DefaultListModel<>();
        service.getAbsentees(allUsers, today).forEach(a -> dlm.addElement(a.getUsername()));
        absenteeList.setModel(dlm);
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(5,5));
        panel.setBorder(new EmptyBorder(10,10,10,10));
        DefaultListModel<String> listModel = new DefaultListModel<>();
        allUsers.forEach(a -> listModel.addElement(a.getUsername()));
        JList<String> userList = new JList<>(listModel);
        panel.add(new JScrollPane(userList), BorderLayout.WEST);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(new EmptyBorder(0,10,0,0));
        JTextField tfName = new JTextField();
        tfName.setMaximumSize(new Dimension(Integer.MAX_VALUE, tfName.getPreferredSize().height));
        JPasswordField pfPass = new JPasswordField();
        pfPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, pfPass.getPreferredSize().height));
        JButton btnAdd = createButton("Ekle");
        JButton btnRemove = createButton("Sil");

        right.add(new JLabel("Ad:")); right.add(tfName);
        right.add(Box.createVerticalStrut(5));
        right.add(new JLabel("Şifre:")); right.add(pfPass);
        right.add(Box.createVerticalStrut(10));
        right.add(btnAdd);
        right.add(Box.createVerticalStrut(5));
        right.add(btnRemove);

        btnAdd.addActionListener(e -> {
            String name = tfName.getText().trim();
            String pass = new String(pfPass.getPassword());
            if (!name.isEmpty() && !pass.isEmpty()) {
                int newId = service.addUserToDb(name, pass);
                if (newId > 0) {
                    Account a = new Staff(newId, name, pass);
                    allUsers.add(a);
                    listModel.addElement(name);
                    tfName.setText(""); pfPass.setText("");
                }
            }
        });
        btnRemove.addActionListener(e -> {
            String sel = userList.getSelectedValue();
            if (sel != null && service.removeUserFromDb(sel)) {
                allUsers.removeIf(a -> a.getUsername().equals(sel));
                listModel.removeElement(sel);
            }
        });

        panel.add(right, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane wrapWithTitle(String title, JComponent comp) {
        JScrollPane sp = new JScrollPane(comp);
        sp.setBorder(BorderFactory.createTitledBorder(title));
        return sp;
    }

    private JButton createButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setFont(b.getFont().deriveFont(Font.PLAIN));
        return b;
    }
}
