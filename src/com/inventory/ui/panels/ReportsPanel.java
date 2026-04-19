package com.inventory.ui.panels;

import com.inventory.dao.ProductDAO;
import com.inventory.dao.SaleDAO;
import com.inventory.dao.StockDAO;
import com.inventory.model.Product;
import com.inventory.model.Sale;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class ReportsPanel extends JPanel {

    private ProductDAO productDAO = new ProductDAO();
    private SaleDAO saleDAO       = new SaleDAO();
    private StockDAO stockDAO     = new StockDAO();

    private static final Color BG        = new Color(26, 26, 46);
    private static final Color CARD_BG   = new Color(22, 33, 62);
    private static final Color BORDER    = new Color(42, 42, 74);
    private static final Color TEXT      = new Color(226, 232, 240);
    private static final Color MUTED     = new Color(100, 116, 139);
    private static final Color INDIGO    = new Color(79, 70, 229);
    private static final Color GREEN     = new Color(34, 197, 94);
    private static final Color AMBER     = new Color(245, 158, 11);
    private static final Color RED       = new Color(239, 68, 68);
    private static final Color TABLE_ALT = new Color(30, 41, 59);

    public ReportsPanel() {
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JScrollPane mainScroll = new JScrollPane(buildContent());
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.getViewport().setBackground(BG);
        mainScroll.setBackground(BG);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(mainScroll, BorderLayout.CENTER);
    }

    private JPanel buildContent() {
        JPanel content = new JPanel();
        content.setBackground(BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Title
        JLabel title = new JLabel("Reports & Analytics");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Overview of sales, stock and product performance");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Summary cards row
        JPanel summaryRow = new JPanel(new GridLayout(1, 4, 14, 0));
        summaryRow.setBackground(BG);
        summaryRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        summaryRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        int totalProducts   = productDAO.getTotalProducts();
        int lowStockCount   = productDAO.getLowStockProducts().size();
        double todaySales   = saleDAO.getTodaySalesTotal();
        double monthlySales = saleDAO.getMonthlySalesTotal();
        int totalSalesCount = saleDAO.getAllSales().size();

        summaryRow.add(createSummaryCard("Total Products",   String.valueOf(totalProducts),             INDIGO));
        summaryRow.add(createSummaryCard("Low Stock Items",  String.valueOf(lowStockCount),             AMBER));
        summaryRow.add(createSummaryCard("Today Revenue",    String.format("₹%.0f", todaySales),       GREEN));
        summaryRow.add(createSummaryCard("Monthly Revenue",  String.format("₹%.0f", monthlySales),     INDIGO));

        // Charts row
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 14, 0));
        chartsRow.setBackground(BG);
        chartsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        chartsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        chartsRow.add(createSalesBarChart());
        chartsRow.add(createTopProductsChart());

        // Tables row
        JPanel tablesRow = new JPanel(new GridLayout(1, 2, 14, 0));
        tablesRow.setBackground(BG);
        tablesRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        tablesRow.add(createTopSellingTable());
        tablesRow.add(createLowStockTable());

        content.add(title);
        content.add(Box.createVerticalStrut(4));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(20));
        content.add(summaryRow);
        content.add(Box.createVerticalStrut(16));
        content.add(chartsRow);
        content.add(Box.createVerticalStrut(16));
        content.add(tablesRow);
        content.add(Box.createVerticalStrut(24));

        return content;
    }

    // Summary metric card
    private JPanel createSummaryCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JPanel accent = new JPanel();
        accent.setBackground(accentColor);
        accent.setPreferredSize(new Dimension(0, 3));
        accent.setMaximumSize(new Dimension(Integer.MAX_VALUE, 3));
        accent.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setForeground(MUTED);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
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

    // Sales bar chart (last 7 days)
    private JPanel createSalesBarChart() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = new JLabel("Sales — Last 7 Days");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        // Build data for last 7 days
        Map<String, Double> salesData = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).toString();
            double total = saleDAO.getSalesByDate(date).stream()
                .mapToDouble(Sale::getTotal).sum();
            String dayLabel = LocalDate.now().minusDays(i).getDayOfWeek()
                .toString().substring(0, 3);
            salesData.put(dayLabel, total);
        }

        JPanel chart = new BarChartPanel(salesData, INDIGO, "₹");
        chart.setBackground(CARD_BG);

        card.add(title, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);

        return card;
    }

    // Top products bar chart
    private JPanel createTopProductsChart() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = new JLabel("Top Products by Sales Quantity");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        // Count sales per product
        Map<String, Double> productData = new LinkedHashMap<>();
        List<Sale> allSales = saleDAO.getAllSales();
        Map<String, Integer> countMap = new HashMap<>();
        for (Sale s : allSales) {
            countMap.merge(s.getProductName(), s.getQuantity(), Integer::sum);
        }
        countMap.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .forEach(e -> productData.put(
                e.getKey().length() > 8 ? e.getKey().substring(0, 8) + ".." : e.getKey(),
                e.getValue().doubleValue()
            ));

        JPanel chart = new BarChartPanel(productData, GREEN, "");
        chart.setBackground(CARD_BG);

        card.add(title, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);

        return card;
    }

    // Top selling products table
    private JPanel createTopSellingTable() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = new JLabel("Top Selling Products");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        String[] columns = {"Product", "Total Qty Sold", "Total Revenue"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        // Aggregate sales data
        List<Sale> allSales = saleDAO.getAllSales();
        Map<String, int[]> aggregated = new LinkedHashMap<>();
        for (Sale s : allSales) {
            aggregated.computeIfAbsent(s.getProductName(), k -> new int[]{0, 0});
            aggregated.get(s.getProductName())[0] += s.getQuantity();
            aggregated.get(s.getProductName())[1] += (int) s.getTotal();
        }
        aggregated.entrySet().stream()
            .sorted((a, b) -> b.getValue()[0] - a.getValue()[0])
            .limit(8)
            .forEach(e -> model.addRow(new Object[]{
                e.getKey(),
                e.getValue()[0],
                String.format("₹%,d", e.getValue()[1])
            }));

        JTable table = createStyledTable(model);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(CARD_BG);

        card.add(title,  BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // Low stock table
    private JPanel createLowStockTable() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = new JLabel("Low Stock Alert");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(RED);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        String[] columns = {"Product", "Category", "Current Qty", "Min Limit"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Product> lowStock = productDAO.getLowStockProducts();
        for (Product p : lowStock) {
            model.addRow(new Object[]{
                p.getName(), p.getCategory(),
                p.getQuantity(), p.getLowStockLimit()
            });
        }

        JTable table = createStyledTable(model);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBackground(sel ? INDIGO : (row % 2 == 0 ? CARD_BG : TABLE_ALT));
                setForeground(col == 2 ? AMBER : (sel ? Color.WHITE : TEXT));
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(CARD_BG);

        card.add(title,  BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        return card;
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
        table.setSelectionBackground(INDIGO);
        table.setSelectionForeground(Color.WHITE);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBackground(sel ? INDIGO : (row % 2 == 0 ? CARD_BG : TABLE_ALT));
                setForeground(sel ? Color.WHITE : TEXT);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
        return table;
    }

    // Inner class for bar chart
    class BarChartPanel extends JPanel {
        private Map<String, Double> data;
        private Color barColor;
        private String prefix;

        public BarChartPanel(Map<String, Double> data, Color barColor, String prefix) {
            this.data = data;
            this.barColor = barColor;
            this.prefix = prefix;
            setBackground(CARD_BG);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (data == null || data.isEmpty()) {
                g2d.setColor(MUTED);
                g2d.setFont(new Font("Arial", Font.PLAIN, 13));
                g2d.drawString("No data available", getWidth() / 2 - 50, getHeight() / 2);
                return;
            }

            int width    = getWidth();
            int height   = getHeight();
            int padding  = 40;
            int barCount = data.size();
            int barWidth = (width - padding * 2) / barCount - 10;

            double maxVal = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
            if (maxVal == 0) maxVal = 1;

            int i = 0;
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                double val    = entry.getValue();
                int barHeight = (int) ((val / maxVal) * (height - padding * 2));
                int x         = padding + i * ((width - padding * 2) / barCount) + 5;
                int y         = height - padding - barHeight;

                // Bar
                g2d.setColor(barColor);
                g2d.fill(new RoundRectangle2D.Float(x, y, barWidth, barHeight, 6, 6));

                // Value label
                g2d.setColor(TEXT);
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                String valStr = val > 0 ? prefix + (int) val : "";
                int strWidth  = g2d.getFontMetrics().stringWidth(valStr);
                g2d.drawString(valStr, x + barWidth / 2 - strWidth / 2, y - 4);

                // Day/name label
                g2d.setColor(MUTED);
                g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                String key      = entry.getKey();
                int keyWidth    = g2d.getFontMetrics().stringWidth(key);
                g2d.drawString(key, x + barWidth / 2 - keyWidth / 2, height - padding + 14);

                i++;
            }

            // Baseline
            g2d.setColor(BORDER);
            g2d.drawLine(padding, height - padding, width - padding, height - padding);
        }
    }
}