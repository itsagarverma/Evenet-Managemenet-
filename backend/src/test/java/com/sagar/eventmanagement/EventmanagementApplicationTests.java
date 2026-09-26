package com.sagar.eventmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockCookie;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Assertions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.mock.web.MockMultipartFile;
import com.sagar.eventmanagement.repository.TestimonialRepository;
import com.sagar.eventmanagement.entity.Testimonial;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Base64;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
		"DB_URL=jdbc:h2:mem:sneh-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
		"DB_USERNAME=sa", "DB_PASSWORD=", "spring.flyway.enabled=false",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"app.gallery.storage.provider=local",
		"app.upload.directory=${java.io.tmpdir}/sneh-foundation-test-uploads",
		"ADMIN_BOOTSTRAP_EMAIL=admin@example.test", "ADMIN_BOOTSTRAP_PASSWORD=temporary-test-password-123"
})
class EventmanagementApplicationTests {
	@Autowired MockMvc mvc;
	@Autowired TestimonialRepository testimonialRepository;

	@Test
	void contextLoads() {
	}

	@Test void supabaseStoragePropertiesAreRegisteredExactlyOnceWhenEnabled() {
		new org.springframework.boot.test.context.runner.ApplicationContextRunner()
				.withUserConfiguration(com.sagar.eventmanagement.gallery.storage.SupabaseStorageConfiguration.class)
				.withPropertyValues(
						"app.gallery.storage.provider=supabase",
						"app.gallery.storage.supabase.endpoint=https://storage.example.test",
						"app.gallery.storage.supabase.region=local",
						"app.gallery.storage.supabase.accessKey=test-access",
						"app.gallery.storage.supabase.secretKey=test-secret",
						"app.gallery.storage.supabase.bucket=gallery",
						"app.gallery.storage.supabase.publicUrl=https://storage.example.test/public")
				.run(context -> {
					Assertions.assertTrue(context.isRunning());
					Assertions.assertEquals(1, context.getBeanNamesForType(
							com.sagar.eventmanagement.gallery.storage.SupabaseStorageProperties.class).length);
					var properties = context.getBean(com.sagar.eventmanagement.gallery.storage.SupabaseStorageProperties.class);
					Assertions.assertEquals("https://storage.example.test", properties.getEndpoint());
					Assertions.assertEquals("local", properties.getRegion());
					Assertions.assertEquals("test-access", properties.getAccessKey());
					Assertions.assertEquals("test-secret", properties.getSecretKey());
					Assertions.assertEquals("gallery", properties.getBucket());
					Assertions.assertEquals("https://storage.example.test/public", properties.getPublicUrl());
				});
	}

	@Test void migrationsCreateContactSettingsAndUseNumericQueryBudget() throws Exception {
		String url = "jdbc:h2:mem:sneh-flyway-check;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
		try (var connection = java.sql.DriverManager.getConnection(url, "sa", "")) {
			connection.createStatement().execute("CREATE TABLE \"query\" (id BIGINT PRIMARY KEY, full_name VARCHAR(255), budget VARCHAR(120))");
			connection.createStatement().executeUpdate("INSERT INTO \"query\" (id, full_name, budget) VALUES (1, 'Existing budget', '750000.25')");
		}
		var flyway = org.flywaydb.core.Flyway.configure().dataSource(url, "sa", "")
				.locations("classpath:db/migration").load();
		flyway.baseline();
		var result = flyway.migrate();
		Assertions.assertEquals(2, result.migrationsExecuted);
		try (var connection = java.sql.DriverManager.getConnection(url, "sa", "");
			 var statement = connection.createStatement();
			 var rows = statement.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CONTACT_SETTINGS'")) {
			Assertions.assertTrue(rows.next());
			Assertions.assertEquals(1, rows.getInt(1));
		}
		try (var connection = java.sql.DriverManager.getConnection(url, "sa", "");
			 var statement = connection.createStatement();
			 var rows = statement.executeQuery("SELECT budget FROM \"query\" WHERE full_name = 'Existing budget'")) {
			Assertions.assertTrue(rows.next());
			Object budget = rows.getObject(1);
			Assertions.assertTrue(budget instanceof Number);
			Assertions.assertEquals(750000.25, ((Number) budget).doubleValue());
		}
	}

	@Test void customerRecordsAreNotPublic() throws Exception {
		mvc.perform(get("/queries/all")).andExpect(status().is4xxClientError());
		mvc.perform(get("/api/enquiries")).andExpect(status().is4xxClientError());
	}

