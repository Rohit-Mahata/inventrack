package com.inventory.ui;

import com.inventory.ui.panels.*;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ContentPanel extends JPanel {

    private CardLayout cardLayout;
    private final Map<String, JPanel> panelMap = new HashMap<>();

    public ContentPanel() {
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        // Add all panels with a name key
        addPanel("Dashboard", new DashboardPanel());
        addPanel("Products", new ProductPanel());
        addPanel("Stock", new StockPanel());
        addPanel("Sales", new SalesPanel());
        addPanel("Reports", new ReportsPanel());

        // Show Dashboard by default
        cardLayout.show(this, "Dashboard");
        refreshPanel("Dashboard");
    }

    // Called by Sidebar to switch panels
    public void showPanel(String name) {
        cardLayout.show(this, name);
        refreshPanel(name);
    }

    private void addPanel(String name, JPanel panel) {
        add(panel, name);
        panelMap.put(name, panel);
    }

    private void refreshPanel(String name) {
        JPanel panel = panelMap.get(name);
        if (panel instanceof Refreshable refreshable) {
            refreshable.refreshData();
        }
    }
}
