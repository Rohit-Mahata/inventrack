package com.inventory.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.inventory.dao.ProductDAO;
import com.inventory.dao.SaleDAO;
import com.inventory.dao.StockDAO;
import com.inventory.model.Product;
import com.inventory.model.Sale;
import com.inventory.model.StockMovement;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;

public class PDFExporter {

    // Color definitions
    private static Color INDIGO_COLOR = new Color(79, 70, 229);
    private static Color ALT_ROW      = new Color(240, 240, 255);
    private static Color BORDER_COLOR = new Color(200, 200, 220);
    private static Color WHITE_COLOR  = new Color(255, 255, 255);
    private static Color DARK_GRAY    = new Color(50, 50, 50);
    private static Color GREEN_COLOR  = new Color(22, 163, 74);
    private static Color RED_COLOR    = new Color(220, 38, 38);
    private static Color AMBER_COLOR  = new Color(180, 120, 0);
    private static Color GRAY_COLOR   = new Color(128, 128, 128);
    private static Color LIGHT_INDIGO = new Color(238, 238, 255);
    private static Color LIGHT_GRAY   = new Color(192, 192, 192);

    // Font definitions
    private static Font titleFont    = new Font(Font.HELVETICA, 20, Font.BOLD, INDIGO_COLOR);
    private static Font headerFont   = new Font(Font.HELVETICA, 11, Font.BOLD, WHITE_COLOR);
    private static Font cellFont     = new Font(Font.HELVETICA, 10, Font.NORMAL, DARK_GRAY);
    private static Font subtitleFont = new Font(Font.HELVETICA, 10, Font.NORMAL, GRAY_COLOR);

    // =========================================================================
    // EXPORT PRODUCTS
    // =========================================================================
    public static void exportProducts() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Products PDF");
        chooser.setSelectedFile(new File("InvenTrack_Products.pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
        int result = chooser.showSaveDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) return;

        String path = chooser.getSelectedFile().getAbsolutePath();
        if (!path.toLowerCase().endsWith(".pdf")) path += ".pdf";

        try {
            ProductDAO productDAO = new ProductDAO();
            List<Product> products = productDAO.getAllProducts();

            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(doc, new FileOutputStream(path));
            doc.open();

            // Header
            addHeader(doc, "Products List");

            // Summary cards
            int totalProducts = products.size();
            long lowStockCount = products.stream()
                .filter(p -> p.getQuantity() <= p.getLowStockLimit()).count();

            addSummaryRow(doc,
                new String[]{"Total Products", "Low Stock Items"},
                new String[]{String.valueOf(totalProducts), String.valueOf(lowStockCount)});

            // Table
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.5f, 2.5f, 1.5f, 1.2f, 1f, 1f, 1.2f});
            table.setSpacingBefore(10);

            addTableHeader(table, "#", "Name", "Category", "Price", "Quantity", "Low Stock Limit", "Status");

            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                boolean alt = i % 2 != 0;
                boolean isLow = p.getQuantity() <= p.getLowStockLimit();

                table.addCell(dataCell(String.valueOf(i + 1), alt));
                table.addCell(dataCell(p.getName(), alt));
                table.addCell(dataCell(p.getCategory(), alt));
                table.addCell(dataCell(String.format("Rs.%.2f", p.getPrice()), alt));
                table.addCell(dataCell(String.valueOf(p.getQuantity()), alt));
                table.addCell(dataCell(String.valueOf(p.getLowStockLimit()), alt));

