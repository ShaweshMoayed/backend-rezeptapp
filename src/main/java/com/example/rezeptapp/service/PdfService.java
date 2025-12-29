package com.example.rezeptapp.service;

import com.example.rezeptapp.model.Ingredient;
import com.example.rezeptapp.model.Nutrition;
import com.example.rezeptapp.model.Recipe;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class PdfService {

    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
    private static final Font H2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
    private static final Font TEXT = FontFactory.getFont(FontFactory.HELVETICA, 11);
    private static final Font MUTED = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(90, 90, 90));

    public byte[] createRecipePdf(Recipe recipe) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 42, 42, 46, 46);
            PdfWriter.getInstance(doc, out);
            doc.open();

            // Header
            Paragraph header = new Paragraph("RezeptApp", MUTED);
            header.setAlignment(Element.ALIGN_RIGHT);
            doc.add(header);

            Paragraph title = new Paragraph(recipe.getTitle() != null ? recipe.getTitle() : "Rezept", TITLE);
            title.setSpacingBefore(6);
            title.setSpacingAfter(10);
            doc.add(title);

            // Bild (optional)
            addRecipeImageIfPossible(doc, recipe);

            // Meta-Infos (Kategorie, Portionen, Zeit)
            doc.add(metaBlock(recipe));

            // Zutaten
            doc.add(sectionTitle("Zutaten"));
            doc.add(ingredientsTable(recipe));

            // Nährwerte
            if (recipe.getNutrition() != null) {
                doc.add(sectionTitle("Nährwerte (pro Portion, wenn bekannt)"));
                doc.add(nutritionTable(recipe.getNutrition()));
            }

            // Anleitung
            doc.add(sectionTitle("Zubereitung"));
            doc.add(instructionsBlock(recipe));

            // Footer
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

    private void addRecipeImageIfPossible(Document doc, Recipe recipe) {
        try {
            if (recipe.getImageUrl() == null || recipe.getImageUrl().isBlank()) return;

            // Robust: erst per HttpClient laden (funktioniert bei vielen Hosts besser)
            byte[] bytes = downloadImage(recipe.getImageUrl());
            if (bytes == null || bytes.length == 0) return;

            Image img = Image.getInstance(bytes);
            img.setAlignment(Image.MIDDLE);
            img.scaleToFit(520, 260);

            PdfPTable frame = new PdfPTable(1);
            frame.setWidthPercentage(100);

            PdfPCell cell = new PdfPCell(img, true);
            cell.setBorderColor(new Color(230, 230, 230));
            cell.setBorderWidth(1f);
            cell.setPadding(6f);
            cell.setBackgroundColor(new Color(250, 250, 250));
            frame.addCell(cell);

            frame.setSpacingAfter(12);
            doc.add(frame);
        } catch (Exception ignored) {
            // Wenn Bild nicht lädt: PDF trotzdem erzeugen (kein Abbruch)
        }
    }

    private byte[] downloadImage(String url) {
        try {
            // Fallback: wenn URL direkt geht
            if (url.startsWith("http")) {
                HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
                HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                        .header("User-Agent", "RezeptApp/1.0")
                        .GET()
                        .build();

                HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) return resp.body();
                return null;
            }

            // Wenn jemand später file:/ oder classpath: nutzen will -> hier erweitern
            return Image.getInstance(new URL(url)).getRawData();
        } catch (Exception e) {
            return null;
        }
    }

    private Element metaBlock(Recipe recipe) {
        PdfPTable meta = new PdfPTable(3);
        meta.setWidthPercentage(100);
        meta.setSpacingAfter(14);

        meta.addCell(metaCell("Kategorie", safe(recipe.getCategory(), "—")));
        meta.addCell(metaCell("Portionen", recipe.getServings() != null ? recipe.getServings().toString() : "—"));
        meta.addCell(metaCell("Zeit (Min.)", recipe.getPrepMinutes() != null ? recipe.getPrepMinutes().toString() : "—"));

        return meta;
    }

    private PdfPCell metaCell(String label, String value) {
        PdfPCell c = new PdfPCell();
        c.setBorderColor(new Color(235, 235, 235));
        c.setBorderWidth(1f);
        c.setPadding(10f);
        c.setBackgroundColor(new Color(248, 249, 250));

        Paragraph p1 = new Paragraph(label, MUTED);
        Paragraph p2 = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
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

    private Element ingredientsTable(Recipe recipe) {
        PdfPTable table = new PdfPTable(new float[]{2.4f, 1f, 1f});
        table.setWidthPercentage(100);
        table.setSpacingAfter(12);

        table.addCell(th("Zutat"));
        table.addCell(th("Menge"));
        table.addCell(th("Einheit"));

        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            PdfPCell c = new PdfPCell(new Phrase("Keine Zutaten hinterlegt.", TEXT));
            c.setColspan(3);
            c.setPadding(10);
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

    private Element nutritionTable(Nutrition n) {
        PdfPTable table = new PdfPTable(new float[]{2f, 1f});
        table.setWidthPercentage(60);
        table.setSpacingAfter(12);

        table.addCell(th("Wert"));
        table.addCell(th("Menge"));

        table.addCell(td("Kalorien (kcal)"));
        table.addCell(td(n.getCaloriesKcal() != null ? n.getCaloriesKcal().toString() : "—"));

        table.addCell(td("Protein (g)"));
        table.addCell(td(n.getProteinG() != null ? n.getProteinG().toString() : "—"));

        table.addCell(td("Fett (g)"));
        table.addCell(td(n.getFatG() != null ? n.getFatG().toString() : "—"));

        table.addCell(td("Kohlenhydrate (g)"));
        table.addCell(td(n.getCarbsG() != null ? n.getCarbsG().toString() : "—"));

        return table;
    }

    private Element instructionsBlock(Recipe recipe) {
        String instr = safe(recipe.getInstructions(), safe(recipe.getDescription(), "—"));
        Paragraph p = new Paragraph(instr, TEXT);
        p.setLeading(0, 1.35f);
        return p;
    }

    private PdfPCell th(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE)));
        c.setBackgroundColor(new Color(45, 108, 223));
        c.setPadding(8);
        c.setBorderColor(new Color(45, 108, 223));
        return c;
    }

    private PdfPCell td(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, TEXT));
        c.setPadding(8);
        c.setBorderColor(new Color(235, 235, 235));
        return c;
    }

    private String safe(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }
}