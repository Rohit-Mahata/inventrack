package com.inventory.ui;

import com.inventory.model.User;
import com.inventory.service.AuthService;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        setTitle("InvenTrack — Login");
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(15, 15, 26));

        // Center card panel
        JPanel card = new JPanel();
        card.setBackground(new Color(22, 33, 62));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(42, 42, 74), 1),
            BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));
        card.setPreferredSize(new Dimension(420, 500));
        card.setMaximumSize(new Dimension(420, 500));

        // Logo
        JPanel logoIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                     RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(30, 41, 59));
                g2d.fillRoundRect(4, 2, 42, 48, 4, 4);
                g2d.setColor(new Color(79, 70, 229));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(4, 2, 42, 48, 4, 4);
                g2d.setColor(new Color(79, 70, 229));
                g2d.fillRoundRect(4, 2, 7, 48, 3, 3);
                g2d.setColor(new Color(79, 70, 229));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawLine(18, 16, 42, 16);
                g2d.setColor(new Color(51, 65, 85));
                g2d.drawLine(18, 25, 42, 25);
                g2d.drawLine(18, 34, 42, 34);
                g2d.drawLine(18, 42, 42, 42);
                g2d.setColor(new Color(30, 41, 59));
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawLine(32, 10, 32, 48);
                g2d.setColor(new Color(34, 197, 94, 180));
                g2d.fillRoundRect(34, 18, 10, 6, 1, 1);
                g2d.setColor(new Color(245, 158, 11, 180));
                g2d.fillRoundRect(34, 27, 7, 6, 1, 1);
                g2d.setColor(new Color(34, 197, 94, 180));
                g2d.fillRoundRect(34, 36, 10, 6, 1, 1);
            }
        };
        logoIcon.setPreferredSize(new Dimension(52, 52));
        logoIcon.setMaximumSize(new Dimension(52, 52));
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoIcon.setBackground(new Color(22, 33, 62));

        // App name
        JLabel appName = new JLabel("<html><center><span style='color:#ffffff;font-size:22px;'>Inven</span><span style='color:#818cf8;font-size:22px;'>Track</span></center></html>");
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitle = new JLabel("Sign in to your account");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(new Color(71, 85, 105));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(42, 42, 74));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Username label
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        userLabel.setForeground(new Color(148, 163, 184));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Username field
        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setForeground(new Color(226, 232, 240));
        usernameField.setBackground(new Color(15, 23, 42));
        usernameField.setCaretColor(new Color(129, 140, 248));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(42, 42, 74), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password label
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        passLabel.setForeground(new Color(148, 163, 184));
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password field
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setForeground(new Color(226, 232, 240));
        passwordField.setBackground(new Color(15, 23, 42));
        passwordField.setCaretColor(new Color(129, 140, 248));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(42, 42, 74), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Login button
        JButton loginBtn = new JButton("Sign In");
        loginBtn.setFont(new Font("Arial", Font.BOLD, 14));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBackground(new Color(79, 70, 229));
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Error label
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(239, 68, 68));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Login action
        loginBtn.addActionListener(e -> handleLogin(errorLabel));

        // Allow Enter key on password field
        passwordField.addActionListener(e -> handleLogin(errorLabel));

        // Assemble card
        card.add(logoIcon);
        card.add(Box.createVerticalStrut(12));
        card.add(appName);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(24));
        card.add(sep);
        card.add(Box.createVerticalStrut(24));
        card.add(userLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(16));
        card.add(passLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(24));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(errorLabel);

        add(card);
        setVisible(true);
    }

    private void handleLogin(JLabel errorLabel) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password are required.");
            return;
        }

        User user = authService.authenticate(username, password);
        if (user != null) {
            dispose();
            new MainFrame(user);
        } else {
            errorLabel.setText("Invalid username or password!");
            passwordField.setText("");
        }
    }
}
