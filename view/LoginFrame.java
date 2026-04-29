package view;

import database.DatabaseHelper;
import model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class LoginFrame extends JFrame {
    private JTextField txtUser = new JTextField();
    private JPasswordField txtPass = new JPasswordField();

    public LoginFrame() {
        setTitle("College Event Portal | EventNexus");
        setSize(900, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

      
        JPanel sidebar = new JPanel(new GridBagLayout());
        sidebar.setBackground(new Color(44, 62, 80));
        sidebar.setPreferredSize(new Dimension(350, 550));

        JLabel logoText = new JLabel("<html><center>EVENTS<br>HUB</center></html>");
        logoText.setForeground(Color.WHITE);
        logoText.setFont(new Font("SansSerif", Font.BOLD, 40));
        sidebar.add(logoText);

     
        JPanel formArea = new JPanel(null);
        formArea.setBackground(Color.WHITE);

        JLabel lblWelcome = new JLabel("Welcome Back");
        lblWelcome.setFont(new Font("SansSerif", Font.BOLD, 32));
        lblWelcome.setBounds(50, 60, 400, 50);

        JLabel lblU = new JLabel("Username");
        lblU.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblU.setBounds(50, 130, 100, 25);
        txtUser.setBounds(50, 160, 350, 35);

        JLabel lblP = new JLabel("Password");
        lblP.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblP.setBounds(50, 210, 100, 25);
        txtPass.setBounds(50, 240, 350, 35);

        JButton btnLogin = new JButton("LOGIN");
        btnLogin.setBackground(new Color(41, 128, 185));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnLogin.setBounds(50, 300, 350, 45);

        JButton btnCreate = new JButton("Create Student Account");
        btnCreate.setBackground(Color.WHITE);
        btnCreate.setForeground(new Color(41, 128, 185));
        btnCreate.setBounds(50, 360, 350, 30);

        JButton btnGuest = new JButton("Continue as Guest");
        btnGuest.setBackground(new Color(189, 195, 199));
        btnGuest.setForeground(Color.BLACK);
        btnGuest.setBounds(50, 410, 350, 45);

      
        addHoverTip(btnLogin, "<b>Authentication</b><br>Login to access your club or admin dashboard.");
        addHoverTip(btnCreate, "<b>Registration</b><br>Create a new student profile to join events.");
        addHoverTip(btnGuest, "<b>Anonymous Browsing</b><br>View active events without logging in.");

    
        btnLogin.addActionListener(e -> processLogin());
        
        btnGuest.addActionListener(e -> launchGuestMode());

       
        btnCreate.addActionListener(e -> {
            JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
            JTextField newUserField = new JTextField();
            JPasswordField newPassField = new JPasswordField();

            panel.add(new JLabel("Choose Username:"));
            panel.add(newUserField);
            panel.add(new JLabel("Choose Password:"));
            panel.add(newPassField);

            int result = JOptionPane.showConfirmDialog(this, panel, "Register New Student",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String user = newUserField.getText().trim();
                String pass = new String(newPassField.getPassword());

                if (user.isEmpty() || pass.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "All fields are required.");
                    return;
                }

                List<User> users = DatabaseHelper.getUsers();
                
            
                boolean exists = false;
                for (User u : users) {
                    if (u.username.equalsIgnoreCase(user)) {
                        exists = true;
                        break;
                    }
                }

                if (exists) {
                    JOptionPane.showMessageDialog(this, "Username already exists. Try another.");
                } else {
                    users.add(new User(user, pass, "STUDENT", "NONE"));
                    DatabaseHelper.saveUsers(users);
                    JOptionPane.showMessageDialog(this, "Registration Successful! You can now login.");
                }
            }
        });

        formArea.add(lblWelcome);
        formArea.add(lblU); formArea.add(txtUser);
        formArea.add(lblP); formArea.add(txtPass);
        formArea.add(btnLogin);
        formArea.add(btnCreate);
        formArea.add(btnGuest);

        add(sidebar, BorderLayout.WEST);
        add(formArea, BorderLayout.CENTER);

        setLocationRelativeTo(null);
    }

    private void addHoverTip(JButton button, String message) {
        JPopupMenu tipPopup = new JPopupMenu();
        tipPopup.setBorder(BorderFactory.createLineBorder(new Color(44, 62, 80), 1));

        JLabel tipLabel = new JLabel("<html><div style='padding:8px; background:#FFFFFF; color:#2C3E50;'>"
                                    + message + "</div></html>");
        tipLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tipPopup.add(tipLabel);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tipPopup.show(button, 0, -tipPopup.getPreferredSize().height - 5);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                tipPopup.setVisible(false);
            }
        });
    }

    private void processLogin() {
        String u = txtUser.getText().trim();
        String p = new String(txtPass.getPassword());

        List<User> accounts = DatabaseHelper.getUsers();
        User found = null;

        for (User account : accounts) {
            if (account.username.equals(u) && account.password.equals(p)) {
                found = account;
                break;
            }
        }

        if (found != null) {
            if (!found.isActive) {
                JOptionPane.showMessageDialog(this, "Account Locked. Contact Admin.");
                return;
            }
            DatabaseHelper.currentUser = found.username;
            if (found.role.equalsIgnoreCase("ADMIN")) new AdminDash().setVisible(true);
            else if (found.role.equalsIgnoreCase("CLUB")) new ClubDash(found.clubName).setVisible(true);
            else new StudentDash().setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Username or Password.");
        }
    }

    private void launchGuestMode() {
        DatabaseHelper.currentUser = "Guest";
        new StudentDash().setVisible(true);
        this.dispose();
    }
}
