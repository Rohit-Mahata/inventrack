package com.inventory.ui;

import com.inventory.model.User;

import javax.swing.*;
import java.awt.*;

public class TopBar extends JPanel {

    public TopBar(User user, Runnable onLogout) {
        String username = user != null && user.getUsername() != null && !user.getUsername().trim().isEmpty()
            ? user.getUsername().trim() : "User";
        String role = user != null && user.getRole() != null && !user.getRole().trim().isEmpty()
            ? user.getRole().trim() : "staff";

        setPreferredSize(new Dimension(0, 60));
        setBackground(new Color(22, 33, 62));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(42, 42, 74)));
        setLayout(new BorderLayout());

        // Left side - logo + name
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(22, 33, 62));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        // Logo icon (drawn with Java2D)
        JPanel logoIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                     RenderingHints.VALUE_ANTIALIAS_ON);

                // Book body (dark)
                g2d.setColor(new Color(30, 41, 59));
                g2d.fillRoundRect(4, 2, 28, 32, 4, 4);

                // Book border
                g2d.setColor(new Color(79, 70, 229));
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRoundRect(4, 2, 28, 32, 4, 4);

                // Spine (left indigo bar)
                g2d.setColor(new Color(79, 70, 229));
                g2d.fillRoundRect(4, 2, 5, 32, 3, 3);

                // Horizontal lines
                g2d.setColor(new Color(79, 70, 229));
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawLine(13, 11, 28, 11);

                g2d.setColor(new Color(51, 65, 85));
                g2d.drawLine(13, 17, 28, 17);
                g2d.drawLine(13, 23, 28, 23);
                g2d.drawLine(13, 29, 28, 29);

                // Vertical divider
                g2d.setColor(new Color(30, 41, 59));
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawLine(22, 8, 22, 32);

                // Green cell
                g2d.setColor(new Color(34, 197, 94, 180));
                g2d.fillRoundRect(23, 12, 7, 4, 1, 1);

                // Amber cell
                g2d.setColor(new Color(245, 158, 11, 180));
                g2d.fillRoundRect(23, 18, 5, 4, 1, 1);

                // Green cell
                g2d.setColor(new Color(34, 197, 94, 180));
                g2d.fillRoundRect(23, 24, 7, 4, 1, 1);
            }
        };
        logoIcon.setPreferredSize(new Dimension(36, 36));
        logoIcon.setMaximumSize(new Dimension(36, 36));
        logoIcon.setBackground(new Color(22, 33, 62));

        // App name panel
        JPanel namePanel = new JPanel();
        namePanel.setBackground(new Color(22, 33, 62));
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        // "InvenTrack" label
        JLabel appName = new JLabel("<html><span style='color:#ffffff'>Inven</span>" +
                                    "<span style='color:#818cf8'>Track</span></html>");
        appName.setFont(new Font("Arial", Font.BOLD, 16));

        // "INVENTORY SYSTEM" label
        JLabel appSub = new JLabel("INVENTORY SYSTEM");
        appSub.setFont(new Font("Arial", Font.PLAIN, 9));
        appSub.setForeground(new Color(71, 85, 105));

        namePanel.add(appName);
        namePanel.add(appSub);

        leftPanel.add(logoIcon);
        leftPanel.add(namePanel);

        // Right side - user info + logout
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(new Color(22, 33, 62));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.X_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

        // Avatar circle
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                     RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(79, 70, 229));
                g2d.fillOval(0, 0, 32, 32);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                FontMetrics fm = g2d.getFontMetrics();
                String text = buildInitials(username);
                int x = (32 - fm.stringWidth(text)) / 2;
                int y = (32 - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(text, x, y);
            }
        };
        avatar.setPreferredSize(new Dimension(32, 32));
        avatar.setMaximumSize(new Dimension(32, 32));
        avatar.setBackground(new Color(22, 33, 62));

        // User name + role
        JPanel userPanel = new JPanel();
        userPanel.setBackground(new Color(22, 33, 62));
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        JLabel userName = new JLabel(username);
        userName.setFont(new Font("Arial", Font.BOLD, 13));
        userName.setForeground(new Color(226, 232, 240));

        JLabel userRole = new JLabel(formatRole(role));
        userRole.setFont(new Font("Arial", Font.PLAIN, 10));
        userRole.setForeground(new Color(71, 85, 105));

        userPanel.add(userName);
        userPanel.add(userRole);

        // Divider
        JSeparator divider = new JSeparator(JSeparator.VERTICAL);
        divider.setPreferredSize(new Dimension(1, 24));
        divider.setMaximumSize(new Dimension(1, 24));
        divider.setForeground(new Color(42, 42, 74));

        // Logout button
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        logoutBtn.setForeground(new Color(148, 163, 184));
        logoutBtn.setBackground(new Color(22, 33, 62));
        logoutBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(58, 58, 90), 1),
            BorderFactory.createEmptyBorder(5, 14, 5, 14)
        ));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            if (onLogout != null) onLogout.run();
        });

        rightPanel.add(avatar);
        rightPanel.add(userPanel);
        rightPanel.add(Box.createHorizontalStrut(16));
        rightPanel.add(divider);
        rightPanel.add(Box.createHorizontalStrut(16));
        rightPanel.add(logoutBtn);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }

    private String buildInitials(String name) {
        String clean = name == null ? "" : name.trim();
        if (clean.isEmpty()) return "US";

        String[] parts = clean.split("\\s+");
        String first = parts.length > 0 && !parts[0].isEmpty() ? parts[0].substring(0, 1) : "U";
        String second;
        if (parts.length > 1 && !parts[1].isEmpty()) {
            second = parts[1].substring(0, 1);
        } else if (parts.length > 0 && parts[0].length() > 1) {
            second = parts[0].substring(1, 2);
        } else {
            second = "S";
        }
        return (first + second).toUpperCase();
    }

    private String formatRole(String role) {
        String clean = role == null ? "" : role.trim();
        if (clean.isEmpty()) return "Staff";
        return clean.substring(0, 1).toUpperCase() + clean.substring(1);
    }
}
