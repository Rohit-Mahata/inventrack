package com.inventory.ui;

import com.inventory.ui.panels.*;
import javax.swing.*;
import java.awt.*;

public class ContentPanel extends JPanel {

    private CardLayout cardLayout;

    public ContentPanel() {
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        // Add all panels with a name key
        add(new DashboardPanel(), "Dashboard");
        add(new ProductPanel(),   "Products");
        add(new StockPanel(),     "Stock");
        add(new SalesPanel(),     "Sales");
        add(new ReportsPanel(),   "Reports");

        // Show Dashboard by default
        cardLayout.show(this, "Dashboard");
    }

    // Called by Sidebar to switch panels
    public void showPanel(String name) {
        cardLayout.show(this, name);
    }
}