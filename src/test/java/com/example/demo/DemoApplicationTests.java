package com.example.demo;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DemoApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Value("${app.jwt.secret}")
	private String jwtSecret;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@AfterEach
	void cleanUsers() {
		userRepository.deleteAll();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void registrationValidatesInputAndNeverReturnsPassword() throws Exception {
		mockMvc.perform(post("/api/v1/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":" ","age":0,"username":"","password":"short"}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors").isArray());

		mockMvc.perform(post("/api/v1/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(createUserJson("create-test")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.password").doesNotExist());

		com.example.demo.model.User user = userRepository.findByUsername("create-test").orElseThrow();
		org.junit.jupiter.api.Assertions.assertNotEquals("password123", user.getPassword());
		org.junit.jupiter.api.Assertions.assertTrue(passwordEncoder.matches("password123", user.getPassword()));
	}

	@Test
	void rejectsDuplicateUsernamesWithConflict() throws Exception {
		register("duplicate-test");

		mockMvc.perform(post("/api/v1/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(createUserJson("duplicate-test")))
				.andExpect(status().isConflict());
	}

	@Test
	void authenticatesAndAcceptsIssuedBearerToken() throws Exception {
		register("auth-test");
		String token = authenticate("auth-test", "password123");

		mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("auth-test"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void rejectsInvalidCredentials() throws Exception {
		mockMvc.perform(post("/api/v1/auth/token")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"unknown","password":"wrong"}
						"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.detail").value("Invalid username or password."));
	}

	@Test
	void publishesVersionedRoutesAndBearerSchemeInOpenApi() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
				.andExpect(jsonPath("$.paths['/api/v1/users/me'].get.security[0].bearerAuth").exists())
				.andExpect(jsonPath("$.paths['/api/v1/auth/token'].post.responses.401").exists());
	}

	@Test
	void requiresAValidBearerTokenForProtectedRoutes() throws Exception {
		mockMvc.perform(get("/api/v1/users/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
				.andExpect(jsonPath("$.status").value(401));
		mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer invalid"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.detail").value(
						"Authentication is required or the bearer token is invalid."));

		Instant now = Instant.now();
		String expiredToken = Jwts.builder()
			.subject("expired-user")
			.issuedAt(Date.from(now.minus(2, ChronoUnit.MINUTES)))
			.expiration(Date.from(now.minus(1, ChronoUnit.MINUTES)))
			.signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)))
			.compact();
		mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + expiredToken))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void performsSelfServiceCrudWithoutGlobalListing() throws Exception {
		register("crud-test");
		String token = authenticate("crud-test", "password123");

		mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("crud-test"));

		mockMvc.perform(get("/api/v1/users").header("Authorization", "Bearer " + token))
				.andExpect(status().isMethodNotAllowed());

		mockMvc.perform(put("/api/v1/users/me")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"Updated User","age":31,"username":"crud-updated"}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("crud-updated"));

		token = authenticate("crud-updated", "password123");
		mockMvc.perform(delete("/api/v1/users/me").header("Authorization", "Bearer " + token))
				.andExpect(status().isNoContent());
	}

	private void register(String username) throws Exception {
		mockMvc.perform(post("/api/v1/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(createUserJson(username)))
				.andExpect(status().isCreated());
	}

	private String authenticate(String username, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/auth/token")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"%s","password":"%s"}
						""".formatted(username, password)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString())
				.get("accessToken").asText();
	}

	private String createUserJson(String username) {
		return """
				{"name":"Test User","age":30,"username":"%s","password":"password123"}
				""".formatted(username);
	}

}
