package com.inventory.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.inventory.util.SessionManager;

public class Sidebar extends JPanel {

    private ContentPanel contentPanel;
    private JButton activeButton;

    private static final Color BG          = new Color(22, 33, 62);
    private static final Color ACTIVE_BG   = new Color(79, 70, 229);
    private static final Color HOVER_BG    = new Color(30, 45, 80);
    private static final Color TEXT_ACTIVE = Color.WHITE;
    private static final Color TEXT_NORMAL = new Color(100, 116, 139);

    public Sidebar(ContentPanel contentPanel) {
        this.contentPanel = contentPanel;

        setPreferredSize(new Dimension(200, 0));
        setBackground(BG);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(42, 42, 74)));
        setLayout(new BorderLayout());

        // Top nav buttons panel
        JPanel navPanel = new JPanel();
        navPanel.setBackground(BG);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(16, 10, 16, 10));

        // Create nav buttons
        JButton dashboardBtn = createNavButton("Dashboard");
        JButton productsBtn  = createNavButton("Products");
        JButton stockBtn     = createNavButton("Stock");
        JButton salesBtn     = createNavButton("Sales");
        JButton reportsBtn   = createNavButton("Reports");
// Users button - admin only
if (SessionManager.isAdmin()) {
    JButton usersBtn = createNavButton("Users");
    usersBtn.addActionListener(e -> {
        switchPanel(usersBtn, "Users");
        contentPanel.refreshCurrent();
    });
    navPanel.add(Box.createVerticalStrut(4));
    navPanel.add(usersBtn);
}

        // Add action listeners
        dashboardBtn.addActionListener(e -> { switchPanel(dashboardBtn, "Dashboard"); contentPanel.refreshCurrent(); });
productsBtn .addActionListener(e -> { switchPanel(productsBtn,  "Products");  contentPanel.refreshCurrent(); });
stockBtn    .addActionListener(e -> { switchPanel(stockBtn,     "Stock");     contentPanel.refreshCurrent(); });
salesBtn    .addActionListener(e -> { switchPanel(salesBtn,     "Sales");     contentPanel.refreshCurrent(); });
reportsBtn  .addActionListener(e -> { switchPanel(reportsBtn,   "Reports");   contentPanel.refreshCurrent(); });

        // Set dashboard as default active
        setActive(dashboardBtn);

        navPanel.add(dashboardBtn);
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(productsBtn);
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(stockBtn);
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(salesBtn);
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(reportsBtn);

        // Bottom settings button
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(BG);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(42, 42, 74)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JButton settingsBtn = createNavButton("Settings");
        bottomPanel.add(settingsBtn);

        add(navPanel,    BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JButton createNavButton(String title) {
        JButton button = new JButton(title);
        button.setFont(new Font("Arial", Font.PLAIN, 13));
        button.setForeground(TEXT_NORMAL);
        button.setBackground(BG);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button != activeButton) {
                    button.setBackground(HOVER_BG);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (button != activeButton) {
                    button.setBackground(BG);
                }
            }
        });

        return button;
    }

    private void switchPanel(JButton button, String panelName) {
        setActive(button);
        contentPanel.showPanel(panelName);
    }

    private void setActive(JButton button) {
        // Reset previous active button
        if (activeButton != null) {
            activeButton.setBackground(BG);
            activeButton.setForeground(TEXT_NORMAL);
            activeButton.setFont(new Font("Arial", Font.PLAIN, 13));
        }

        // Set new active button
        activeButton = button;
        activeButton.setBackground(ACTIVE_BG);
        activeButton.setForeground(TEXT_ACTIVE);
        activeButton.setFont(new Font("Arial", Font.BOLD, 13));
    }
}