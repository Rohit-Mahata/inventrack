package com.inventory.ui.panels;

import com.inventory.dao.ProductDAO;
import com.inventory.model.Product;
import com.inventory.util.PDFExporter;
import com.inventory.util.CSVImporter;
import com.inventory.util.SessionManager;
import java.awt.Frame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ProductPanel extends JPanel {

    private ProductDAO productDAO = new ProductDAO();
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JScrollPane scrollPane;

    // Select mode state
    private boolean selectMode = false;
    private JButton selectToggleBtn;
    private JPanel bottomBar;
    private JPanel normalBottomBar;

    // Bottom bar controls for select mode
    private JButton markAllBtn;
    private JButton deleteMarkedBtn;
    private JButton exportMarkedBtn;
    private JLabel markedCountLabel;

    private static final Color BG        = new Color(26, 26, 46);
    private static final Color CARD_BG   = new Color(22, 33, 62);
    private static final Color BORDER    = new Color(42, 42, 74);
    private static final Color TEXT      = new Color(226, 232, 240);
    private static final Color MUTED     = new Color(100, 116, 139);
    private static final Color INDIGO    = new Color(79, 70, 229);
    private static final Color GREEN     = new Color(34, 197, 94);
    private static final Color RED       = new Color(239, 68, 68);
    private static final Color TABLE_ALT = new Color(30, 41, 59);
    private static final Color AMBER     = new Color(245, 158, 11);

    public ProductPanel() {
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // ============ TOP SECTION ============
        JPanel topSection = new JPanel();
        topSection.setBackground(BG);
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        // Row 1: Title left
        JPanel row1 = new JPanel(new BorderLayout());
        row1.setBackground(BG);
        row1.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(BG);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Products");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Manage your product inventory");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        titlePanel.add(title);
        titlePanel.add(subtitle);
        row1.add(titlePanel, BorderLayout.WEST);

        // Row 2: Search + all action buttons
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.setBackground(BG);

        searchField = new JTextField(14);
        searchField.setBackground(CARD_BG);
        searchField.setForeground(TEXT);
        searchField.setCaretColor(TEXT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        searchField.setFont(new Font("Arial", Font.PLAIN, 13));

        JButton searchBtn    = createButton("Search", INDIGO);
        JButton addBtn       = createButton("+ Add", GREEN);
        JButton exportBtn    = createButton("Export PDF", new Color(220, 38, 38));
        JButton importCsvBtn = createButton("Import CSV", new Color(37, 99, 235));
        selectToggleBtn      = createButton("Select", AMBER);

        searchBtn.addActionListener(e -> searchProducts());
        addBtn.addActionListener(e -> showProductDialog(null));
        exportBtn.addActionListener(e -> PDFExporter.exportProducts());
        importCsvBtn.addActionListener(e -> {
            CSVImporter.importProducts((JFrame) SwingUtilities.getWindowAncestor(this));
            loadProducts();
        });
        selectToggleBtn.addActionListener(e -> toggleSelectMode());

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(MUTED);
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        row2.add(searchLabel);
        row2.add(searchField);
        row2.add(searchBtn);
        row2.add(createSeparatorLabel());
        row2.add(exportBtn);
        row2.add(importCsvBtn);
        row2.add(createSeparatorLabel());
        row2.add(selectToggleBtn);
        row2.add(addBtn);

        topSection.add(row1);
        topSection.add(row2);

        // ============ TABLE ============
        buildTable();
        scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.setBackground(CARD_BG);

        // ============ NORMAL BOTTOM BAR (edit/delete/refresh) ============
        normalBottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        normalBottomBar.setBackground(BG);
        normalBottomBar.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        normalBottomBar.setPreferredSize(new Dimension(0, 48));

        JButton editBtn    = createButton("Edit Selected", INDIGO);
        JButton deleteBtn  = createButton("Delete Selected", RED);
        JButton refreshBtn = createButton("Refresh", MUTED);

        if (!SessionManager.isAdmin()) {
            editBtn.setEnabled(false);
            editBtn.setToolTipText("Admin only");
            deleteBtn.setEnabled(false);
            deleteBtn.setToolTipText("Admin only");
        }

        editBtn.addActionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                Product p = productDAO.getProductById(id);
                showProductDialog(p);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a product to edit.");
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                String name = (String) tableModel.getValueAt(row, 1);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete product: " + name + "?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    productDAO.deleteProduct(id);
                    loadProducts();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a product to delete.");
            }
        });

        refreshBtn.addActionListener(e -> loadProducts());

        normalBottomBar.add(refreshBtn);
        normalBottomBar.add(editBtn);
        normalBottomBar.add(deleteBtn);

        // ============ SELECT MODE BOTTOM BAR ============
        bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(BG);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        bottomBar.setPreferredSize(new Dimension(0, 48));
        bottomBar.setVisible(false);

        JPanel leftBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        leftBar.setBackground(BG);

        markedCountLabel = new JLabel("0 marked");
        markedCountLabel.setForeground(TEXT);
        markedCountLabel.setFont(new Font("Arial", Font.BOLD, 12));

        markAllBtn       = createButton("Mark All", INDIGO);
        deleteMarkedBtn  = createButton("Delete Marked", RED);
        exportMarkedBtn  = createButton("Export Marked", new Color(37, 99, 235));

        deleteMarkedBtn.setEnabled(false);
        exportMarkedBtn.setEnabled(false);

        markAllBtn.addActionListener(e -> toggleMarkAll());

        deleteMarkedBtn.addActionListener(e -> {
            List<Integer> ids = getMarkedProductIds();
            if (ids.isEmpty()) return;
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete " + ids.size() + " marked product(s)?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                for (int id : ids) productDAO.deleteProduct(id);
                loadProducts();
            }
        });

        exportMarkedBtn.addActionListener(e -> {
            List<Product> marked = getMarkedProducts();
            if (!marked.isEmpty()) PDFExporter.exportProductList(marked);
        });

        if (!SessionManager.isAdmin()) {
            deleteMarkedBtn.setEnabled(false);
            deleteMarkedBtn.setToolTipText("Admin only");
        }

        leftBar.add(markAllBtn);
        leftBar.add(markedCountLabel);
        leftBar.add(deleteMarkedBtn);
        leftBar.add(exportMarkedBtn);

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        rightBar.setBackground(BG);
        JButton cancelBtn = createButton("Done", GREEN);
        cancelBtn.addActionListener(e -> toggleSelectMode());
        rightBar.add(cancelBtn);

        bottomBar.add(leftBar, BorderLayout.WEST);
        bottomBar.add(rightBar, BorderLayout.EAST);

        // ============ WRAPPER for both bottom bars ============
        JPanel bottomWrapper = new JPanel(new CardLayout());
        bottomWrapper.setBackground(BG);
        bottomWrapper.setPreferredSize(new Dimension(0, 48));
        bottomWrapper.add(normalBottomBar, "normal");
        bottomWrapper.add(bottomBar, "select");

        // ============ ASSEMBLE ============
        add(topSection,     BorderLayout.NORTH);
        add(scrollPane,     BorderLayout.CENTER);
        add(bottomWrapper,  BorderLayout.SOUTH);

        loadProducts();
    }

    private JLabel createSeparatorLabel() {
        JLabel sep = new JLabel("|");
        sep.setForeground(BORDER);
        sep.setFont(new Font("Arial", Font.PLAIN, 14));
        return sep;
    }

    private void buildTable() {
        if (selectMode) {
            String[] cols = {"✓", "ID", "Name", "Category", "Quantity", "Price (₹)", "Low Stock Limit"};
            tableModel = new DefaultTableModel(cols, 0) {
                @Override public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : Object.class; }
                @Override public boolean isCellEditable(int r, int c) { return c == 0; }
            };
        } else {
            String[] cols = {"ID", "Name", "Category", "Quantity", "Price (₹)", "Low Stock Limit"};
            tableModel = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
        }

        if (productTable == null) {
            productTable = new JTable(tableModel);
        } else {
            productTable.setModel(tableModel);
        }

        productTable.setBackground(CARD_BG);
        productTable.setForeground(TEXT);
        productTable.setFont(new Font("Arial", Font.PLAIN, 13));
        productTable.setRowHeight(38);
        productTable.setShowGrid(false);
        productTable.setIntercellSpacing(new Dimension(0, 0));
        productTable.getTableHeader().setBackground(new Color(30, 41, 59));
        productTable.getTableHeader().setForeground(MUTED);
        productTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        productTable.setSelectionBackground(INDIGO);
        productTable.setSelectionForeground(Color.WHITE);

        int idCol = selectMode ? 1 : 0;
        if (selectMode) {
            productTable.getColumnModel().getColumn(0).setPreferredWidth(35);
            productTable.getColumnModel().getColumn(0).setMaxWidth(45);
        }
        productTable.getColumnModel().getColumn(idCol).setPreferredWidth(40);
        productTable.getColumnModel().getColumn(idCol).setMaxWidth(60);

        // Renderer
        productTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (sel) {
                    setBackground(INDIGO);
                    setForeground(Color.WHITE);
                } else if (selectMode && (boolean) tableModel.getValueAt(row, 0)) {
                    setBackground(new Color(55, 48, 163));
                    setForeground(TEXT);
                } else {
                    setBackground(row % 2 == 0 ? CARD_BG : TABLE_ALT);
                    setForeground(TEXT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        if (selectMode) {
            tableModel.addTableModelListener(e -> {
                if (e.getColumn() == 0 || e.getColumn() == -1) updateMarkedState();
            });
        }

        // Double-click to edit
        for (var ml : productTable.getMouseListeners()) {
            if (ml instanceof java.awt.event.MouseAdapter) {
                productTable.removeMouseListener(ml);
            }
        }
        productTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (selectMode && productTable.columnAtPoint(e.getPoint()) == 0) return;
                if (e.getClickCount() == 2) {
                    int row = productTable.getSelectedRow();
                    if (row >= 0) {
                        int idColIdx = selectMode ? 1 : 0;
                        int id = (int) tableModel.getValueAt(row, idColIdx);
                        Product p = productDAO.getProductById(id);
                        showProductDialog(p);
                    }
                }
            }
        });
    }

    private void toggleSelectMode() {
        selectMode = !selectMode;
        selectToggleBtn.setText(selectMode ? "Cancel Select" : "Select");
        selectToggleBtn.setBackground(selectMode ? MUTED : AMBER);

        normalBottomBar.setVisible(!selectMode);
        bottomBar.setVisible(selectMode);

        // Find the CardLayout parent and switch
        Container bottomWrapper = normalBottomBar.getParent();
        if (bottomWrapper != null && bottomWrapper.getLayout() instanceof CardLayout cl) {
            cl.show(bottomWrapper, selectMode ? "select" : "normal");
        }

        buildTable();
        loadProducts();
        if (selectMode) updateMarkedState();
        revalidate();
        repaint();
    }

    private void toggleMarkAll() {
        boolean allMarked = isAllMarked();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(!allMarked, i, 0);
        }
    }

    private boolean isAllMarked() {
        if (tableModel.getRowCount() == 0) return false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (!(boolean) tableModel.getValueAt(i, 0)) return false;
        }
        return true;
    }

    private void updateMarkedState() {
        int count = getMarkedCount();
        markedCountLabel.setText(count + " marked");
        deleteMarkedBtn.setEnabled(count > 0 && SessionManager.isAdmin());
        exportMarkedBtn.setEnabled(count > 0);
        markAllBtn.setText(isAllMarked() ? "Unmark All" : "Mark All");
        productTable.repaint();
    }

    private int getMarkedCount() {
        int c = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++)
            if ((boolean) tableModel.getValueAt(i, 0)) c++;
        return c;
    }

    private List<Integer> getMarkedProductIds() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++)
            if ((boolean) tableModel.getValueAt(i, 0))
                ids.add((int) tableModel.getValueAt(i, 1));
        return ids;
    }

    private List<Product> getMarkedProducts() {
        List<Product> list = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ((boolean) tableModel.getValueAt(i, 0)) {
                Product p = productDAO.getProductById((int) tableModel.getValueAt(i, 1));
                if (p != null) list.add(p);
            }
        }
        return list;
    }

    private void showProductDialog(Product existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            isEdit ? "Edit Product" : "Add Product", true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(CARD_BG);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setBackground(CARD_BG);
        form.setLayout(new GridLayout(6, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JTextField nameField     = createFormField(isEdit ? existing.getName() : "");
        JTextField categoryField = createFormField(isEdit ? existing.getCategory() : "");
        JTextField qtyField      = createFormField(isEdit ? String.valueOf(existing.getQuantity()) : "");
        JTextField priceField    = createFormField(isEdit ? String.valueOf(existing.getPrice()) : "");
        JTextField limitField    = createFormField(isEdit ? String.valueOf(existing.getLowStockLimit()) : "10");

        form.add(createFormLabel("Name:"));            form.add(nameField);
        form.add(createFormLabel("Category:"));        form.add(categoryField);
        form.add(createFormLabel("Quantity:"));        form.add(qtyField);
        form.add(createFormLabel("Price (₹):"));       form.add(priceField);
        form.add(createFormLabel("Low Stock Limit:")); form.add(limitField);

        JButton saveBtn = createButton(isEdit ? "Update" : "Save", INDIGO);
        saveBtn.addActionListener(e -> {
            try {
                Product p = new Product();
                if (isEdit) p.setId(existing.getId());
                p.setName(nameField.getText().trim());
                p.setCategory(categoryField.getText().trim());
                p.setQuantity(Integer.parseInt(qtyField.getText().trim()));
                p.setPrice(Double.parseDouble(priceField.getText().trim()));
                p.setLowStockLimit(Integer.parseInt(limitField.getText().trim()));

                if (isEdit) productDAO.updateProduct(p);
                else        productDAO.addProduct(p);

                loadProducts();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers for quantity, price and limit.");
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(CARD_BG);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 16, 24));
        btnPanel.add(saveBtn);

        form.add(new JLabel());
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public void loadProducts() {
        tableModel.setRowCount(0);
        List<Product> products = productDAO.getAllProducts();
        for (Product p : products) {
            if (selectMode) {
                tableModel.addRow(new Object[]{ false,
                    p.getId(), p.getName(), p.getCategory(),
                    p.getQuantity(), String.format("₹%.2f", p.getPrice()),
                    p.getLowStockLimit()
                });
            } else {
                tableModel.addRow(new Object[]{
                    p.getId(), p.getName(), p.getCategory(),
                    p.getQuantity(), String.format("₹%.2f", p.getPrice()),
                    p.getLowStockLimit()
                });
            }
        }
        if (selectMode) updateMarkedState();
    }

    private void searchProducts() {
        String keyword = searchField.getText().trim();
        tableModel.setRowCount(0);
        List<Product> products = keyword.isEmpty()
            ? productDAO.getAllProducts()
            : productDAO.searchProducts(keyword);
        for (Product p : products) {
            if (selectMode) {
                tableModel.addRow(new Object[]{ false,
                    p.getId(), p.getName(), p.getCategory(),
                    p.getQuantity(), String.format("₹%.2f", p.getPrice()),
                    p.getLowStockLimit()
                });
            } else {
                tableModel.addRow(new Object[]{
                    p.getId(), p.getName(), p.getCategory(),
                    p.getQuantity(), String.format("₹%.2f", p.getPrice()),
                    p.getLowStockLimit()
                });
            }
        }
        if (selectMode) updateMarkedState();
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return btn;
    }

    private JTextField createFormField(String value) {
        JTextField field = new JTextField(value);
        field.setBackground(new Color(15, 23, 42));
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        return field;
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setFont(new Font("Arial", Font.PLAIN, 13));
        return label;
    }
}