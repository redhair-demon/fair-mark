package org.example;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class CrptApi {

    private final HttpClient httpClient;
    private final int requestLimit;
    private final TimeUnit timeUnit;
    private final ReentrantLock lock;
    private final Semaphore semaphore;
    private final ScheduledExecutorService schedule;
    private final URI apiUri;
    private final ObjectMapper objectMapper;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = Objects.requireNonNull(timeUnit, "TimeUnit cannot be null");
        this.requestLimit = requestLimit;
        this.httpClient = HttpClient.newHttpClient();
        this.lock = new ReentrantLock();
        this.semaphore = new Semaphore(requestLimit);
        this.apiUri = URI.create("https://ismp.crpt.ru/api/v3/");
        this.objectMapper = new ObjectMapper();
        this.schedule = Executors.newScheduledThreadPool(1);
        schedule.scheduleAtFixedRate(this::resetRequestLimit, 1, 1, timeUnit);
    }

    private void resetRequestLimit() {
        lock.lock();
        try {
            semaphore.release(requestLimit - semaphore.availablePermits());
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Создание документа для ввода в оборот товара
     *
     * @param document документ
     * @param signature подпись
     * @return HTTP response body as String
     * @throws InterruptedException if the operation is interrupted
     * @throws IOException if an I/O error occurs when sending or receiving
     */
    public String createDocument(DocumentDto document, String signature) throws InterruptedException, IOException {
        semaphore.acquire();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(apiUri.resolve("lk/documents/create"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + signature)
                .POST(HttpRequest
                        .BodyPublishers
                        .ofString(objectMapper
                                .writeValueAsString(document)
                        )
                )
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    @JsonAutoDetect
    public static class DocumentDto {
        public Description description;
        public String doc_id;
        public String doc_status;
        public String doc_type;
        public boolean importRequest;
        public String owner_inn;
        public String participant_inn;
        public String producer_inn;
        public String production_date;
        public String production_type;
        public Product[] products;
        public String reg_date;
        public String reg_number;

        public static class Description {
            public String participantInn;
        }
        public static class Product {
            public String certificate_document;
            public String certificate_document_date;
            public String certificate_document_number;
            public String owner_inn;
            public String producer_inn;
            public String production_date;
            public String tnved_code;
            public String uit_code;
            public String uitu_code;
        }
    }
}
