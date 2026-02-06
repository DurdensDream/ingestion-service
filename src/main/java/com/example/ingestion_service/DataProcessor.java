package com.example.ingestion_service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;

@Service
public class DataProcessor {

    private final TransactionRepository repository;
    private final RestHighLevelClient openSearchClient;

    public DataProcessor(TransactionRepository repository, RestHighLevelClient openSearchClient) {
        this.repository = repository;
        this.openSearchClient = openSearchClient;
    }

    @KafkaListener(topics = "raw-data", groupId = "ingestion-group")
    public void consume(String message) {
        System.out.println("Consumed: " + message);

        try {
            // parse
            String[] parts = message.split(",");
            parts[0] = parts[0].replace("\uFEFF", "");
            if (parts.length != 2) {
                System.err.println("Invalid message format: " + message);
                return;
            }
            String user = parts[0];
            Double amount = Double.parseDouble(parts[1]);

            // validate
            if (amount < 0) {
                System.err.println("Validation Error: Negative amount for " + user);
                return;
            }

            // save to db
            TransactionEntry entry = new TransactionEntry();

            entry.setUsername(user);
            entry.setAmount(amount);
            repository.save(entry);
            System.out.println(">>SUCCESS: Valid Data for " + user + " with amount " + amount);

            // sync to opensearch

            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("username", user);
            jsonMap.put("amount", amount);

            IndexRequest request = new IndexRequest("transactions");
            request.source(jsonMap, XContentType.JSON);

            openSearchClient.index(request, RequestOptions.DEFAULT);
            System.out.println(">>SUCCESS: Indexed to OpenSearch: " + user);

        } catch (Exception e) {
            System.err.println("Error processing message: " + message);
        }
    }
}