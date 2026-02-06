package com.example.ingestion_service;

import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/ingest")
public class IngestionController {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RestHighLevelClient openSearchClient;

    // constructor
    public IngestionController(KafkaTemplate<String, String> kafkaTemplate, RestHighLevelClient openSearchClient) {
        this.kafkaTemplate = kafkaTemplate;
        this.openSearchClient = openSearchClient;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file) {
        // check empty
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        // check csv
        if (!file.getContentType().equals("text/csv")) {
            return ResponseEntity.badRequest().body("File is not a CSV");
        }

        // read line by line
        try (BufferedReader bReader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = bReader.readLine()) != null) {
                kafkaTemplate.send("raw-data", line);
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
        return ResponseEntity.ok("File accepted for processing");
    }

    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam String user) throws Exception {
        SearchRequest searchRequest = new SearchRequest("transactions");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(QueryBuilders.matchQuery("username", user));
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = openSearchClient.search(searchRequest, RequestOptions.DEFAULT);

        List<Map<String, Object>> results = new ArrayList<>();
        searchResponse.getHits().forEach(hit -> results.add(hit.getSourceAsMap()));

        return results;
    }

}
