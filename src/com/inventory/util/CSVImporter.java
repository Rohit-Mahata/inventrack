package com.inventory.util;

import com.inventory.dao.ProductDAO;
import com.inventory.model.Product;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CSVImporter {

    public static void importProducts(JFrame parent) {
        // Color scheme
        final Color BG      = new Color(26, 26, 46);
        final Color CARD_BG = new Color(22, 33, 62);
        final Color TEXT    = new Color(226, 232, 240);
        final Color MUTED   = new Color(100, 116, 139);
        final Color INDIGO  = new Color(79, 70, 229);
        final Color GREEN   = new Color(34, 197, 94);
        final Color RED     = new Color(239, 68, 68);

        // Open file chooser for CSV
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select CSV File");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        int result = chooser.showOpenDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) return;

        String filePath = chooser.getSelectedFile().getAbsolutePath();

        List<Product> validProducts = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Read and parse CSV
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                // Skip header row
                if (lineNumber == 1) continue;

                // Skip empty lines
                if (line.trim().isEmpty()) continue;

                String[] columns = line.split(",");

                // Validate column count
                if (columns.length != 5) {
                    errors.add("Row " + lineNumber + ": wrong number of columns");
                    continue;
                }

                String name = columns[0].trim();
                String category = columns[1].trim();
                String qtyStr = columns[2].trim();
                String priceStr = columns[3].trim();
                String limitStr = columns[4].trim();

                // Validate name
                if (name.isEmpty()) {
                    errors.add("Row " + lineNumber + ": name cannot be empty");
                    continue;
                }

                // Validate quantity
                int quantity;
                try {
                    quantity = Integer.parseInt(qtyStr);
                } catch (NumberFormatException e) {
                    errors.add("Row " + lineNumber + ": quantity must be a number");
                    continue;
                }

                // Validate price
                double price;
                try {
                    price = Double.parseDouble(priceStr);
                } catch (NumberFormatException e) {
                    errors.add("Row " + lineNumber + ": price must be a number");
                    continue;
                }

                // Validate lowStockLimit
                int lowStockLimit;
                try {
                    lowStockLimit = Integer.parseInt(limitStr);
                } catch (NumberFormatException e) {
                    errors.add("Row " + lineNumber + ": lowStockLimit must be a number");
                    continue;
                }

                Product p = new Product();
                p.setName(name);
                p.setCategory(category);
                p.setQuantity(quantity);
                p.setPrice(price);
                p.setLowStockLimit(lowStockLimit);
                validProducts.add(p);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                "Error reading CSV file: " + e.getMessage(),
                "Import Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Show preview dialog
        JDialog previewDialog = new JDialog(parent, "CSV Import Preview", true);
        previewDialog.setSize(700, 500);
        previewDialog.setLocationRelativeTo(parent);
        previewDialog.getContentPane().setBackground(BG);
        previewDialog.setLayout(new BorderLayout());

        // Top info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(BG);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));

        JLabel titleLabel = new JLabel("CSV Import Preview");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel validLabel = new JLabel("Valid rows: " + validProducts.size());
        validLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        validLabel.setForeground(GREEN);
        validLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel invalidLabel = new JLabel("Invalid rows: " + errors.size());
        invalidLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        invalidLabel.setForeground(errors.isEmpty() ? MUTED : RED);
        invalidLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(validLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(invalidLabel);

        // Show errors if any
        if (!errors.isEmpty()) {
            JTextArea errorArea = new JTextArea();
            errorArea.setBackground(CARD_BG);
            errorArea.setForeground(RED);
            errorArea.setFont(new Font("Arial", Font.PLAIN, 11));
            errorArea.setEditable(false);
            errorArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            StringBuilder sb = new StringBuilder();
            for (String err : errors) {
                sb.append(err).append("\n");
            }
            errorArea.setText(sb.toString());

            JScrollPane errorScroll = new JScrollPane(errorArea);
            errorScroll.setPreferredSize(new Dimension(0, 80));
            errorScroll.setBorder(BorderFactory.createLineBorder(new Color(42, 42, 74), 1));
            errorScroll.getViewport().setBackground(CARD_BG);
            errorScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel errTitle = new JLabel("Errors:");
            errTitle.setFont(new Font("Arial", Font.BOLD, 12));
            errTitle.setForeground(RED);
            errTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

            infoPanel.add(Box.createVerticalStrut(8));
            infoPanel.add(errTitle);
            infoPanel.add(Box.createVerticalStrut(4));
            infoPanel.add(errorScroll);
        }

        // Preview table
        String[] columns = {"Name", "Category", "Quantity", "Price", "Low Stock Limit"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Product p : validProducts) {
            tableModel.addRow(new Object[]{
                p.getName(), p.getCategory(), p.getQuantity(),
                String.format("%.2f", p.getPrice()), p.getLowStockLimit()
            });
        }

        JTable previewTable = new JTable(tableModel);
        previewTable.setBackground(CARD_BG);
        previewTable.setForeground(TEXT);
        previewTable.setFont(new Font("Arial", Font.PLAIN, 13));
        previewTable.setRowHeight(34);
        previewTable.setShowGrid(false);
        previewTable.setIntercellSpacing(new Dimension(0, 0));
        previewTable.getTableHeader().setBackground(new Color(30, 41, 59));
        previewTable.getTableHeader().setForeground(MUTED);
        previewTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        previewTable.setSelectionBackground(INDIGO);
        previewTable.setSelectionForeground(Color.WHITE);

        Color tableAlt = new Color(30, 41, 59);
        previewTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBackground(sel ? INDIGO : (row % 2 == 0 ? CARD_BG : tableAlt));
                setForeground(sel ? Color.WHITE : TEXT);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        JScrollPane tableScroll = new JScrollPane(previewTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(42, 42, 74), 1));
        tableScroll.getViewport().setBackground(CARD_BG);
        tableScroll.setBackground(CARD_BG);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(BG);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        tablePanel.add(tableScroll, BorderLayout.CENTER);

        // Bottom buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BG);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(MUTED);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 12));
        cancelBtn.setBorderPainted(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        cancelBtn.addActionListener(e -> previewDialog.dispose());

        JButton importBtn = new JButton("Import");
        importBtn.setBackground(INDIGO);
        importBtn.setForeground(Color.WHITE);
        importBtn.setFont(new Font("Arial", Font.BOLD, 12));
        importBtn.setBorderPainted(false);
        importBtn.setFocusPainted(false);
        importBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        importBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        if (validProducts.isEmpty()) {
            importBtn.setEnabled(false);
            importBtn.setToolTipText("No valid products to import");
        }

        importBtn.addActionListener(e -> {
            ProductDAO productDAO = new ProductDAO();
            int inserted = 0;
            int merged = 0;
            for (Product p : validProducts) {
                Product match = productDAO.findExactMatch(p.getName(), p.getPrice(), p.getLowStockLimit());
                if (match != null) {
                    if (productDAO.addQuantityToExisting(match.getId(), p.getQuantity())) merged++;
                } else {
                    if (productDAO.addProduct(p)) inserted++;
                }
            }
            previewDialog.dispose();
            JOptionPane.showMessageDialog(parent,
                "Import complete!\nNew products: " + inserted + "\nMerged (quantity added): " + merged,
                "Import Complete", JOptionPane.INFORMATION_MESSAGE);
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(importBtn);

        previewDialog.add(infoPanel, BorderLayout.NORTH);
        previewDialog.add(tablePanel, BorderLayout.CENTER);
        previewDialog.add(buttonPanel, BorderLayout.SOUTH);
        previewDialog.setVisible(true);
    }
}
