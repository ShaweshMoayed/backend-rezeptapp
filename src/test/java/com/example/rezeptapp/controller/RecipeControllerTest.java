// rezeptapp/src/test/java/com/example/rezeptapp/controller/RecipeControllerTest.java
package com.example.rezeptapp.controller;

import com.example.rezeptapp.model.Ingredient;
import com.example.rezeptapp.model.Nutrition;
import com.example.rezeptapp.model.Recipe;
import com.example.rezeptapp.repository.RecipeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecipeControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired RecipeRepository recipeRepo;

    private String randomUser() {
        return "u_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    private String registerAndLogin(String username, String password) throws Exception {
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new AuthController.AuthRequest(username, password))))
                .andExpect(status().isOk());

        String json = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new AuthController.AuthRequest(username, password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(blankOrNullString())))
                .andReturn().getResponse().getContentAsString();

        return om.readTree(json).get("token").asText();
    }

    private Recipe validRecipe(String title) {
        Recipe r = new Recipe();
        r.setTitle(title);
        r.setDescription("Beschreibung für " + title);
        r.setInstructions("1) Schritt eins\n2) Schritt zwei");
        r.setCategory("Test");
        r.setServings(2);
        r.setPrepMinutes(15);

        Nutrition n = new Nutrition();
        n.setCaloriesKcal(500);
        n.setProteinG(20.0);
        n.setFatG(10.0);
        n.setCarbsG(60.0);
        r.setNutrition(n);

        Ingredient i = new Ingredient();
        i.setName("Zutat A");
        i.setAmount("1");
        i.setUnit("Stk");
        r.setIngredients(List.of(i));

        return r;
    }

    private long createRecipeAs(String token, String title) throws Exception {
        Recipe r = validRecipe(title);

        String json = mvc.perform(post("/rezeptapp")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(r)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.createdByUsername", not(blankOrNullString())))
                .andReturn().getResponse().getContentAsString();

        return om.readTree(json).get("id").asLong();
    }

    private long anyPublicRecipeId() {
        // Seeder-Rezepte sind public => createdByUsername == null
        return recipeRepo.findPublicOnly().stream()
                .findFirst()
                .map(Recipe::getId)
                .orElseThrow(() -> new IllegalStateException("Keine public Rezepte vorhanden (Seeder?)"));
    }

    @Test
    void getAllRecipes_asGuest_returnsOnlyPublic() throws Exception {
        mvc.perform(get("/rezeptapp"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", is(not(empty()))))
                // public: createdByUsername muss null sein
                .andExpect(jsonPath("$[*].createdByUsername", everyItem(nullValue())));
    }

    @Test
    void createRecipe_withoutAuth_returns401() throws Exception {
        Recipe r = validRecipe("NoAuthRecipe");

        mvc.perform(post("/rezeptapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(r)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createRecipe_withAuth_setsOwner_andRecipeAppearsInListForUser() throws Exception {
        String u = randomUser();
        String token = registerAndLogin(u, "pw123");
        String title = "Owned_" + UUID.randomUUID().toString().substring(0, 8);

        long id = createRecipeAs(token, title);

        // user sieht own + public
        String listJson = mvc.perform(get("/rezeptapp")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode arr = om.readTree(listJson);
        boolean found = false;
        for (JsonNode n : arr) {
            if (n.hasNonNull("id") && n.get("id").asLong() == id) found = true;
        }
        org.junit.jupiter.api.Assertions.assertTrue(found, "Erstelltes Rezept muss in der Liste sichtbar sein.");
    }

    @Test
    void getRecipe_owned_asGuest_returns403() throws Exception {
        String u = randomUser();
        String token = registerAndLogin(u, "pw123");
        long ownedId = createRecipeAs(token, "OwnedForForbidden_" + UUID.randomUUID().toString().substring(0, 6));

        mvc.perform(get("/rezeptapp/" + ownedId)) // ohne auth
                .andExpect(status().isForbidden());
    }

    @Test
    void updateRecipe_asWrongUser_returns403() throws Exception {
        String u1 = randomUser();
        String t1 = registerAndLogin(u1, "pw123");
        long ownedId = createRecipeAs(t1, "OwnedToUpdate_" + UUID.randomUUID().toString().substring(0, 6));

        String u2 = randomUser();
        String t2 = registerAndLogin(u2, "pw123");

        Recipe patch = new Recipe();
        patch.setTitle("HackedTitle");

        mvc.perform(put("/rezeptapp/" + ownedId)
                        .header("Authorization", "Bearer " + t2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(patch)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteRecipe_asOwner_ok_andThenNotFoundOrForbiddenForGuest() throws Exception {
        String u = randomUser();
        String token = registerAndLogin(u, "pw123");
        long ownedId = createRecipeAs(token, "OwnedToDelete_" + UUID.randomUUID().toString().substring(0, 6));

        mvc.perform(delete("/rezeptapp/" + ownedId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // guest darf owned sowieso nicht sehen -> 403 (nach delete kann’s auch 403 bleiben durch Service-Logik)
        mvc.perform(get("/rezeptapp/" + ownedId))
                .andExpect(status().isForbidden());
    }

    @Test
    void favorites_add_and_remove_ok() throws Exception {
        String u = randomUser();
        String token = registerAndLogin(u, "pw123");

        long publicId = anyPublicRecipeId();

        // add favorite
        mvc.perform(post("/rezeptapp/" + publicId + "/favorite")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // ids contains publicId
        mvc.perform(get("/rezeptapp/favorites/ids")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItem((int) publicId)));

        // remove favorite
        mvc.perform(delete("/rezeptapp/" + publicId + "/favorite")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // ids does not contain
        mvc.perform(get("/rezeptapp/favorites/ids")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(hasItem((int) publicId))));
    }

    @Test
    void downloadRecipePdf_public_ok_hasPdfContentType() throws Exception {
        long publicId = anyPublicRecipeId();

        mvc.perform(get("/rezeptapp/" + publicId + "/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")))
                .andExpect(header().string("Content-Disposition", containsString("attachment")));
    }
}