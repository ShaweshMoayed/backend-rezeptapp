package com.example.rezeptapp.config;

import com.example.rezeptapp.model.Ingredient;
import com.example.rezeptapp.model.Nutrition;
import com.example.rezeptapp.model.Recipe;
import com.example.rezeptapp.repository.RecipeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedRecipes(RecipeRepository recipeRepository) {
        return args -> {
            if (recipeRepository.count() > 0) return;

            // ===== 1) Carbonara =====
            Recipe r1 = new Recipe();
            r1.setTitle("Spaghetti Carbonara");
            r1.setDescription("Klassisch cremig – ohne Sahne, nur Ei & Parmesan.");
            r1.setInstructions("""
                    1) Wasser & Pasta:
                       Einen großen Topf mit Wasser zum Kochen bringen und kräftig salzen (es sollte „wie Meerwasser“ schmecken).
                       Spaghetti hineingeben und nach Packungsangabe al dente kochen. Hebe am Ende 1–2 Kellen Nudelwasser auf.

                    2) Speck/Guanciale:
                       Guanciale oder Speck in Würfel schneiden. In einer Pfanne ohne extra Öl bei mittlerer Hitze langsam auslassen,
                       bis er goldbraun und knusprig ist. Pfanne dann vom Herd ziehen.

                    3) Eier-Parmesan-Mix:
                       Eier in einer Schüssel verquirlen und den fein geriebenen Parmesan einrühren.
                       Großzügig mit frisch gemahlenem Pfeffer würzen. (Kein Salz nötig – Speck & Käse sind schon salzig.)

                    4) Alles verbinden:
                       Spaghetti abgießen und direkt zur Pfanne mit dem Speck geben. Kurz durchschwenken.
                       Jetzt etwas Nudelwasser hinzufügen (für Cremigkeit).

                    5) Cremig machen (wichtig!):
                       Pfanne vom Herd lassen, dann erst den Eier-Parmesan-Mix zügig unterrühren.
                       Bei Bedarf schluckweise Nudelwasser zugeben, bis eine seidige, cremige Sauce entsteht.
                       Sofort servieren und mit extra Parmesan & Pfeffer toppen.
                    """);
            r1.setCategory("Pasta");
            r1.setServings(2);
            r1.setPrepMinutes(25);

            Nutrition n1 = new Nutrition();
            n1.setCaloriesKcal(760);
            n1.setProteinG(32.0);
            n1.setFatG(32.0);
            n1.setCarbsG(82.0);
            r1.setNutrition(n1);

            r1.setIngredients(List.of(
                    ing("Spaghetti", "200", "g"),
                    ing("Guanciale/Speck", "120", "g"),
                    ing("Ei", "2", "Stk"),
                    ing("Parmesan", "60", "g"),
                    ing("Pfeffer", "1", "TL")
            ));

            // ===== 2) Veggie Bowl =====
            Recipe r2 = new Recipe();
            r2.setTitle("Veggie Bowl");
            r2.setDescription("Frisch, gesund, schnell – ideal fürs Meal-Prep.");
            r2.setInstructions("""
                    1) Quinoa vorbereiten:
                       Quinoa in einem Sieb gründlich abspülen (nimmt Bitterstoffe weg).
                       Dann mit Wasser im Verhältnis ca. 1:2 aufkochen und 12–15 Minuten sanft köcheln lassen,
                       bis die Körner gar sind. Anschließend 5 Minuten abgedeckt ruhen lassen und mit einer Gabel auflockern.

                    2) Kichererbsen & Gemüse:
                       Kichererbsen abgießen, abspülen und gut abtropfen lassen.
                       Tomaten halbieren, Avocado in Scheiben schneiden. Optional kannst du Gurke, Paprika oder Rotkohl ergänzen.

                    3) Einfaches Zitronen-Dressing:
                       Zitrone auspressen und den Saft mit etwas Salz und Pfeffer mischen.
                       Wenn du willst, kannst du noch einen kleinen Schuss Olivenöl oder etwas Joghurt dazugeben.

                    4) Bowl anrichten:
                       Quinoa als Basis in eine Schüssel geben. Kichererbsen, Tomaten und Avocado darauf verteilen.
                       Mit dem Dressing beträufeln und nach Geschmack mit Kräutern (z.B. Petersilie) toppen.

                    5) Meal-Prep Tipp:
                       Fürs Mitnehmen: Quinoa & Kichererbsen getrennt vom frischen Gemüse lagern.
                       Avocado erst kurz vor dem Essen schneiden, damit sie nicht braun wird.
                    """);
            r2.setCategory("Healthy");
            r2.setServings(2);
            r2.setPrepMinutes(20);

            Nutrition n2 = new Nutrition();
            n2.setCaloriesKcal(520);
            n2.setProteinG(18.0);
            n2.setFatG(16.0);
            n2.setCarbsG(70.0);
            r2.setNutrition(n2);

            r2.setIngredients(List.of(
                    ing("Quinoa", "150", "g"),
                    ing("Kichererbsen", "1", "Dose"),
                    ing("Avocado", "1", "Stk"),
                    ing("Tomaten", "200", "g"),
                    ing("Zitrone", "1", "Stk")
            ));

            // ===== 3) Pancakes =====
            Recipe r3 = new Recipe();
            r3.setTitle("Pancakes mit Beeren");
            r3.setDescription("Fluffige Pancakes – perfekt fürs Wochenende.");
            r3.setInstructions("""
                    1) Teig mischen:
                       Mehl und Backpulver in einer Schüssel vermengen.
                       In einer zweiten Schüssel Eier mit Milch kurz verquirlen.
                       Flüssigkeit zu den trockenen Zutaten geben und nur so lange rühren, bis keine Mehlnester mehr zu sehen sind
                       (nicht zu lange rühren – sonst werden Pancakes zäh).

                    2) Teig ruhen lassen:
                       Den Teig 5–10 Minuten stehen lassen. In der Zeit kann das Backpulver arbeiten und macht sie fluffiger.

                    3) Pfanne vorbereiten:
                       Pfanne auf mittlere Hitze bringen und ganz leicht einfetten (z.B. ein kleines Stück Butter oder neutraler Ölspray).
                       Wenn die Pfanne zu heiß ist, werden sie außen dunkel und innen nicht gar.

                    4) Ausbacken:
                       Pro Pancake eine kleine Kelle Teig in die Pfanne geben.
                       Warten, bis sich viele kleine Bläschen bilden und die Ränder leicht trocken wirken – dann wenden.
                       Zweite Seite 1–2 Minuten backen, bis sie goldbraun ist.

                    5) Servieren:
                       Mit Beeren toppen. Optional passt Joghurt oder etwas Ahornsirup super dazu.
                       Wenn du sie warm halten willst: im Ofen bei ca. 80–100°C zwischenlagern.
                    """);
            r3.setCategory("Dessert");
            r3.setServings(3);
            r3.setPrepMinutes(20);

            Nutrition n3 = new Nutrition();
            n3.setCaloriesKcal(610);
            n3.setProteinG(14.0);
            n3.setFatG(18.0);
            n3.setCarbsG(96.0);
            r3.setNutrition(n3);

            r3.setIngredients(List.of(
                    ing("Mehl", "200", "g"),
                    ing("Milch", "250", "ml"),
                    ing("Ei", "2", "Stk"),
                    ing("Backpulver", "2", "TL"),
                    ing("Beeren", "150", "g")
            ));

            recipeRepository.saveAll(List.of(r1, r2, r3));
        };
    }

    private Ingredient ing(String name, String amount, String unit) {
        Ingredient i = new Ingredient();
        i.setName(name);
        i.setAmount(amount);
        i.setUnit(unit);
        return i;
    }
}