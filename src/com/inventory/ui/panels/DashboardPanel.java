package com.inventory.ui.panels;

import com.inventory.dao.ProductDAO;
import com.inventory.dao.SaleDAO;
import com.inventory.model.Product;
import com.inventory.model.Sale;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {

    private ProductDAO productDAO = new ProductDAO();
    private SaleDAO saleDAO = new SaleDAO();

    private JLabel totalProductsLabel;
    private JLabel lowStockLabel;
    private JLabel todaySalesLabel;
    private JLabel monthlySalesLabel;
    private JTable recentSalesTable;
    private JTable lowStockTable;

    private static final Color BG         = new Color(26, 26, 46);
    private static final Color CARD_BG    = new Color(22, 33, 62);
    private static final Color BORDER     = new Color(42, 42, 74);
    private static final Color TEXT       = new Color(226, 232, 240);
    private static final Color MUTED      = new Color(100, 116, 139);
    private static final Color INDIGO     = new Color(79, 70, 229);
    private static final Color GREEN      = new Color(34, 197, 94);
    private static final Color AMBER      = new Color(245, 158, 11);
    private static final Color RED        = new Color(239, 68, 68);
    private static final Color TABLE_ALT  = new Color(30, 41, 59);

    public DashboardPanel() {
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Main content panel
        JPanel content = new JPanel();
        content.setBackground(BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Page title
        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Overview of your inventory");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Metric cards row
        JPanel metricsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        metricsRow.setBackground(BG);
        metricsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        metricsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalProductsLabel  = new JLabel("0");
        lowStockLabel       = new JLabel("0");
        todaySalesLabel     = new JLabel("₹0");
        monthlySalesLabel   = new JLabel("₹0");

        metricsRow.add(createMetricCard("Total Products",    totalProductsLabel, INDIGO));
        metricsRow.add(createMetricCard("Low Stock Alerts",  lowStockLabel,      AMBER));
        metricsRow.add(createMetricCard("Today's Sales",     todaySalesLabel,    GREEN));
        metricsRow.add(createMetricCard("Monthly Revenue",   monthlySalesLabel,  INDIGO));

        // Bottom tables row
        JPanel tablesRow = new JPanel(new GridLayout(1, 2, 14, 0));
        tablesRow.setBackground(BG);
        tablesRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        tablesRow.add(createRecentSalesPanel());
        tablesRow.add(createLowStockPanel());

        content.add(title);
        content.add(Box.createVerticalStrut(4));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(20));
        content.add(metricsRow);
        content.add(Box.createVerticalStrut(20));
        content.add(tablesRow);

        add(content, BorderLayout.NORTH);

        // Load real data
        loadData();
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Accent bar at top
        JPanel accent = new JPanel();
        accent.setBackground(accentColor);
        accent.setPreferredSize(new Dimension(0, 3));
        accent.setMaximumSize(new Dimension(Integer.MAX_VALUE, 3));
        accent.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setForeground(MUTED);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(new Font("Arial", Font.BOLD, 26));
        valueLabel.setForeground(TEXT);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(accent);
        card.add(Box.createVerticalStrut(12));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(valueLabel);

        return card;
    }

    private JPanel createRecentSalesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = new JLabel("Recent Sales");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        String[] columns = {"Product", "Qty", "Total", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        recentSalesTable = createStyledTable(model);

        JScrollPane scroll = new JScrollPane(recentSalesTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(CARD_BG);
        scroll.getViewport().setBackground(CARD_BG);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLowStockPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = new JLabel("Low Stock Items");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        String[] columns = {"Product", "Category", "Qty", "Limit"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        lowStockTable = createStyledTable(model);

        JScrollPane scroll = new JScrollPane(lowStockTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(CARD_BG);
        scroll.getViewport().setBackground(CARD_BG);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(CARD_BG);
        table.setForeground(TEXT);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setBackground(new Color(30, 41, 59));
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
        table.setSelectionBackground(INDIGO);
        table.setSelectionForeground(Color.WHITE);

        // Alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBackground(sel ? INDIGO : (row % 2 == 0 ? CARD_BG : TABLE_ALT));
                setForeground(sel ? Color.WHITE : TEXT);
                setFont(new Font("Arial", Font.PLAIN, 13));
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        return table;
    }

    public void loadData() {
        // Load metric values
        int totalProducts = productDAO.getTotalProducts();
        int lowStock      = productDAO.getLowStockProducts().size();
        double today      = saleDAO.getTodaySalesTotal();
        double monthly    = saleDAO.getMonthlySalesTotal();

        totalProductsLabel.setText(String.valueOf(totalProducts));
        lowStockLabel.setText(String.valueOf(lowStock));
        todaySalesLabel.setText(String.format("₹%.0f", today));
        monthlySalesLabel.setText(String.format("₹%.0f", monthly));

        // Load recent sales table
        DefaultTableModel salesModel = (DefaultTableModel) recentSalesTable.getModel();
        salesModel.setRowCount(0);
        List<Sale> recentSales = saleDAO.getRecentSales(8);
        for (Sale s : recentSales) {
            salesModel.addRow(new Object[]{
                s.getProductName(),
                s.getQuantity(),
                String.format("₹%.0f", s.getTotal()),
                s.getSaleDate()
            });
        }

        // Load low stock table
        DefaultTableModel stockModel = (DefaultTableModel) lowStockTable.getModel();
        stockModel.setRowCount(0);
        List<Product> lowStockProducts = productDAO.getLowStockProducts();
        for (Product p : lowStockProducts) {
            stockModel.addRow(new Object[]{
                p.getName(),
                p.getCategory(),
                p.getQuantity(),
                p.getLowStockLimit()
            });
        }
    }
}