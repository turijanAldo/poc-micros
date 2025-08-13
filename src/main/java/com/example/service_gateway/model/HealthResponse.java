package com.example.service_gateway.model;

public class HealthResponse {
        private String status;
        private String message;
        private String responseTime;
        private Long testUserId;

        public HealthResponse(String status, String message, String responseTime, Long testUserId) {
            this.status = status;
            this.message = message;
            this.responseTime = responseTime;
            this.testUserId = testUserId;
        }

        // Getters para serialización JSON
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public String getResponseTime() { return responseTime; }
        public Long getTestUserId() { return testUserId; }
}
