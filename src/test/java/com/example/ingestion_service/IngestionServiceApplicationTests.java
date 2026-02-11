package com.example.ingestion_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class IngestionServiceApplicationTests {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
			DockerImageName.parse("postgres:15-alpine"));

	@Container
	static KafkaContainer kafka = new KafkaContainer(
			DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
	}

	@TestConfiguration
	static class TestOpenSearchConfig {
		@Bean
		@Primary
		public RestHighLevelClient testOpenSearchClient() {
			// Provide a dummy client; OpenSearch is not required for this integration test
			return new RestHighLevelClient(
					RestClient.builder(new HttpHost("localhost", 19200, "http")));
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void uploadCsvReturns202() throws Exception {
		String csvContent = "User1,100.50\nUser2,200.75\n";
		MockMultipartFile file = new MockMultipartFile(
				"file", "test.csv", "text/csv", csvContent.getBytes());

		mockMvc.perform(multipart("/api/ingest/upload").file(file))
				.andExpect(status().isAccepted());
	}

	@Test
	void uploadEmptyFileReturnsBadRequest() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file", "empty.csv", "text/csv", new byte[0]);

		mockMvc.perform(multipart("/api/ingest/upload").file(file))
				.andExpect(status().isBadRequest());
	}
}
