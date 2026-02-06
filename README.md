# High Volume Ingestion Service 🚀

A distributed system built with **Java 21** and **Spring Boot 3** to handle high-throughput data ingestion.

## ✨ Features
*   **Streaming Ingestion:** Processes large CSV files line-by-line using `InputStream` (Low Memory Footprint).
*   **Async Processing:** Decouples upload from processing using **Kafka**.
*   **Reliable Storage:** Persists valid transactions to **PostgreSQL**.
*   **Search Engine:** Syncs data to **OpenSearch** for real-time querying.

## 🛠️ Tech Stack
*   **Java 21** (Virtual Threads enabled)
*   **Spring Boot 3.x** (Web, Data JPA, Kafka)
*   **Apache Kafka** (Message Broker)
*   **PostgreSQL** (Relational DB)
*   **OpenSearch** (Search Engine)
*   **Docker Compose** (Infrastructure)

## 🏃‍♂️ How to Run

1.  **Start Infrastructure:**
    ```bash
    docker-compose up -d
    ```
    *(Starts Kafka, Zookeeper, Postgres, OpenSearch)*

2.  **Run Application:**
    ```bash
    ./mvnw spring-boot:run
    ```

## 🔌 API Endpoints

### 1. Upload CSV
```bash
curl -X POST -F "file=@large_data.csv" http://localhost:8080/api/ingest/upload
```

### 2. Search Users
```bash
curl "http://localhost:8080/api/ingest/search?user=User100"
```