	@Test void publicEnquirySubmissionReturnsOnlyAReceipt() throws Exception {
		var csrf = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
		String token = com.fasterxml.jackson.databind.json.JsonMapper.builder().build().readTree(csrf.getResponse().getContentAsString()).get("token").asText();
		mvc.perform(post("/queries").cookie(new MockCookie("XSRF-TOKEN", token)).header("X-XSRF-TOKEN", token)
				.contentType(MediaType.APPLICATION_JSON).content("{\"fullName\":\"Enquiry Test\",\"phone\":\"9999999999\",\"eventType\":\"Wedding\"}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").exists()).andExpect(jsonPath("$.fullName").doesNotExist());
	}

	@Test void publishedGalleryReadApiIsPublic() throws Exception {
		mvc.perform(get("/api/gallery/categories")).andExpect(status().isOk());
	}

	@Test void seededGalleryRoutesResolveOnlyTheirOwnEmptyCategory() throws Exception {
		for (String slug : new String[]{"barat", "haldi", "mehendi", "mandap", "sangeet", "birthday", "baby-shower", "show-flow", "reception"}) {
			mvc.perform(get("/api/gallery/categories/" + slug)).andExpect(status().isOk())
					.andExpect(jsonPath("$.slug").value(slug)).andExpect(jsonPath("$.images").isArray());
		}
		mvc.perform(get("/api/gallery/categories/not-a-real-category")).andExpect(status().isNotFound());
	}

	@Test void publicTestimonialsIncludePublishedRowsOnly() throws Exception {
		var visible = new Testimonial(); visible.setName("Public test client"); visible.setReview("Existing CMS content test"); visible.setPublished(true); visible.setDisplayOrder(0); testimonialRepository.save(visible);
		var hidden = new Testimonial(); hidden.setName("Hidden test client"); hidden.setReview("Must remain private"); hidden.setPublished(false); hidden.setDisplayOrder(1); testimonialRepository.save(hidden);
		mvc.perform(get("/api/testimonials")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].name").value("Public test client"));
	}

	@Test void contactSettingsArePublicReadAndAdminWriteOnly() throws Exception {
		mvc.perform(get("/api/contact-settings")).andExpect(status().isOk())
				.andExpect(jsonPath("$.whatsappUrl").value("https://wa.me/919302259211"))
				.andExpect(jsonPath("$.email").value("thesnehmoments@gmail.com"));
		mvc.perform(get("/api/admin/contact-settings")).andExpect(status().is4xxClientError());
		mvc.perform(put("/api/admin/contact-settings").contentType(MediaType.APPLICATION_JSON)
				.content("{\"whatsapp\":\"+91 9302259211\",\"whatsappUrl\":\"https://wa.me/919302259211\"}"))
				.andExpect(status().is4xxClientError());
	}

	@Test void galleryManagementRequiresAdmin() throws Exception {
		mvc.perform(get("/api/gallery/admin/categories")).andExpect(status().is4xxClientError());
	}

