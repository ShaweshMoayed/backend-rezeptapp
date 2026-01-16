package com.example.rezeptapp.service;

import com.example.rezeptapp.model.MealPlan;
import com.example.rezeptapp.model.MealPlanEntry;
import com.example.rezeptapp.model.MealSlot;
import com.example.rezeptapp.model.Recipe;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class MealPlanPdfService {

    // ===== App-Farben (wie PdfService) =====
    private static final Color BRAND = new Color(47, 93, 76);          // #2f5d4c
    private static final Color BORDER = new Color(235, 235, 235);
    private static final Color TEXT_MUTED = new Color(90, 90, 90);

    // ===== Fonts =====
    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BRAND);
    private static final Font BOX_HEAD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BRAND);
    private static final Font SUB = FontFactory.getFont(FontFactory.HELVETICA, 11, TEXT_MUTED);

    private static final Font H2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, BRAND);
    private static final Font MUTED = FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_MUTED);

    private static final Font CHIP_LABEL = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_MUTED);

    // Dynamische Fonts für lange Rezeptnamen
    private static final Font CHIP_VALUE_12 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
    private static final Font CHIP_VALUE_11 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
    private static final Font CHIP_VALUE_10 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private static final Font CHIP_VALUE_9  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9,  Color.BLACK);

    // Deutsch + Berlin Timezone
    private static final ZoneId ZONE_DE = ZoneId.of("Europe/Berlin");
    private static final Locale LOCALE_DE = Locale.GERMANY;
    private static final DateTimeFormatter DT_DE = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm", LOCALE_DE);
    private static final DateTimeFormatter DF_DE = DateTimeFormatter.ofPattern("dd.MM.yyyy", LOCALE_DE);

    public byte[] createPlanPdf(MealPlan plan) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document doc = new Document(PageSize.A4, 44, 44, 64, 60);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new PageNumbers());

            doc.open();

            // Titel-Box (wie Rezept-PDF)
            doc.add(buildTitleBox(plan));

            // Tages-Blöcke (keepTogether)
            LocalDate monday = plan.getWeekStartMonday();
            List<MealPlanEntry> entries = plan.getEntries() == null ? List.of() : plan.getEntries();
            entries = entries.stream()
                    .sorted(Comparator.comparing(MealPlanEntry::getDay).thenComparing(MealPlanEntry::getSlot))
                    .toList();

            doc.add(sectionTitle("Woche im Überblick"));

            for (int i = 0; i < 7; i++) {
                LocalDate day = monday.plusDays(i);
                doc.add(dayBlock(day, entries));
            }

            doc.add(Chunk.NEWLINE);
            Paragraph footer = new Paragraph("Erstellt mit RezeptApp • PDF-Export", MUTED);
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF konnte nicht erstellt werden: " + e.getMessage(), e);
        }
    }

    // ===== Titel-Box inkl. Header + Logo + Datum =====
    private Element buildTitleBox(MealPlan plan) {
        PdfPTable outer = new PdfPTable(1);
        outer.setWidthPercentage(100);

        PdfPCell box = new PdfPCell();
        box.setBorderColor(BORDER);
        box.setBorderWidth(1f);
        box.setPadding(14f);
        box.setBackgroundColor(Color.WHITE);

        PdfPTable head = new PdfPTable(new float[]{3.2f, 1f});
        head.setWidthPercentage(100);

        PdfPCell left = new PdfPCell(new Phrase("RezeptApp – Wochenplan", BOX_HEAD));
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
            right.addElement(new Phrase(""));
        }

        head.addCell(left);
        head.addCell(right);
        head.setSpacingAfter(10);

        String title = (plan.getTitle() == null || plan.getTitle().isBlank())
                ? "Wochenplan"
                : plan.getTitle().trim();

        // Robuster: extrem lange Titel weich kürzen (Design bleibt sauber)
        String safeTitle = softTruncate(title, 90);

        Paragraph pTitle = new Paragraph(safeTitle, TITLE);
        pTitle.setSpacingAfter(6);
        pTitle.setLeading(0, 1.15f);

        Paragraph createdP = new Paragraph("PDF erstellt am " + formatNowDe(), SUB);
        createdP.setSpacingAfter(6);

        LocalDate monday = plan.getWeekStartMonday();
        LocalDate sunday = monday.plusDays(6);
        Paragraph range = new Paragraph("Woche: " + DF_DE.format(monday) + " – " + DF_DE.format(sunday), SUB);

        box.addElement(head);
        box.addElement(pTitle);
        box.addElement(createdP);
        box.addElement(range);

        outer.addCell(box);
        outer.setSpacingAfter(12);
        return outer;
    }

    private Image loadLogo() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("static/logo.png")) {
            if (is == null) return null;
            byte[] bytes = is.readAllBytes();
            return Image.getInstance(bytes);
        } catch (Exception e) {
            return null;
        }
    }

    private String formatNowDe() {
        return ZonedDateTime.now(ZONE_DE).format(DT_DE);
    }

    private Paragraph sectionTitle(String t) {
        Paragraph p = new Paragraph(t, H2);
        p.setSpacingBefore(6);
        p.setSpacingAfter(8);
        return p;
    }

    // ===== Tages-Block (keepTogether) =====
    private Element dayBlock(LocalDate day, List<MealPlanEntry> entries) {
        PdfPTable outer = new PdfPTable(1);
        outer.setWidthPercentage(100);
        outer.setSpacingAfter(10);
        outer.setKeepTogether(true);

        PdfPCell box = new PdfPCell();
        box.setBorderColor(BORDER);
        box.setBorderWidth(1f);
        box.setPadding(12f);
        box.setBackgroundColor(Color.WHITE);

        Paragraph head = new Paragraph(dayLabel(day), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BRAND));
        head.setSpacingAfter(8);
        box.addElement(head);

        PdfPTable grid = new PdfPTable(new float[]{1f, 1f, 1f});
        grid.setWidthPercentage(100);

        grid.addCell(slotCard(
                "Frühstück",
                recipeTitle(entries, day, MealSlot.BREAKFAST),
                servingsText(entries, day, MealSlot.BREAKFAST)
        ));
        grid.addCell(slotCard(
                "Mittagessen",
                recipeTitle(entries, day, MealSlot.LUNCH),
                servingsText(entries, day, MealSlot.LUNCH)
        ));
        grid.addCell(slotCard(
                "Abendessen",
                recipeTitle(entries, day, MealSlot.DINNER),
                servingsText(entries, day, MealSlot.DINNER)
        ));

        box.addElement(grid);
        outer.addCell(box);

        return outer;
    }

    private PdfPCell slotCard(String label, String value, String servings) {
        PdfPCell c = new PdfPCell();
        c.setBorderColor(BORDER);
        c.setBorderWidth(1f);
        c.setPadding(10f);
        c.setBackgroundColor(Color.WHITE);

        // Robuster: sorgt für sauberes Wrapping/Vertikal-Layout
        c.setNoWrap(false);
        c.setUseAscender(true);
        c.setUseDescender(true);
        c.setVerticalAlignment(Element.ALIGN_TOP);

        Paragraph l = new Paragraph(label, CHIP_LABEL);

        String vRaw = (value == null || value.isBlank()) ? "—" : value.trim();
        // Robuster: extrem lange Rezeptnamen weich kürzen, damit Karten nicht explodieren
        String vTxt = softTruncate(vRaw, 80);
        Font vFont = pickValueFont(vTxt);

        Paragraph v = new Paragraph(vTxt, vFont);
        v.setSpacingBefore(3);
        v.setLeading(0, 1.12f); // etwas enger, damit 2-3 Zeilen noch gut passen

        c.addElement(l);
        c.addElement(v);

        if (servings != null && !servings.isBlank()) {
            Paragraph s = new Paragraph(servings, MUTED);
            s.setSpacingBefore(3);
            c.addElement(s);
        }

        return c;
    }

    // Dynamische Schriftgröße je nach Länge
    private Font pickValueFont(String text) {
        int len = text == null ? 0 : text.length();
        if (len > 70) return CHIP_VALUE_9;
        if (len > 55) return CHIP_VALUE_10;
        if (len > 40) return CHIP_VALUE_11;
        return CHIP_VALUE_12;
    }

    // Weiches Kürzen (ohne "hartes" Abschneiden mitten im Wort, wenn möglich)
    private String softTruncate(String s, int max) {
        if (s == null) return "";
        String t = s.trim();
        if (t.length() <= max) return t;

        int cut = max - 1; // Platz für "…"
        if (cut < 1) return "…";

        // Wenn möglich: auf letztem Leerzeichen vor cut schneiden
        int lastSpace = t.lastIndexOf(' ', cut);
        if (lastSpace >= Math.max(10, cut - 25)) {
            return t.substring(0, lastSpace).trim() + "…";
        }
        return t.substring(0, cut).trim() + "…";
    }

    private String recipeTitle(List<MealPlanEntry> entries, LocalDate day, MealSlot slot) {
        for (MealPlanEntry e : entries) {
            if (day.equals(e.getDay()) && slot == e.getSlot()) {
                Recipe r = e.getRecipe();
                return r == null ? "—" : safe(r.getTitle(), "—");
            }
        }
        return "—";
    }

    private String servingsText(List<MealPlanEntry> entries, LocalDate day, MealSlot slot) {
        for (MealPlanEntry e : entries) {
            if (day.equals(e.getDay()) && slot == e.getSlot()) {
                Integer s = e.getServings();
                if (s == null) return "";
                if (s == 1) return "1 Portion";
                return s + " Portionen";
            }
        }
        return "";
    }

    private String dayLabel(LocalDate d) {
        DayOfWeek w = d.getDayOfWeek();
        String name = switch (w) {
            case MONDAY -> "Montag";
            case TUESDAY -> "Dienstag";
            case WEDNESDAY -> "Mittwoch";
            case THURSDAY -> "Donnerstag";
            case FRIDAY -> "Freitag";
            case SATURDAY -> "Samstag";
            case SUNDAY -> "Sonntag";
        };
        return name + " • " + DF_DE.format(d);
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