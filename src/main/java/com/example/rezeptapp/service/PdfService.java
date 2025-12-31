package com.example.rezeptapp.service;

import com.example.rezeptapp.model.Ingredient;
import com.example.rezeptapp.model.Nutrition;
import com.example.rezeptapp.model.Recipe;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

@Service
public class PdfService {

    // App-Farben (wie dein UI)
    private static final Color BRAND = new Color(47, 93, 76);          // #2f5d4c
    private static final Color BORDER = new Color(235, 235, 235);
    private static final Color TEXT_MUTED = new Color(90, 90, 90);

    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BRAND);
    private static final Font BOX_HEAD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BRAND);
    private static final Font SUB = FontFactory.getFont(FontFactory.HELVETICA, 11, TEXT_MUTED);
    private static final Font H2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, BRAND);
    private static final Font TEXT = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
    private static final Font MUTED = FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_MUTED);

    public byte[] createRecipePdf(Recipe recipe) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document doc = new Document(PageSize.A4, 44, 44, 64, 60);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new PageNumbers());

            doc.open();

            // ✅ Titel-Box enthält jetzt auch "RezeptApp – Rezeptkarte" + Logo
            doc.add(buildTitleBoxWithHeader(recipe));

            // Bild: nur Base64 (keine externen URLs)
            addRecipeImageIfPossible(doc, recipe);

            // Meta-Karten (Kategorie / Portionen / Zeit)
            doc.add(metaBlock(recipe));

            // Zutaten
            doc.add(sectionTitle("Zutaten"));
            doc.add(ingredientsTable(recipe));

            // Nährwerte
            if (recipe.getNutrition() != null) {
                doc.add(sectionTitle("Nährwerte"));
                doc.add(nutritionCards(recipe.getNutrition()));
            }

            // Zubereitung
            doc.add(sectionTitle("Zubereitung"));
            doc.add(instructionsBlock(recipe));

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF konnte nicht erstellt werden: " + e.getMessage(), e);
        }
    }

    // ===== Titel-Box inkl. Header-Zeile + Logo =====
    private Element buildTitleBoxWithHeader(Recipe recipe) {
        PdfPTable outer = new PdfPTable(1);
        outer.setWidthPercentage(100);

        PdfPCell box = new PdfPCell();
        box.setBorderColor(BORDER);
        box.setBorderWidth(1f);
        box.setPadding(14f);
        box.setBackgroundColor(Color.WHITE);

        // Header-Zeile: links "RezeptApp – Rezeptkarte", rechts Logo (kein grauer Text mehr)
        PdfPTable head = new PdfPTable(new float[]{3.2f, 1f});
        head.setWidthPercentage(100);

        PdfPCell left = new PdfPCell(new Phrase("RezeptApp – Rezeptkarte", BOX_HEAD));
        left.setBorder(Rectangle.NO_BORDER);

        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Image logo = loadLogo();
        if (logo != null) {
            logo.scaleToFit(44, 44);
            logo.setAlignment(Image.ALIGN_RIGHT);
            right.addElement(logo);
        } else {
            // ✅ kein Ersatztext mehr (gewünscht)
            right.addElement(new Phrase(""));
        }

        head.addCell(left);
        head.addCell(right);
        head.setSpacingAfter(10);

        // Titel + Datum + Beschreibung
        Paragraph title = new Paragraph(safe(recipe.getTitle(), "Rezept"), TITLE);
        title.setSpacingAfter(6);

        Paragraph createdP = new Paragraph("Erstellt am " + formatCreatedAt(recipe), SUB);
        createdP.setSpacingAfter(8);

        String descTxt = safe(recipe.getDescription(), "").trim();
        Paragraph desc = new Paragraph(descTxt, TEXT);
        desc.setLeading(0, 1.25f);

        box.addElement(head);
        box.addElement(title);
        box.addElement(createdP);
        if (!descTxt.isBlank()) box.addElement(desc);

        outer.addCell(box);
        outer.setSpacingAfter(12);
        return outer;
    }

    private Image loadLogo() {
        // ✅ Datei hier ablegen: src/main/resources/static/logo.png
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("static/logo.png")) {
            if (is == null) return null;
            byte[] bytes = is.readAllBytes();
            return Image.getInstance(bytes);
        } catch (Exception e) {
            return null;
        }
    }

    private String formatCreatedAt(Recipe r) {
        try {
            if (r.getCreatedAt() != null) {
                Date d = Date.from(r.getCreatedAt());
                return new SimpleDateFormat("dd.MM.yyyy, HH:mm").format(d);
            }
        } catch (Exception ignored) {}
        return new SimpleDateFormat("dd.MM.yyyy, HH:mm").format(new Date());
    }

    // ===== Rezeptbild (nur Base64!) =====
    private void addRecipeImageIfPossible(Document doc, Recipe recipe) {
        try {
            byte[] bytes = null;

            if (recipe.getImageBase64() != null && !recipe.getImageBase64().isBlank()) {
                bytes = decodeBase64Image(recipe.getImageBase64());
            }

            // ❌ bewusst KEIN imageUrl-Download mehr
            if (bytes == null || bytes.length == 0) return;

            Image img = Image.getInstance(bytes);
            img.scaleToFit(520, 260);

            PdfPTable frame = new PdfPTable(1);
            frame.setWidthPercentage(100);

            PdfPCell cell = new PdfPCell(img, true);
            cell.setBorderColor(BORDER);
            cell.setBorderWidth(1f);
            cell.setPadding(8f);
            cell.setBackgroundColor(new Color(250, 250, 250));
            frame.addCell(cell);

            frame.setSpacingAfter(12);
            doc.add(frame);
        } catch (Exception ignored) {}
    }

    private byte[] decodeBase64Image(String base64) {
        try {
            int comma = base64.indexOf(',');
            String clean = (comma >= 0) ? base64.substring(comma + 1) : base64;
            clean = clean.trim();
            return Base64.getDecoder().decode(clean);
        } catch (Exception e) {
            return null;
        }
    }

    // ===== Meta-Karten =====
    private Element metaBlock(Recipe recipe) {
        PdfPTable meta = new PdfPTable(3);
        meta.setWidthPercentage(100);
        meta.setSpacingAfter(14);

        meta.addCell(metaCell("Kategorie", safe(recipe.getCategory(), "—")));
        meta.addCell(metaCell("Portionen", recipe.getServings() != null ? recipe.getServings().toString() : "—"));
        meta.addCell(metaCell("Zeit", recipe.getPrepMinutes() != null ? recipe.getPrepMinutes() + " min" : "—"));

        return meta;
    }

    private PdfPCell metaCell(String label, String value) {
        PdfPCell c = new PdfPCell();
        c.setBorderColor(BORDER);
        c.setBorderWidth(1f);
        c.setPadding(10f);
        c.setBackgroundColor(Color.WHITE);

        Paragraph p1 = new Paragraph(label, MUTED);
        Paragraph p2 = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK));
        c.addElement(p1);
        c.addElement(p2);
        return c;
    }

    private Paragraph sectionTitle(String t) {
        Paragraph p = new Paragraph(t, H2);
        p.setSpacingBefore(6);
        p.setSpacingAfter(8);
        return p;
    }

    // ===== Zutaten-Tabelle =====
    private Element ingredientsTable(Recipe recipe) {
        PdfPTable table = new PdfPTable(new float[]{2.6f, 1f, 1f});
        table.setWidthPercentage(100);
        table.setSpacingAfter(12);

        table.addCell(th("Zutat"));
        table.addCell(th("Menge"));
        table.addCell(th("Einheit"));

        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            PdfPCell c = new PdfPCell(new Phrase("Keine Zutaten hinterlegt.", TEXT));
            c.setColspan(3);
            c.setPadding(10);
            c.setBorderColor(BORDER);
            table.addCell(c);
            return table;
        }

        for (Ingredient ing : recipe.getIngredients()) {
            table.addCell(td(safe(ing.getName(), "—")));
            table.addCell(td(safe(ing.getAmount(), "—")));
            table.addCell(td(safe(ing.getUnit(), "—")));
        }

        return table;
    }

    // ===== Nährwerte Cards =====
    private Element nutritionCards(Nutrition n) {
        PdfPTable grid = new PdfPTable(new float[]{1f, 1f});
        grid.setWidthPercentage(70);
        grid.setSpacingAfter(12);

        grid.addCell(nutriCard("Kalorien (kcal)", n.getCaloriesKcal() != null ? n.getCaloriesKcal().toString() : "—"));
        grid.addCell(nutriCard("Protein (g)", n.getProteinG() != null ? n.getProteinG().toString() : "—"));
        grid.addCell(nutriCard("Fett (g)", n.getFatG() != null ? n.getFatG().toString() : "—"));
        grid.addCell(nutriCard("Kohlenhydrate (g)", n.getCarbsG() != null ? n.getCarbsG().toString() : "—"));

        return grid;
    }

    private PdfPCell nutriCard(String label, String value) {
        PdfPCell c = new PdfPCell();
        c.setBorderColor(BORDER);
        c.setBorderWidth(1f);
        c.setPadding(10f);
        c.setBackgroundColor(Color.WHITE);

        Paragraph l = new Paragraph(label, MUTED);
        Paragraph v = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK));
        v.setSpacingBefore(3);

        c.addElement(l);
        c.addElement(v);
        return c;
    }

    // ===== Zubereitung =====
    private Element instructionsBlock(Recipe recipe) {
        String instr = safe(recipe.getInstructions(), safe(recipe.getDescription(), "—"));
        Paragraph p = new Paragraph(instr, TEXT);
        p.setLeading(0, 1.4f);
        return p;
    }

    private PdfPCell th(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE)));
        c.setBackgroundColor(BRAND);
        c.setPadding(8);
        c.setBorderColor(BRAND);
        return c;
    }

    private PdfPCell td(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, TEXT));
        c.setPadding(8);
        c.setBorderColor(BORDER);
        return c;
    }

    private String safe(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }

    // ===== Seitenzahlen unten rechts =====
    private static class PageNumbers extends PdfPageEventHelper {
        private final Font f = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_MUTED);

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            String text = "Seite " + writer.getPageNumber();

            float x = document.right();
            float y = document.bottom() - 18;

            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, new Phrase(text, f), x, y, 0);
        }
    }
}