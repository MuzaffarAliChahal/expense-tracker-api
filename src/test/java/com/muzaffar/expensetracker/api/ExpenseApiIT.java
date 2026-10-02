package com.muzaffar.expensetracker.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseApiIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    private String register(String email) throws Exception {
        String body = """
                {"email":"%s","password":"Str0ngPassw0rd!","fullName":"Test User"}
                """.formatted(email);
        String response = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("accessToken").asText();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    private JsonNode postJson(String token, String url, String body) throws Exception {
        String response = mvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(response);
    }

    @Test
    void fullExpenseFlow() throws Exception {
        String token = register(uniqueEmail());
        LocalDate today = LocalDate.now();

        long food = postJson(token, "/api/categories", "{\"name\":\"Food\"}").get("id").asLong();
        long travel = postJson(token, "/api/categories", "{\"name\":\"Travel\"}").get("id").asLong();

        long lunch = postJson(token, "/api/expenses", """
                {"amount":25.50,"description":"Lunch","spentOn":"%s","categoryId":%d}
                """.formatted(today, food)).get("id").asLong();
        postJson(token, "/api/expenses", """
                {"amount":120.00,"description":"Train","spentOn":"%s","categoryId":%d}
                """.formatted(today, travel));
        postJson(token, "/api/expenses", """
                {"amount":4.50,"description":"Coffee","spentOn":"%s"}
                """.formatted(today));

        mvc.perform(get("/api/expenses").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(3));

        mvc.perform(get("/api/expenses").param("categoryId", String.valueOf(food))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].categoryName").value("Food"));

        mvc.perform(put("/api/expenses/" + lunch).header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":30.00,"description":"Lunch (updated)","spentOn":"%s","categoryId":%d}
                                """.formatted(today, food)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(30.00));

        mvc.perform(get("/api/reports/monthly").param("year", String.valueOf(today.getYear()))
                        .param("month", String.valueOf(today.getMonthValue()))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(154.50))
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(jsonPath("$.byCategory", hasSize(3)))
                .andExpect(jsonPath("$.byCategory[0].category").value("Travel"));

        mvc.perform(delete("/api/expenses/" + lunch).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/expenses/" + lunch).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void usersCannotSeeEachOthersExpenses() throws Exception {
        String alice = register(uniqueEmail());
        String bob = register(uniqueEmail());
        long id = postJson(alice, "/api/expenses", """
                {"amount":10,"description":"Private","spentOn":"%s"}
                """.formatted(LocalDate.now())).get("id").asLong();

        mvc.perform(get("/api/expenses/" + id).header(HttpHeaders.AUTHORIZATION, bearer(bob)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/expenses").header(HttpHeaders.AUTHORIZATION, bearer(bob)))
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void requiresAValidToken() throws Exception {
        mvc.perform(get("/api/expenses")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/expenses").header(HttpHeaders.AUTHORIZATION, "Bearer nope"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginAndDuplicateRegistration() throws Exception {
        String email = uniqueEmail();
        register(email);

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"Str0ngPassw0rd!\",\"fullName\":\"X\"}".formatted(email)))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"Str0ngPassw0rd!\"}".formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"wrong-password\"}".formatted(email)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validatesExpenses() throws Exception {
        String token = register(uniqueEmail());
        mvc.perform(post("/api/expenses").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":0,\"spentOn\":\"2999-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists())
                .andExpect(jsonPath("$.errors.spentOn").exists());
    }
}
