package com.inventory.ui;
import com.inventory.dao.UserDAO;
import com.inventory.dao.SecurityQuestionDAO;
import com.inventory.model.User;
import com.inventory.util.FirebaseConfig;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    // Color scheme
    private static final Color BG        = new Color(15, 15, 26);
    private static final Color CARD_BG   = new Color(22, 33, 62);
    private static final Color BORDER    = new Color(42, 42, 74);
    private static final Color TEXT      = new Color(226, 232, 240);
    private static final Color MUTED     = new Color(148, 163, 184);
    private static final Color DIM       = new Color(71, 85, 105);
    private static final Color INDIGO    = new Color(79, 70, 229);
    private static final Color INDIGO_LT = new Color(129, 140, 248);
    private static final Color RED       = new Color(239, 68, 68);
    private static final Color FIELD_BG  = new Color(15, 23, 42);

    public LoginFrame() {
        setTitle("InvenTrack — Login");
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(BG);

        // Center card panel
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));
        card.setPreferredSize(new Dimension(420, 540));
        card.setMaximumSize(new Dimension(420, 540));

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
                g2d.setColor(INDIGO);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(4, 2, 42, 48, 4, 4);
                g2d.setColor(INDIGO);
                g2d.fillRoundRect(4, 2, 7, 48, 3, 3);
                g2d.setColor(INDIGO);
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
        logoIcon.setBackground(CARD_BG);

        // App name
        JLabel appName = new JLabel("<html><center><span style='color:#ffffff;font-size:22px;'>Inven</span><span style='color:#818cf8;font-size:22px;'>Track</span></center></html>");
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitle = new JLabel("Sign in to your account");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(DIM);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Username label
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        userLabel.setForeground(MUTED);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Username field
        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setForeground(TEXT);
        usernameField.setBackground(FIELD_BG);
        usernameField.setCaretColor(INDIGO_LT);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password label
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        passLabel.setForeground(MUTED);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password field
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setForeground(TEXT);
        passwordField.setBackground(FIELD_BG);
        passwordField.setCaretColor(INDIGO_LT);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Login button
        JButton loginBtn = new JButton("Sign In");
        loginBtn.setFont(new Font("Arial", Font.BOLD, 14));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBackground(INDIGO);
        loginBtn.setOpaque(true);
        loginBtn.setContentAreaFilled(true);
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Error label
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        errorLabel.setForeground(RED);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Forgot Password link
        JLabel forgotLink = new JLabel("Forgot Password?");
        forgotLink.setFont(new Font("Arial", Font.PLAIN, 12));
        forgotLink.setForeground(INDIGO_LT);
        forgotLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        forgotLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showForgotPasswordDialog();
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                forgotLink.setText("<html><u>Forgot Password?</u></html>");
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                forgotLink.setText("Forgot Password?");
            }
        });

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
        card.add(Box.createVerticalStrut(8));
        card.add(forgotLink);

        add(card);
        setVisible(true);
    }

    private void handleLogin(JLabel errorLabel) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        UserDAO userDAO = new UserDAO();
        User user = userDAO.login(username, password);

        if (user != null) {
            com.inventory.util.SessionManager.setCurrentUser(user);

            // Check active session enforcement when Firebase is connected
            if (FirebaseConfig.isConnected()) {
                try {
                    var db = FirebaseConfig.getDB();
                    var docRef = db.collection("active_sessions").document(username);
                    var docSnapshot = docRef.get().get();
                    if (docSnapshot.exists()) {
                        Boolean active = docSnapshot.getBoolean("active");
                        if (active != null && active) {
                            int choice = JOptionPane.showConfirmDialog(this,
                                "This account appears to be logged in on another device.\n" +
                                "Do you want to force login and take over the session?",
                                "Active Session Detected", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if (choice != JOptionPane.YES_OPTION) {
                                com.inventory.util.SessionManager.logout();
                                return;
                            }
                        }
                    }
                    // Write active session
                    Map<String, Object> sessionData = new HashMap<>();
                    sessionData.put("username", username);
                    sessionData.put("active", true);
                    sessionData.put("sessionId", com.inventory.util.SessionManager.getSessionId());
                    sessionData.put("loginTime", com.google.cloud.Timestamp.now());
                    docRef.set(sessionData).get();
                } catch (Exception e) {
                    System.out.println("Active session check error: " + e.getMessage());
                    // Allow login even if check fails
                }
            }

            dispose();
            new MainFrame();
        } else {
            errorLabel.setText("Invalid username or password!");
            passwordField.setText("");
        }
    }

    private void showForgotPasswordDialog() {
        JDialog dialog = new JDialog(this, "Reset Password", true);
        dialog.setSize(480, 600);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(CARD_BG);
        dialog.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setBackground(CARD_BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(24, 30, 16, 30));

        // Title
        JLabel title = new JLabel("Reset Password");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(6));

        JLabel desc = new JLabel("Enter your username and answer security questions");
        desc.setFont(new Font("Arial", Font.PLAIN, 12));
        desc.setForeground(new Color(100, 116, 139));
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(desc);
        content.add(Box.createVerticalStrut(20));

        // Username input
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        userLabel.setForeground(MUTED);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(userLabel);
        content.add(Box.createVerticalStrut(4));

        JTextField userField = new JTextField();
        userField.setFont(new Font("Arial", Font.PLAIN, 14));
        userField.setForeground(TEXT);
        userField.setBackground(FIELD_BG);
        userField.setCaretColor(INDIGO_LT);
        userField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        userField.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(userField);
        content.add(Box.createVerticalStrut(16));

        // Panel to hold security questions (shown after username lookup)
        JPanel questionsPanel = new JPanel();
        questionsPanel.setBackground(CARD_BG);
        questionsPanel.setLayout(new BoxLayout(questionsPanel, BoxLayout.Y_AXIS));
        questionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        questionsPanel.setVisible(false);

        // Status/error label
        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(RED);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // New password fields (shown after questions verified)
        JPanel newPassPanel = new JPanel();
        newPassPanel.setBackground(CARD_BG);
        newPassPanel.setLayout(new BoxLayout(newPassPanel, BoxLayout.Y_AXIS));
        newPassPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        newPassPanel.setVisible(false);

        JPasswordField newPassField = new JPasswordField();
        stylePasswordField(newPassField);
        JPasswordField confirmPassField = new JPasswordField();
        stylePasswordField(confirmPassField);

        JLabel newPassLabel = new JLabel("New Password");
        newPassLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        newPassLabel.setForeground(MUTED);
        newPassLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel confirmLabel = new JLabel("Confirm Password");
        confirmLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        confirmLabel.setForeground(MUTED);
        confirmLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        newPassPanel.add(newPassLabel);
        newPassPanel.add(Box.createVerticalStrut(4));
        newPassPanel.add(newPassField);
        newPassPanel.add(Box.createVerticalStrut(12));
        newPassPanel.add(confirmLabel);
        newPassPanel.add(Box.createVerticalStrut(4));
        newPassPanel.add(confirmPassField);

        // Shared state
        final UserDAO userDAO = new UserDAO();
        final SecurityQuestionDAO securityDAO = new SecurityQuestionDAO();
        final User[] foundUser = {null};
        @SuppressWarnings("unchecked")
        final Map<String, String>[] storedQA = new Map[]{null};
        final JTextField[] answerFields = new JTextField[SecurityQuestionDAO.QUESTIONS.length];

        // "Find Account" button
        JButton findBtn = new JButton("Find Account");
        findBtn.setFont(new Font("Arial", Font.BOLD, 13));
        findBtn.setForeground(Color.WHITE);
        findBtn.setBackground(INDIGO);
        findBtn.setOpaque(true);
        findBtn.setContentAreaFilled(true);
        findBtn.setBorderPainted(false);
        findBtn.setFocusPainted(false);
        findBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        findBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        findBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        // "Verify Answers" button
        JButton verifyBtn = new JButton("Verify Answers");
        verifyBtn.setFont(new Font("Arial", Font.BOLD, 13));
        verifyBtn.setForeground(Color.WHITE);
        verifyBtn.setBackground(new Color(245, 158, 11));
        verifyBtn.setOpaque(true);
        verifyBtn.setContentAreaFilled(true);
        verifyBtn.setBorderPainted(false);
        verifyBtn.setFocusPainted(false);
        verifyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        verifyBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        verifyBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        verifyBtn.setVisible(false);

        // "Reset Password" button
        JButton resetBtn = new JButton("Reset Password");
        resetBtn.setFont(new Font("Arial", Font.BOLD, 13));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setBackground(new Color(34, 197, 94));
        resetBtn.setOpaque(true);
        resetBtn.setContentAreaFilled(true);
        resetBtn.setBorderPainted(false);
        resetBtn.setFocusPainted(false);
        resetBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        resetBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        resetBtn.setVisible(false);

        // Step 1: Find account
        findBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            if (username.isEmpty()) {
                statusLabel.setText("naam bo apna .");
                return;
            }

            foundUser[0] = userDAO.getUserByUsername(username);
            if (foundUser[0] == null) {
                statusLabel.setText("jhut bolraha he kon he be tuh.");
                return;
            }

            storedQA[0] = securityDAO.getQuestionsByUserId(foundUser[0].getId());
            if (storedQA[0] == null || storedQA[0].isEmpty()) {
                statusLabel.setText("ishka koi security questionset nahi huwa he.");
                return;
            }

            // Show questions
            questionsPanel.removeAll();
            JSeparator qSep = new JSeparator();
            qSep.setForeground(BORDER);
            qSep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            questionsPanel.add(qSep);
            questionsPanel.add(Box.createVerticalStrut(12));

            JLabel qTitle = new JLabel("Koi 2 security questions ka answer dede:");
            qTitle.setFont(new Font("Arial", Font.BOLD, 13));
            qTitle.setForeground(new Color(245, 158, 11));
            qTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            questionsPanel.add(qTitle);
            questionsPanel.add(Box.createVerticalStrut(10));

            int idx = 0;
            for (String question : storedQA[0].keySet()) {
                JLabel qLabel = new JLabel((idx + 1) + ". " + question);
                qLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                qLabel.setForeground(MUTED);
                qLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                questionsPanel.add(qLabel);
                questionsPanel.add(Box.createVerticalStrut(4));

                JTextField aField = new JTextField();
                aField.setFont(new Font("Arial", Font.PLAIN, 13));
                aField.setForeground(TEXT);
                aField.setBackground(FIELD_BG);
                aField.setCaretColor(INDIGO_LT);
                aField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER, 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
                aField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                aField.setAlignmentX(Component.LEFT_ALIGNMENT);
                questionsPanel.add(aField);
                questionsPanel.add(Box.createVerticalStrut(8));

                answerFields[idx] = aField;
                idx++;
            }

            questionsPanel.setVisible(true);
            verifyBtn.setVisible(true);
            findBtn.setVisible(false);
            userField.setEditable(false);
            statusLabel.setText(" ");
            dialog.revalidate();
            dialog.repaint();
        });

        // Step 2: Verify answers (need at least 2 correct out of total)
        verifyBtn.addActionListener(e -> {
            int idx = 0;
            int correctCount = 0;
            int totalQuestions = storedQA[0].size();

            for (Map.Entry<String, String> entry : storedQA[0].entrySet()) {
                String expected = entry.getValue().toLowerCase().trim();
                String given = answerFields[idx].getText().toLowerCase().trim();
                if (!given.isEmpty() && expected.equals(given)) {
                    correctCount++;
                }
                idx++;
            }

            int required = 2; // Need at least 2 correct answers
            if (correctCount < required) {
                statusLabel.setText("At least " + required + " correct answers needed. You got " + correctCount + ". Try again.");
                return;
            }

            // Show new password fields
            questionsPanel.setVisible(false);
            verifyBtn.setVisible(false);
            newPassPanel.setVisible(true);
            resetBtn.setVisible(true);
            statusLabel.setForeground(new Color(34, 197, 94));
            statusLabel.setText("✓ Identity verified! Naya password kya rakhna he");
            dialog.revalidate();
            dialog.repaint();
        });

        // Step 3: Reset password
        resetBtn.addActionListener(e -> {
            String newPass = new String(newPassField.getPassword()).trim();
            String confirmPass = new String(confirmPassField.getPassword()).trim();

            if (newPass.isEmpty()) {
                statusLabel.setForeground(RED);
                statusLabel.setText("khali password dekha he ?.");
                return;
            }
            if (newPass.length() < 4) {
                statusLabel.setForeground(RED);
                statusLabel.setText("Password must be at least 4 characters.");
                return;
            }
            if (!newPass.equals(confirmPass)) {
                statusLabel.setForeground(RED);
                statusLabel.setText("galat he bhai .");
                return;
            }

            boolean success = userDAO.updatePassword(foundUser[0].getId(), newPass);
            if (success) {
                dialog.dispose();
                JOptionPane.showMessageDialog(this,
                    "Password reset successfully!\nYou can now log in with your new password.",
                    "Password Reset", JOptionPane.INFORMATION_MESSAGE);
            } else {
                statusLabel.setForeground(RED);
                statusLabel.setText("Failed to reset password. Try again.");
            }
        });

        // Assemble content
        content.add(statusLabel);
        content.add(Box.createVerticalStrut(12));
        content.add(findBtn);
        content.add(Box.createVerticalStrut(12));
        content.add(questionsPanel);
        content.add(Box.createVerticalStrut(12));
        content.add(verifyBtn);
        content.add(newPassPanel);
        content.add(Box.createVerticalStrut(12));
        content.add(resetBtn);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.setBackground(CARD_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Cancel button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(CARD_BG);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 12));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setBackground(new Color(100, 116, 139));
        cancelBtn.setOpaque(true);
        cancelBtn.setContentAreaFilled(true);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        cancelBtn.addActionListener(e -> dialog.dispose());
        bottomPanel.add(cancelBtn);

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void stylePasswordField(JPasswordField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setForeground(TEXT);
        field.setBackground(FIELD_BG);
        field.setCaretColor(INDIGO_LT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}