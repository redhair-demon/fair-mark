package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 3);

        // Пример документа в формате JSON
        String document = """
            {
                "description": { "participantInn": "1234567890" },
                "doc_id": "doc123",
                "doc_status": "NEW",
                "doc_type": "LP_INTRODUCE_GOODS",
                "importRequest": true,
                "owner_inn": "1234567890",
                "participant_inn": "1234567890",
                "producer_inn": "1234567890",
                "production_date": "2024-09-20",
                "production_type": "OWN_PRODUCTION",
                "products": [
                    {
                        "certificate_document": "doc_cert",
                        "certificate_document_date": "2024-09-20",
                        "certificate_document_number": "cert123",
                        "owner_inn": "1234567890",
                        "producer_inn": "1234567890",
                        "production_date": "2024-09-20",
                        "tnved_code": "tnved123",
                        "uit_code": "uit123",
                        "uitu_code": "uitu123"
                    }
                ],
                "reg_date": "2024-09-20",
                "reg_number": "reg123"
            }
        """;

        String signature = "signature";

        ObjectMapper objectMapper = new ObjectMapper();
        Runnable task = () -> {
            try {
                String response = crptApi.createDocument(objectMapper.readValue(document, CrptApi.DocumentDto.class), signature);
                System.out.println(Thread.currentThread().getName() + " at " + System.currentTimeMillis() + " Response: " + response);
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        };

        for (int i = 0; i < 50; i++) {
            new Thread(task, String.valueOf(i)).start();
        }
    }
}