                // Status cell colored
                Font statusFont = new Font(Font.HELVETICA, 9, Font.BOLD,
                    isLow ? RED_COLOR : GREEN_COLOR);
                PdfPCell statusCell = new PdfPCell(new Phrase(isLow ? "LOW STOCK" : "OK", statusFont));
                statusCell.setPadding(7);
                statusCell.setBorderColor(BORDER_COLOR);
                statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                if (alt) statusCell.setBackgroundColor(ALT_ROW);
                table.addCell(statusCell);
            }

            doc.add(table);
            addFooter(doc);
            doc.close();

            JOptionPane.showMessageDialog(null,
                "Products PDF exported successfully!\n" + path,
                "Export Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Export failed: " + e.getMessage(),
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // EXPORT SELECTED PRODUCTS
    // =========================================================================
    public static void exportProductList(List<Product> products) {
        if (products == null || products.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                "No products to export.", "Export", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Selected Products PDF");
        chooser.setSelectedFile(new File("InvenTrack_Selected_Products.pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
        int result = chooser.showSaveDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) return;

        String path = chooser.getSelectedFile().getAbsolutePath();
        if (!path.toLowerCase().endsWith(".pdf")) path += ".pdf";

        try {
            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(doc, new FileOutputStream(path));
            doc.open();

            // Header
            addHeader(doc, "Selected Products (" + products.size() + " items)");

            // Summary cards
            long lowStockCount = products.stream()
                .filter(p -> p.getQuantity() <= p.getLowStockLimit()).count();

            addSummaryRow(doc,
                new String[]{"Selected Products", "Low Stock Items"},
                new String[]{String.valueOf(products.size()), String.valueOf(lowStockCount)});

            // Table
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.5f, 2.5f, 1.5f, 1.2f, 1f, 1f, 1.2f});
            table.setSpacingBefore(10);

            addTableHeader(table, "#", "Name", "Category", "Price", "Quantity", "Low Stock Limit", "Status");

            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                boolean alt = i % 2 != 0;
                boolean isLow = p.getQuantity() <= p.getLowStockLimit();

                table.addCell(dataCell(String.valueOf(i + 1), alt));
                table.addCell(dataCell(p.getName(), alt));
                table.addCell(dataCell(p.getCategory(), alt));
                table.addCell(dataCell(String.format("Rs.%.2f", p.getPrice()), alt));
                table.addCell(dataCell(String.valueOf(p.getQuantity()), alt));
                table.addCell(dataCell(String.valueOf(p.getLowStockLimit()), alt));

                Font statusFont = new Font(Font.HELVETICA, 9, Font.BOLD,
                    isLow ? RED_COLOR : GREEN_COLOR);
                PdfPCell statusCell = new PdfPCell(new Phrase(isLow ? "LOW STOCK" : "OK", statusFont));
                statusCell.setPadding(7);
                statusCell.setBorderColor(BORDER_COLOR);
                statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                if (alt) statusCell.setBackgroundColor(ALT_ROW);
                table.addCell(statusCell);
            }

            doc.add(table);
            addFooter(doc);
            doc.close();

            JOptionPane.showMessageDialog(null,
                "Selected products PDF exported successfully!\n" + path,
                "Export Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Export failed: " + e.getMessage(),
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // EXPORT SALES
    // =========================================================================
    public static void exportSales() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Sales PDF");
        chooser.setSelectedFile(new File("InvenTrack_Sales.pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
        int result = chooser.showSaveDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) return;

        String path = chooser.getSelectedFile().getAbsolutePath();
        if (!path.toLowerCase().endsWith(".pdf")) path += ".pdf";

        try {
            SaleDAO saleDAO = new SaleDAO();
            List<Sale> sales = saleDAO.getAllSales();

            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(doc, new FileOutputStream(path));
            doc.open();

            // Header
            addHeader(doc, "Sales History");

            // Summary cards
            int totalSales = sales.size();
            double totalRevenue = sales.stream().mapToDouble(Sale::getTotal).sum();
            double todayRevenue = saleDAO.getTodaySalesTotal();
            double monthlyRevenue = saleDAO.getMonthlySalesTotal();

            addSummaryRow(doc,
                new String[]{"Total Sales", "Total Revenue", "Today Revenue", "Monthly Revenue"},
                new String[]{String.valueOf(totalSales),
                             String.format("Rs.%.2f", totalRevenue),
                             String.format("Rs.%.2f", todayRevenue),
                             String.format("Rs.%.2f", monthlyRevenue)});

            // Table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.5f, 2.5f, 1f, 1.5f, 2f});
            table.setSpacingBefore(10);

            addTableHeader(table, "#", "Product Name", "Quantity", "Total", "Date");

            for (int i = 0; i < sales.size(); i++) {
                Sale s = sales.get(i);
                boolean alt = i % 2 != 0;
                table.addCell(dataCell(String.valueOf(i + 1), alt));
                table.addCell(dataCell(s.getProductName(), alt));
                table.addCell(dataCell(String.valueOf(s.getQuantity()), alt));
                table.addCell(dataCell(String.format("Rs.%.2f", s.getTotal()), alt));
                table.addCell(dataCell(s.getSaleDate(), alt));
            }

            doc.add(table);
            addFooter(doc);
            doc.close();

            JOptionPane.showMessageDialog(null,
                "Sales PDF exported successfully!\n" + path,
                "Export Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Export failed: " + e.getMessage(),
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // EXPORT STOCK MOVEMENTS
    // =========================================================================
    public static void exportStock() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Stock Movements PDF");
        chooser.setSelectedFile(new File("InvenTrack_Stock.pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
        int result = chooser.showSaveDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) return;

        String path = chooser.getSelectedFile().getAbsolutePath();
        if (!path.toLowerCase().endsWith(".pdf")) path += ".pdf";

        try {
            StockDAO stockDAO = new StockDAO();
            List<StockMovement> movements = stockDAO.getAllMovements();

            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(doc, new FileOutputStream(path));
            doc.open();

            // Header
            addHeader(doc, "Stock Movements");

            // Summary cards
            int totalMovements = movements.size();
            int totalIn = movements.stream()
                .filter(m -> m.getType().equals("IN"))
                .mapToInt(StockMovement::getQuantity).sum();
            int totalOut = movements.stream()
                .filter(m -> m.getType().equals("OUT"))
                .mapToInt(StockMovement::getQuantity).sum();

            addSummaryRow(doc,
                new String[]{"Total Movements", "Total IN", "Total OUT"},
                new String[]{String.valueOf(totalMovements),
                             String.valueOf(totalIn),
                             String.valueOf(totalOut)});

            // Table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.5f, 2.5f, 0.8f, 1f, 2f, 1.5f});
            table.setSpacingBefore(10);

            addTableHeader(table, "#", "Product Name", "Type", "Quantity", "Note", "Date");

            for (int i = 0; i < movements.size(); i++) {
                StockMovement m = movements.get(i);
                boolean alt = i % 2 != 0;
                table.addCell(dataCell(String.valueOf(i + 1), alt));
                table.addCell(dataCell(m.getProductName(), alt));

                // Type cell colored
                Font typeFont = new Font(Font.HELVETICA, 10, Font.BOLD,
                    m.getType().equals("IN") ? GREEN_COLOR : RED_COLOR);
                PdfPCell typeCell = new PdfPCell(new Phrase(m.getType(), typeFont));
                typeCell.setPadding(7);
                typeCell.setBorderColor(BORDER_COLOR);
                typeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                if (alt) typeCell.setBackgroundColor(ALT_ROW);
                table.addCell(typeCell);

                table.addCell(dataCell(String.valueOf(m.getQuantity()), alt));
                table.addCell(dataCell(m.getNote(), alt));
                table.addCell(dataCell(m.getMoveDate(), alt));
            }

            doc.add(table);
            addFooter(doc);
            doc.close();

            JOptionPane.showMessageDialog(null,
                "Stock Movements PDF exported successfully!\n" + path,
                "Export Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Export failed: " + e.getMessage(),
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // EXPORT REPORTS
    // =========================================================================
    public static void exportReports() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Report PDF");
        chooser.setSelectedFile(new File("InvenTrack_Report.pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
        int result = chooser.showSaveDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) return;

        String path = chooser.getSelectedFile().getAbsolutePath();
        if (!path.toLowerCase().endsWith(".pdf")) path += ".pdf";

        try {
            ProductDAO productDAO = new ProductDAO();
            SaleDAO saleDAO = new SaleDAO();

            int totalProducts = productDAO.getTotalProducts();
            List<Product> lowStockProducts = productDAO.getLowStockProducts();
            int lowStockCount = lowStockProducts.size();
            double todaySales = saleDAO.getTodaySalesTotal();
            double monthlySales = saleDAO.getMonthlySalesTotal();

            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, new FileOutputStream(path));
            doc.open();

            // Header
            addHeader(doc, "Analytics Report");

            // Summary cards
            addSummaryRow(doc,
                new String[]{"Total Products", "Low Stock Count", "Today Sales", "Monthly Sales"},
                new String[]{String.valueOf(totalProducts),
                             String.valueOf(lowStockCount),
                             String.format("Rs.%.2f", todaySales),
                             String.format("Rs.%.2f", monthlySales)});

            // --- Top 10 Selling Products ---
            Paragraph topTitle = new Paragraph("Top 10 Selling Products",
                new Font(Font.HELVETICA, 13, Font.BOLD, INDIGO_COLOR));
            topTitle.setSpacingBefore(16);
            topTitle.setSpacingAfter(8);
            doc.add(topTitle);

            List<Sale> allSales = saleDAO.getAllSales();
            Map<String, Integer> productQty = new LinkedHashMap<>();
            Map<String, Double> productRevenue = new LinkedHashMap<>();
            for (Sale s : allSales) {
                productQty.merge(s.getProductName(), s.getQuantity(), Integer::sum);
                productRevenue.merge(s.getProductName(), s.getTotal(), Double::sum);
            }

            // Sort by quantity descending
            List<Map.Entry<String, Integer>> sorted = new ArrayList<>(productQty.entrySet());
            sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

            PdfPTable topTable = new PdfPTable(4);
            topTable.setWidthPercentage(100);
            topTable.setWidths(new float[]{0.5f, 3f, 1.5f, 2f});
            addTableHeader(topTable, "Rank", "Product Name", "Total Qty Sold", "Total Revenue");

            int rank = 1;
            for (Map.Entry<String, Integer> entry : sorted) {
                if (rank > 10) break;
                boolean alt = rank % 2 == 0;
                topTable.addCell(dataCell(String.valueOf(rank), alt));
                topTable.addCell(dataCell(entry.getKey(), alt));
                topTable.addCell(dataCell(String.valueOf(entry.getValue()), alt));
                double revenue = productRevenue.getOrDefault(entry.getKey(), 0.0);
                topTable.addCell(dataCell(String.format("Rs.%.2f", revenue), alt));
                rank++;
            }
            doc.add(topTable);

            // --- Low Stock Alert ---
            Paragraph lowTitle = new Paragraph("Low Stock Alerts",
                new Font(Font.HELVETICA, 13, Font.BOLD, RED_COLOR));
            lowTitle.setSpacingBefore(16);
            lowTitle.setSpacingAfter(8);
            doc.add(lowTitle);

            PdfPTable lowTable = new PdfPTable(5);
            lowTable.setWidthPercentage(100);
            lowTable.setWidths(new float[]{0.5f, 2.5f, 1.5f, 1.2f, 1.2f});
            addTableHeader(lowTable, "#", "Name", "Category", "Current Qty", "Low Stock Limit");

            for (int i = 0; i < lowStockProducts.size(); i++) {
                Product p = lowStockProducts.get(i);
                boolean alt = i % 2 != 0;
                lowTable.addCell(dataCell(String.valueOf(i + 1), alt));
                lowTable.addCell(dataCell(p.getName(), alt));
                lowTable.addCell(dataCell(p.getCategory(), alt));

                // Current quantity in red bold
                Font qtyFont = new Font(Font.HELVETICA, 10, Font.BOLD, RED_COLOR);
                PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(p.getQuantity()), qtyFont));
                qtyCell.setPadding(7);
                qtyCell.setBorderColor(BORDER_COLOR);
                qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                if (alt) qtyCell.setBackgroundColor(ALT_ROW);
                lowTable.addCell(qtyCell);

                lowTable.addCell(dataCell(String.valueOf(p.getLowStockLimit()), alt));
            }
            doc.add(lowTable);

            addFooter(doc);
            doc.close();

            JOptionPane.showMessageDialog(null,
                "Report PDF exported successfully!\n" + path,
                "Export Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Export failed: " + e.getMessage(),
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private static void addHeader(Document doc, String reportName) throws DocumentException {
        Paragraph appName = new Paragraph("InvenTrack", titleFont);
        appName.setAlignment(Element.ALIGN_LEFT);
        doc.add(appName);

        Font reportFont = new Font(Font.HELVETICA, 15, Font.BOLD, DARK_GRAY);
        Paragraph report = new Paragraph(reportName, reportFont);
        report.setAlignment(Element.ALIGN_LEFT);
        doc.add(report);

        String ts = "Generated: " + LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        Paragraph sub = new Paragraph(ts, subtitleFont);
        sub.setAlignment(Element.ALIGN_LEFT);
        sub.setSpacingAfter(12);
        doc.add(sub);

        LineSeparator ls = new LineSeparator(1f, 100f, INDIGO_COLOR, Element.ALIGN_CENTER, -2);
        doc.add(new Chunk(ls));
        doc.add(Chunk.NEWLINE);
    }

    private static void addTableHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(INDIGO_COLOR);
            cell.setPadding(8);
            cell.setBorderColor(BORDER_COLOR);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private static PdfPCell dataCell(String text, boolean altRow) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "-" : text, cellFont));
        cell.setPadding(7);
        cell.setBorderColor(BORDER_COLOR);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        if (altRow) cell.setBackgroundColor(ALT_ROW);
        return cell;
    }

    private static void addSummaryRow(Document doc, String[] labels, String[] values) throws DocumentException {
        PdfPTable summary = new PdfPTable(labels.length);
        summary.setWidthPercentage(100);
        summary.setSpacingBefore(8);
        summary.setSpacingAfter(8);

        for (int i = 0; i < labels.length; i++) {
            PdfPCell cell = new PdfPCell();
            cell.setPadding(10);
            cell.setBorderColor(BORDER_COLOR);
            cell.setBackgroundColor(LIGHT_INDIGO);

            Paragraph p = new Paragraph();
            p.add(new Chunk(values[i] + "\n",
                new Font(Font.HELVETICA, 14, Font.BOLD, INDIGO_COLOR)));
            p.add(new Chunk(labels[i],
                new Font(Font.HELVETICA, 9, Font.NORMAL, GRAY_COLOR)));
            p.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(p);
            summary.addCell(cell);
        }
        doc.add(summary);
    }

    private static void addFooter(Document doc) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        LineSeparator ls = new LineSeparator(0.5f, 100f, LIGHT_GRAY, Element.ALIGN_CENTER, -2);
        doc.add(new Chunk(ls));
        Paragraph footer = new Paragraph(
            "Generated by InvenTrack  |  " +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
            new Font(Font.HELVETICA, 8, Font.ITALIC, GRAY_COLOR));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(4);
        doc.add(footer);
    }
}