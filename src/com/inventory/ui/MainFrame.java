package com.inventory.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        // Window settings
        setTitle("Inventory Management System");
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top bar
        TopBar topBar = new TopBar();

        // Content panel (CardLayout - holds all panels)
        ContentPanel contentPanel = new ContentPanel();

        // Sidebar (gets reference to contentPanel for switching)
        Sidebar sidebar = new Sidebar(contentPanel);

        // Add to frame
        add(topBar,       BorderLayout.NORTH);
        add(sidebar,      BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        setVisible(true);
    }
}