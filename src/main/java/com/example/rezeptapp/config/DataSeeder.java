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
            // ✅ Insert-if-missing: niemals "return", nur fehlende einfügen

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
            r1.setCategory("Italienisch");
            r1.setServings(2);
            r1.setPrepMinutes(25);
            r1.setNutrition(nut(760, 32.0, 32.0, 82.0));
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
            r2.setNutrition(nut(520, 18.0, 16.0, 70.0));
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
            r3.setNutrition(nut(610, 14.0, 18.0, 96.0));
            r3.setIngredients(List.of(
                    ing("Mehl", "200", "g"),
                    ing("Milch", "250", "ml"),
                    ing("Ei", "2", "Stk"),
                    ing("Backpulver", "2", "TL"),
                    ing("Beeren", "150", "g")
            ));

            Recipe r4 = new Recipe();
            r4.setTitle("Aglio e Olio");
            r4.setDescription("Minimalistisch, würzig, in kurzer Zeit fertig – perfekt, wenn’s schnell gehen soll.");
            r4.setInstructions("""
                    1) Wasser & Pasta:
                       Einen großen Topf Wasser zum Kochen bringen und ordentlich salzen.
                       Spaghetti nach Packungsangabe al dente kochen. Wichtig: am Ende 1–2 Kellen Nudelwasser aufheben,
                       damit du später die Sauce schön cremig bekommst.

                    2) Knoblauch vorbereiten:
                       Knoblauch schälen und in dünne Scheiben schneiden (oder fein hacken, wenn du es intensiver magst).
                       Petersilie waschen und fein hacken. Chiliflocken bereitstellen.

                    3) Öl aromatisieren:
                       Olivenöl in einer großen Pfanne bei mittlerer Hitze erwärmen.
                       Knoblauch darin langsam goldgelb braten (nicht dunkel werden lassen, sonst wird er bitter).
                       Chiliflocken kurz mitziehen lassen, damit das Öl das Aroma aufnimmt.

                    4) Pasta einarbeiten:
                       Spaghetti abgießen und direkt in die Pfanne geben.
                       Jetzt schluckweise Nudelwasser hinzufügen und alles kräftig schwenken, bis das Öl sich leicht bindet
                       und die Nudeln glänzend umhüllt sind.

                    5) Finish & Servieren:
                       Pfanne vom Herd nehmen, Petersilie untermischen und nach Geschmack mit Salz/Pfeffer abschmecken.
                       Optional passt Parmesan oder Zitronenabrieb super – aber klassisch geht’s auch ohne.
                       Sofort servieren, solange alles heiß ist.
                    """);
            r4.setCategory("Italienisch");
            r4.setServings(2);
            r4.setPrepMinutes(15);
            r4.setNutrition(nut(640, 16.0, 20.0, 96.0));
            r4.setIngredients(List.of(
                    ing("Spaghetti", "200", "g"),
                    ing("Knoblauch", "3", "Zehen"),
                    ing("Olivenöl", "4", "EL"),
                    ing("Chiliflocken", "1", "TL"),
                    ing("Petersilie", "1", "Bund")
            ));

            Recipe r5 = new Recipe();
            r5.setTitle("Gemüse-Curry mit Reis");
            r5.setDescription("Cremiges Curry mit warmen Gewürzen – einfach, sättigend und super variabel.");
            r5.setInstructions("""
                    1) Reis kochen:
                       Reis in einem Sieb kurz waschen (optional, macht ihn lockerer).
                       Dann mit Wasser und einer Prise Salz nach Packungsangabe garen.
                       Wenn er fertig ist, abdecken und warm halten.

                    2) Basis anbraten:
                       Zwiebel und Knoblauch fein würfeln.
                       In einem Topf oder einer großen Pfanne mit etwas Öl glasig anbraten.
                       Currypulver (oder Paste) kurz mitrösten, damit die Gewürze ihr Aroma entfalten.

                    3) Gemüse vorbereiten:
                       Gemüse nach Wahl (z.B. Paprika, Zucchini, Karotte) in mundgerechte Stücke schneiden.
                       Erst die festeren Sorten in die Pfanne geben und 2–3 Minuten anbraten,
                       dann weichere Gemüsesorten ergänzen.

                    4) Köcheln lassen:
                       Kokosmilch dazugeben und bei Bedarf mit einem Schluck Wasser oder Brühe strecken.
                       10–12 Minuten sanft köcheln lassen, bis das Gemüse gar ist, aber noch etwas Biss hat.
                       Zwischendurch umrühren, damit nichts ansetzt.

                    5) Abschmecken & Servieren:
                       Mit Salz, Pfeffer und optional Limetten-/Zitronensaft abschmecken.
                       Wer mag, gibt noch frische Kräuter dazu (Koriander oder Petersilie).
                       Curry zusammen mit dem Reis servieren.
                    """);
            r5.setCategory("Orientalisch");
            r5.setServings(3);
            r5.setPrepMinutes(30);
            r5.setNutrition(nut(680, 14.0, 24.0, 98.0));
            r5.setIngredients(List.of(
                    ing("Reis", "250", "g"),
                    ing("Kokosmilch", "400", "ml"),
                    ing("Zwiebel", "1", "Stk"),
                    ing("Gemüsemix (z.B. Paprika, Zucchini)", "400", "g"),
                    ing("Currypulver", "2", "TL")
            ));

            Recipe r6 = new Recipe();
            r6.setTitle("Falafel Wrap");
            r6.setDescription("Knusprige Falafel im Wrap mit frischem Gemüse und cremiger Sauce.");
            r6.setInstructions("""
                    1) Falafel zubereiten:
                       Falafel nach Packungsanleitung im Ofen, Airfryer oder in der Pfanne knusprig backen.
                       Wenn du sie in der Pfanne machst, nur wenig Öl verwenden und regelmäßig wenden.

                    2) Sauce anrühren:
                       Joghurt mit Zitronensaft, Salz und Pfeffer verrühren.
                       Optional Knoblauch fein reiben und dazugeben (macht’s würziger).
                       Wer es frischer mag: etwas gehackte Petersilie oder Minze untermischen.

                    3) Gemüse schneiden:
                       Salat waschen und trocken tupfen.
                       Tomaten und ggf. Gurke in Scheiben schneiden. Alles griffbereit stellen,
                       damit du die Wraps schnell füllen kannst.

                    4) Wraps erwärmen & belegen:
                       Wraps kurz in einer trockenen Pfanne erwärmen, dann werden sie flexibler.
                       Sauce auf den Wrap streichen, Salat und Gemüse verteilen, Falafel drauflegen.

                    5) Einrollen & Servieren:
                       Seiten leicht einklappen, dann straff aufrollen.
                       Halbieren und sofort servieren – so bleibt alles frisch und knusprig.
                    """);
            r6.setCategory("Orientalisch");
            r6.setServings(2);
            r6.setPrepMinutes(20);
            r6.setNutrition(nut(720, 26.0, 28.0, 88.0));
            r6.setIngredients(List.of(
                    ing("Wraps", "2", "Stk"),
                    ing("Falafel", "10", "Stk"),
                    ing("Joghurt", "150", "g"),
                    ing("Salat", "1", "Handvoll"),
                    ing("Tomate", "1", "Stk")
            ));

            Recipe r7 = new Recipe();
            r7.setTitle("Tofu Stir-Fry");
            r7.setDescription("Schnelles Wok-Gericht mit Gemüse, Sojasauce und Sesam – perfekt für unter der Woche.");
            r7.setInstructions("""
                    1) Tofu vorbereiten:
                       Tofu trocken tupfen (je trockener, desto knuspriger).
                       In Würfel schneiden und optional leicht salzen.
                       In einer heißen Pfanne mit etwas Öl goldbraun anbraten und beiseitestellen.

                    2) Gemüse schneiden:
                       Gemüse (z.B. Brokkoli, Paprika, Karotte) in mundgerechte Stücke schneiden.
                       Wenn du Brokkoli nutzt, kann ein kurzes Blanchieren (1–2 Minuten) helfen,
                       damit er schneller gar wird und schön grün bleibt.

                    3) Sauce mischen:
                       Sojasauce mit geriebenem Ingwer und optional etwas Knoblauch verrühren.
                       Wer es süßlich mag, kann einen kleinen Teelöffel Honig/Zucker dazugeben.
                       Alles bereitstellen, weil es im Wok schnell geht.

                    4) Wok-Phase:
                       Gemüse bei hoher Hitze kurz und kräftig anbraten, damit es knackig bleibt.
                       Dann den Tofu wieder dazugeben und die Sauce darüber gießen.
                       1–2 Minuten schwenken, bis alles gut glasiert ist.

                    5) Servieren:
                       Mit Sesam bestreuen und nach Wunsch mit Reis oder Nudeln servieren.
                       Optional: ein Spritzer Limette oder etwas Chili bringt extra Kick.
                    """);
            r7.setCategory("Asiatisch");
            r7.setServings(2);
            r7.setPrepMinutes(25);
            r7.setNutrition(nut(590, 28.0, 22.0, 68.0));
            r7.setIngredients(List.of(
                    ing("Tofu", "250", "g"),
                    ing("Gemüse (Brokkoli/Paprika)", "350", "g"),
                    ing("Sojasauce", "3", "EL"),
                    ing("Ingwer", "1", "TL"),
                    ing("Sesam", "1", "EL")
            ));

            Recipe r8 = new Recipe();
            r8.setTitle("Sushi Bowl");
            r8.setDescription("Sushi-Feeling ohne Rollen: Reis, frische Toppings und eine schnelle Sauce.");
            r8.setInstructions("""
                    1) Reis kochen:
                       Reis gründlich waschen, bis das Wasser klarer wird (macht ihn weniger klebrig).
                       Dann nach Packungsangabe kochen und kurz ruhen lassen.
                       Optional: mit Reisessig, etwas Zucker und Salz abschmecken (Sushi-Style).

                    2) Toppings vorbereiten:
                       Gurke, Avocado und Karotte schneiden (z.B. in Stifte oder dünne Scheiben).
                       Wenn du extra Protein willst: Tofu anbraten oder Lachs/Thunfisch (optional) vorbereiten.

                    3) Sauce anrühren:
                       Sojasauce mit Sesamöl und Limetten-/Zitronensaft mischen.
                       Optional etwas Sriracha oder Chili, wenn du es schärfer magst.

                    4) Bowl zusammenbauen:
                       Reis als Basis in eine Schüssel geben.
                       Toppings hübsch darauf anrichten (sieht direkt besser aus und macht mehr Spaß).

                    5) Finish:
                       Sauce darüber träufeln und mit Sesam und optional Nori-Streifen toppen.
                       Sofort servieren, damit Avocado frisch bleibt.
                    """);
            r8.setCategory("Asiatisch");
            r8.setServings(2);
            r8.setPrepMinutes(25);
            r8.setNutrition(nut(650, 18.0, 20.0, 96.0));
            r8.setIngredients(List.of(
                    ing("Reis", "200", "g"),
                    ing("Avocado", "1", "Stk"),
                    ing("Gurke", "1", "Stk"),
                    ing("Sojasauce", "3", "EL"),
                    ing("Sesam", "1", "EL")
            ));

            Recipe r9 = new Recipe();
            r9.setTitle("Chili sin Carne");
            r9.setDescription("Deftig, vegan, perfekt zum Vorkochen – schmeckt am nächsten Tag oft noch besser.");
            r9.setInstructions("""
                    1) Basis anbraten:
                       Zwiebel und Knoblauch fein würfeln und in einem Topf mit etwas Öl glasig anbraten.
                       Dadurch bekommt das Chili eine gute Grundwürze.

                    2) Gemüse hinzufügen:
                       Paprika in Würfel schneiden und kurz mit anbraten.
                       Wer mag, kann auch Karotte oder Sellerie ergänzen – macht’s noch aromatischer.

                    3) Bohnen & Tomaten:
                       Bohnen und Mais abgießen, abspülen und in den Topf geben.
                       Tomaten (stückig) dazugeben und alles gut verrühren.

                    4) Köcheln lassen:
                       Das Chili 15–20 Minuten bei kleiner Hitze köcheln lassen.
                       Zwischendurch umrühren und bei Bedarf einen Schluck Wasser dazugeben,
                       falls es zu dick wird.

                    5) Abschmecken & Servieren:
                       Mit Salz, Pfeffer, Chili und Kreuzkümmel abschmecken.
                       Optional passt Kakao (eine Prise) für Tiefe oder Limettensaft für Frische.
                       Mit Reis, Brot oder einfach pur servieren.
                    """);
            r9.setCategory("Vegan");
            r9.setServings(4);
            r9.setPrepMinutes(35);
            r9.setNutrition(nut(540, 22.0, 10.0, 88.0));
            r9.setIngredients(List.of(
                    ing("Kidneybohnen", "1", "Dose"),
                    ing("Mais", "1", "Dose"),
                    ing("Tomaten (stückig)", "400", "g"),
                    ing("Paprika", "2", "Stk"),
                    ing("Kreuzkümmel", "1", "TL")
            ));

            Recipe r10 = new Recipe();
            r10.setTitle("Guacamole mit Nachos");
            r10.setDescription("Cremige Guacamole – ein super schneller Snack für Filmabend oder Gäste.");
            r10.setInstructions("""
                    1) Avocado vorbereiten:
                       Avocados halbieren, Kern entfernen und das Fruchtfleisch mit einem Löffel herausnehmen.
                       In einer Schüssel mit einer Gabel grob zerdrücken (nicht komplett pürieren – Struktur ist lecker).

                    2) Säure & Würze:
                       Limettensaft dazugeben (verhindert auch, dass die Avocado schnell braun wird).
                       Mit Salz und Pfeffer würzen. Wer’s scharf mag, gibt Chili oder Jalapeños dazu.

                    3) Tomate schneiden:
                       Tomate entkernen (optional) und in kleine Würfel schneiden.
                       Unter die Avocado mischen, so wird’s frischer und weniger „schwer“.

                    4) Extra Geschmack:
                       Optional rote Zwiebel sehr fein würfeln und unterrühren.
                       Auch Koriander passt super, wenn du den Geschmack magst.

                    5) Servieren:
                       Alles abschmecken und direkt mit Nachos servieren.
                       Tipp: Wenn du sie aufbewahrst, die Oberfläche glatt streichen und Limettensaft drüber geben.
                    """);
            r10.setCategory("Mexikanisch");
            r10.setServings(2);
            r10.setPrepMinutes(10);
            r10.setNutrition(nut(620, 10.0, 34.0, 70.0));
            r10.setIngredients(List.of(
                    ing("Avocado", "2", "Stk"),
                    ing("Limette", "1", "Stk"),
                    ing("Tomate", "1", "Stk"),
                    ing("Nachos", "150", "g"),
                    ing("Salz", "1", "Prise")
            ));

            Recipe r11 = new Recipe();
            r11.setTitle("Burrito Bowl");
            r11.setDescription("Wie ein Burrito, nur als Bowl – schnell, bunt und richtig sättigend.");
            r11.setInstructions("""
                    1) Reis kochen:
                       Reis nach Packungsangabe kochen und warm halten.
                       Optional kannst du ihn am Ende mit etwas Limettensaft und Salz abschmecken.

                    2) Bohnen & Mais:
                       Bohnen und Mais abgießen, abspülen und kurz in einem kleinen Topf erwärmen.
                       Wer mag, kann sie mit etwas Paprika-/Chili-Gewürz würzen.

                    3) Frische Zutaten:
                       Tomaten würfeln, Salat waschen, Avocado schneiden.
                       Optional kannst du auch Zwiebel, Gurke oder Mais-Salsa ergänzen.

                    4) Bowl anrichten:
                       Reis als Basis in die Schüssel.
                       Bohnen, Mais und frische Toppings darauf verteilen (am besten „in Bereichen“, sieht schön aus).

                    5) Sauce & Finish:
                       Salsa darüber geben oder eine schnelle Joghurt-Limetten-Sauce anrühren.
                       Optional: Käse, Jalapeños oder frische Kräuter für mehr Geschmack.
                    """);
            r11.setCategory("Mexikanisch");
            r11.setServings(2);
            r11.setPrepMinutes(25);
            r11.setNutrition(nut(720, 24.0, 22.0, 104.0));
            r11.setIngredients(List.of(
                    ing("Reis", "180", "g"),
                    ing("Schwarze Bohnen", "1", "Dose"),
                    ing("Mais", "1/2", "Dose"),
                    ing("Avocado", "1", "Stk"),
                    ing("Salsa", "4", "EL")
            ));

            Recipe r12 = new Recipe();
            r12.setTitle("Caesar Salad");
            r12.setDescription("Knackig, cremig, mit Croutons – ein Klassiker, der immer funktioniert.");
            r12.setInstructions("""
                    1) Salat vorbereiten:
                       Romanasalat waschen und gut trocken schleudern (sonst wässert das Dressing).
                       In mundgerechte Stücke schneiden und in eine große Schüssel geben.

                    2) Croutons machen:
                       Brot in Würfel schneiden.
                       In einer Pfanne mit wenig Öl oder Butter anrösten, bis sie goldbraun und knusprig sind.
                       Optional Knoblauch für mehr Aroma in die Pfanne geben.

                    3) Dressing anrühren:
                       Joghurt oder Mayonnaise mit Zitronensaft verrühren.
                       Parmesan fein reiben und dazugeben. Mit Salz, Pfeffer und ggf. Knoblauch abschmecken.

                    4) Alles mischen:
                       Dressing über den Salat geben und vorsichtig vermengen,
                       sodass alles leicht überzogen ist (nicht ertränken).

                    5) Toppen & servieren:
                       Croutons darüber geben, damit sie knusprig bleiben.
                       Mit extra Parmesan toppen und sofort servieren.
                    """);
            r12.setCategory("Salat");
            r12.setServings(2);
            r12.setPrepMinutes(20);
            r12.setNutrition(nut(520, 18.0, 32.0, 38.0));
            r12.setIngredients(List.of(
                    ing("Romanasalat", "1", "Kopf"),
                    ing("Brot", "2", "Scheiben"),
                    ing("Parmesan", "40", "g"),
                    ing("Zitrone", "1/2", "Stk"),
                    ing("Joghurt/Mayonnaise", "3", "EL")
            ));

            Recipe r13 = new Recipe();
            r13.setTitle("Tomatensuppe");
            r13.setDescription("Cremig und gemütlich – ideal an kalten Tagen oder wenn du schnell etwas Warmes willst.");
            r13.setInstructions("""
                    1) Zwiebel & Knoblauch:
                       Zwiebel und Knoblauch fein würfeln.
                       In einem Topf mit etwas Öl bei mittlerer Hitze glasig anschwitzen,
                       damit die Basis schön aromatisch wird.

                    2) Tomaten anrösten:
                       Tomaten (stückig) dazugeben und 2–3 Minuten mit anschwitzen.
                       Das bringt mehr Tiefe, als wenn man sie nur kocht.

                    3) Brühe & köcheln:
                       Gemüsebrühe dazugeben und alles 10 Minuten sanft köcheln lassen.
                       Dabei gelegentlich umrühren, damit nichts anbrennt.

                    4) Pürieren & cremig machen:
                       Suppe mit dem Pürierstab fein pürieren.
                       Optional Sahne/Creme Fraiche einrühren oder für vegan eine Pflanzencreme nutzen.

                    5) Abschmecken & Servieren:
                       Mit Salz, Pfeffer und Basilikum abschmecken.
                       Mit Brot oder Croutons servieren – das macht’s richtig rund.
                    """);
            r13.setCategory("Suppe");
            r13.setServings(3);
            r13.setPrepMinutes(25);
            r13.setNutrition(nut(360, 9.0, 14.0, 48.0));
            r13.setIngredients(List.of(
                    ing("Tomaten (stückig)", "800", "g"),
                    ing("Zwiebel", "1", "Stk"),
                    ing("Knoblauch", "2", "Zehen"),
                    ing("Gemüsebrühe", "500", "ml"),
                    ing("Basilikum", "1", "Handvoll")
            ));

            Recipe r14 = new Recipe();
            r14.setTitle("Rührei Frühstück");
            r14.setDescription("Schnelles Frühstück: cremiges Rührei, das immer gelingt – perfekt mit Brot.");
            r14.setInstructions("""
                    1) Eier vorbereiten:
                       Eier in einer Schüssel aufschlagen und mit Salz und Pfeffer verquirlen.
                       Optional einen kleinen Schluck Milch dazugeben – macht es etwas cremiger.

                    2) Pfanne richtig temperieren:
                       Butter in einer Pfanne schmelzen.
                       Hitze eher niedrig bis mittel wählen, damit das Ei nicht trocken wird.

                    3) Langsam stocken lassen:
                       Eier in die Pfanne geben und mit einem Spatel langsam vom Rand zur Mitte schieben.
                       Nicht zu heiß und nicht zu hektisch – so wird’s schön cremig.

                    4) Optional verfeinern:
                       Wenn du willst, kurz vor Schluss Schnittlauch oder etwas Käse dazugeben.
                       Dann nur noch kurz rühren, bis alles verbunden ist.

                    5) Servieren:
                       Rührei direkt servieren, solange es noch saftig ist.
                       Mit Brot und Tomaten oder einem kleinen Salat kombinieren.
                    """);
            r14.setCategory("Frühstück");
            r14.setServings(1);
            r14.setPrepMinutes(10);
            r14.setNutrition(nut(520, 28.0, 30.0, 34.0));
            r14.setIngredients(List.of(
                    ing("Ei", "3", "Stk"),
                    ing("Butter", "10", "g"),
                    ing("Brot", "2", "Scheiben"),
                    ing("Tomate", "1", "Stk"),
                    ing("Schnittlauch", "1", "EL")
            ));

            Recipe r15 = new Recipe();
            r15.setTitle("Classic Burger");
            r15.setDescription("Saftiger Burger mit Käse und frischen Toppings – wie aus dem Diner.");
            r15.setInstructions("""
                    1) Patties formen:
                       Hackfleisch nicht zu stark kneten (sonst wird’s fest).
                       In zwei gleich große Patties formen und in der Mitte leicht eindrücken,
                       damit sie sich beim Braten nicht zu stark wölben. Mit Salz und Pfeffer würzen.

                    2) Braten oder Grillen:
                       Pfanne oder Grill gut vorheizen.
                       Patties 3–4 Minuten pro Seite braten (je nach Dicke) und kurz vor Ende den Käse drauflegen,
                       damit er leicht schmilzt. Nicht ständig drücken – sonst verlieren sie Saft.

                    3) Buns rösten:
                       Burger Buns aufschneiden und kurz anrösten (Pfanne oder Grill).
                       Das sorgt dafür, dass sie nicht durchweichen und mehr Geschmack bekommen.

                    4) Burger bauen:
                       Sauce auf den unteren Bun, dann Salat, Patty mit Käse, Tomate und Zwiebel.
                       Optional Gurken oder extra Sauce oben drauf.

                    5) Servieren:
                       Burger sofort servieren, solange Patty und Bun warm sind.
                       Dazu passen Ofenpommes, Kartoffelspalten oder ein frischer Salat.
                    """);
            r15.setCategory("Amerikanisch");
            r15.setServings(2);
            r15.setPrepMinutes(30);
            r15.setNutrition(nut(980, 52.0, 54.0, 72.0));
            r15.setIngredients(List.of(
                    ing("Rinderhack", "300", "g"),
                    ing("Burger Buns", "2", "Stk"),
                    ing("Cheddar", "2", "Scheiben"),
                    ing("Salat", "2", "Blätter"),
                    ing("Tomate", "1", "Stk")
            ));

            // ✅ Jetzt: nur hinzufügen, wenn Titel noch nicht existiert
            ensure(recipeRepository, r1);
            ensure(recipeRepository, r2);
            ensure(recipeRepository, r3);
            ensure(recipeRepository, r4);
            ensure(recipeRepository, r5);
            ensure(recipeRepository, r6);
            ensure(recipeRepository, r7);
            ensure(recipeRepository, r8);
            ensure(recipeRepository, r9);
            ensure(recipeRepository, r10);
            ensure(recipeRepository, r11);
            ensure(recipeRepository, r12);
            ensure(recipeRepository, r13);
            ensure(recipeRepository, r14);
            ensure(recipeRepository, r15);
        };
    }

    private void ensure(RecipeRepository repo, Recipe recipe) {
        String title = (recipe.getTitle() == null) ? "" : recipe.getTitle().trim();
        if (title.isBlank()) return;

        if (repo.existsByTitleIgnoreCase(title)) {
            return; // ✅ schon drin -> nichts tun
        }

        // ✅ wichtig: Backrefs auf recipe setzen (falls deine Recipe.setIngredients das NICHT macht)
        if (recipe.getIngredients() != null) {
            for (Ingredient ing : recipe.getIngredients()) {
                ing.setRecipe(recipe);
            }
        }

        repo.save(recipe);
    }

    private Ingredient ing(String name, String amount, String unit) {
        Ingredient i = new Ingredient();
        i.setName(name);
        i.setAmount(amount);
        i.setUnit(unit);
        return i;
    }

    private Nutrition nut(int kcal, double protein, double fat, double carbs) {
        Nutrition n = new Nutrition();
        n.setCaloriesKcal(kcal);
        n.setProteinG(protein);
        n.setFatG(fat);
        n.setCarbsG(carbs);
        return n;
    }
}