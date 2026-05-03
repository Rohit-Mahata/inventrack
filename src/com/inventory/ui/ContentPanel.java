package com.inventory.ui;

import com.inventory.ui.panels.*;
import javax.swing.*;
import java.awt.*;

public class ContentPanel extends JPanel {

    private static ContentPanel instance;
    private CardLayout cardLayout;
    private DashboardPanel dashboardPanel;
    private ProductPanel productPanel;
    private StockPanel stockPanel;
    private SalesPanel salesPanel;
    private UserPanel userPanel;
    private String currentPanel = "Dashboard";

    public static ContentPanel getInstance() {
        return instance;
    }

    public ContentPanel() {
        instance = this;
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        dashboardPanel = new DashboardPanel();
        productPanel   = new ProductPanel();
        stockPanel     = new StockPanel();
        salesPanel     = new SalesPanel();
        userPanel      = new UserPanel();

        add(dashboardPanel, "Dashboard");
        add(productPanel,   "Products");
        add(stockPanel,     "Stock");
        add(salesPanel,     "Sales");
        add(userPanel,      "Users");
        cardLayout.show(this, "Dashboard");
    }

    public void showPanel(String name) {
        currentPanel = name;
        cardLayout.show(this, name);
    }

    public void refreshCurrent() {
        switch (currentPanel) {
            case "Dashboard" -> dashboardPanel.loadData();
            case "Products"  -> productPanel.loadProducts();
            case "Stock"     -> stockPanel.loadMovements();
            case "Sales"     -> salesPanel.loadSales();
            case "Users"     -> userPanel.loadUsers();
        }
    }
}