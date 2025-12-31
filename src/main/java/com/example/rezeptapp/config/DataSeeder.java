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

            Recipe r1 = new Recipe();
            r1.setTitle("Spaghetti Carbonara");
            r1.setDescription("Klassisch cremig – ohne Sahne, nur Ei & Parmesan.");
            r1.setInstructions("""
                    1) Wasser salzen, Pasta kochen.
                    2) Speck anbraten.
                    3) Ei + Parmesan verrühren.
                    4) Pasta abgießen, mit Speck mischen.
                    5) Topf vom Herd, Eiermix einrühren, pfeffern.
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

            Recipe r2 = new Recipe();
            r2.setTitle("Veggie Bowl");
            r2.setDescription("Frisch, gesund, schnell – ideal fürs Meal-Prep.");
            r2.setInstructions("""
                    1) Quinoa kochen.
                    2) Gemüse schneiden.
                    3) Alles in Schüssel anrichten.
                    4) Dressing darüber, toppen.
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

            Recipe r3 = new Recipe();
            r3.setTitle("Pancakes mit Beeren");
            r3.setDescription("Fluffige Pancakes – perfekt fürs Wochenende.");
            r3.setInstructions("""
                    1) Teig rühren.
                    2) Pfanne leicht fetten.
                    3) Pancakes ausbacken.
                    4) Mit Beeren servieren.
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