	@Test void adminCanLoginAndReadProtectedManagementApi() throws Exception {
		var csrf = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
		String token = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
				.readTree(csrf.getResponse().getContentAsString()).get("token").asText();
		MockCookie csrfCookie = new MockCookie("XSRF-TOKEN", token);
		mvc.perform(post("/api/auth/login").cookie(csrfCookie).header("X-XSRF-TOKEN", token).contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"admin@example.test\",\"password\":\"wrong-password\"}"))
				.andExpect(status().isUnauthorized());
		var login = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
				.cookie(csrfCookie).header("X-XSRF-TOKEN", token).contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"admin@example.test\",\"password\":\"temporary-test-password-123\"}"))
				.andExpect(status().isOk()).andReturn();
		MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
		mvc.perform(put("/api/admin/contact-settings").session(session).cookie(csrfCookie).header("X-XSRF-TOKEN", token)
				.contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"thesnehmoments@gmail.com\",\"phone\":\"+91 9302259211\",\"whatsapp\":\"+91 9302259211\",\"whatsappUrl\":\"https://wa.me/919302259211\",\"instagram\":\"https://www.instagram.com/thesnehmoments\",\"facebook\":\"\",\"website\":\"https://thesnehmoments.in/\",\"address\":\"Bhopal, Madhya Pradesh\"}"))
				.andExpect(status().isOk());
		mvc.perform(get("/api/gallery/admin/categories").session(session)).andExpect(status().isOk());
		mvc.perform(get("/api/enquiries").session(session)).andExpect(status().isOk());
		var created = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/gallery/admin/categories")
				.session(session).cookie(csrfCookie).header("X-XSRF-TOKEN", token).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Integration Test\",\"slug\":\"integration-test\",\"published\":true,\"displayOrder\":99}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.slug").value("integration-test")).andReturn();
		long categoryId = new com.fasterxml.jackson.databind.ObjectMapper().readTree(created.getResponse().getContentAsString()).get("id").asLong();
		mvc.perform(get("/api/gallery/categories/integration-test")).andExpect(status().isOk());
		byte[] png = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aVxsAAAAASUVORK5CYII=");
		var uploaded = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/gallery/admin/categories/" + categoryId + "/images")
				.file(new MockMultipartFile("files", "pixel.png", "image/png", png)).session(session).cookie(csrfCookie).header("X-XSRF-TOKEN", token))
				.andExpect(status().isOk()).andExpect(jsonPath("$.items[0].success").value(true)).andExpect(jsonPath("$.items[0].image.url").exists()).andReturn();
		long imageId = new com.fasterxml.jackson.databind.ObjectMapper().readTree(uploaded.getResponse().getContentAsString()).get("items").get(0).get("image").get("id").asLong();
		mvc.perform(put("/api/gallery/admin/images/" + imageId).session(session).cookie(csrfCookie).header("X-XSRF-TOKEN", token)
				.contentType(MediaType.APPLICATION_JSON).content("{\"published\":true,\"displayOrder\":4,\"altText\":\"Integration test image\"}")).andExpect(status().isOk());
		String mediaUrl = new com.fasterxml.jackson.databind.ObjectMapper().readTree(uploaded.getResponse().getContentAsString()).get("items").get(0).get("image").get("url").asText();
		mvc.perform(get(mediaUrl)).andExpect(status().isOk());
		mvc.perform(get("/api/gallery/categories/integration-test")).andExpect(status().isOk()).andExpect(jsonPath("$.images.length()").value(1));
		mvc.perform(put("/api/gallery/admin/categories/" + categoryId).session(session).cookie(csrfCookie).header("X-XSRF-TOKEN", token)
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Integration Test\",\"slug\":\"integration-test\",\"description\":\"Updated\",\"coverImage\":\"" + mediaUrl + "\",\"published\":true,\"displayOrder\":1}"))
				.andExpect(status().isOk());
		mvc.perform(get("/api/gallery/categories/integration-test")).andExpect(status().isOk()).andExpect(jsonPath("$.description").value("Updated"))
				.andExpect(jsonPath("$.coverImage").value(mediaUrl));
		mvc.perform(put("/api/gallery/admin/images/" + imageId).session(session).cookie(csrfCookie).header("X-XSRF-TOKEN", token)
				.contentType(MediaType.APPLICATION_JSON).content("{\"published\":false}")).andExpect(status().isOk());
		mvc.perform(get("/api/gallery/categories/integration-test")).andExpect(status().isOk()).andExpect(jsonPath("$.images.length()").value(0));
		var partial = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/gallery/admin/categories/" + categoryId + "/images")
				.file(new MockMultipartFile("files", "bad.txt", "text/plain", "bad".getBytes()))
				.file(new MockMultipartFile("files", "pixel.png", "image/png", png)).session(session).cookie(csrfCookie).header("X-XSRF-TOKEN", token))
				.andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(2))
				.andExpect(jsonPath("$.items[0].success").value(false)).andExpect(jsonPath("$.items[1].success").value(true)).andReturn();
		long partialImageId = new com.fasterxml.jackson.databind.ObjectMapper().readTree(partial.getResponse().getContentAsString()).get("items").get(1).get("image").get("id").asLong();
		mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/gallery/admin/categories/" + categoryId + "/images")
				.file(new MockMultipartFile("files", "wrong.txt", "text/plain", "not image".getBytes())).session(session).cookie(csrfCookie).header("X-XSRF-TOKEN", token))
				.andExpect(status().isOk()).andExpect(jsonPath("$.items[0].success").value(false))
				.andExpect(jsonPath("$.items[0].error").value("Only JPEG, PNG, and GIF images are allowed"));
		mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/gallery/admin/categories/" + categoryId + "/images")
				.file(new MockMultipartFile("files", "too-large.png", "image/png", new byte[10 * 1024 * 1024 + 1])).session(session).cookie(csrfCookie).header("X-XSRF-TOKEN", token))
				.andExpect(status().isOk()).andExpect(jsonPath("$.items[0].success").value(false))
				.andExpect(jsonPath("$.items[0].error").value("Each image must be at most 10 MB"));
		mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/gallery/admin/images/" + partialImageId)
				.session(session).cookie(csrfCookie).header("X-XSRF-TOKEN", token)).andExpect(status().isOk());
		mvc.perform(get("/api/admin/contact-settings").session(session)).andExpect(status().isOk());
		mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/gallery/admin/categories/" + categoryId)
				.session(session).cookie(csrfCookie).header("X-XSRF-TOKEN", token)).andExpect(status().isOk());
		mvc.perform(post("/api/auth/logout").session(session).cookie(csrfCookie).header("X-XSRF-TOKEN", token)).andExpect(status().isOk());
		mvc.perform(get("/api/enquiries").session(session)).andExpect(status().is4xxClientError());
	}

}
