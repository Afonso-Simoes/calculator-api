# Calculator API

## Project Context

This project implements a RESTful Calculator API that supports basic arithmetic operations (sum, subtraction, multiplication, division) with arbitrary precision decimal numbers. It is built with Spring Boot, communicates internally via Apache Kafka, and demonstrates modular architecture with `rest` and `calculator` modules.

## Main Features

- **REST Endpoints:** Supports operations `sum`, `subtract`, `multiply`, and `divide` via HTTP GET requests.
- **Kafka-based Communication:** Inter-module communication uses Apache Kafka for request/response handling.
- **Arbitrary Precision:** Uses `BigDecimal` for precise decimal calculations.
- **Unique Request Tracking:** Each request has a unique identifier propagated via MDC and response headers.
- **Logging:** SLF4J with MDC integration logs all key events and errors.
- **Dockerized:** Comes with Dockerfiles and Docker Compose for easy setup.
- **Tests:** Comprehensive unit tests covering core logic and Kafka messaging.
- **Timeout Handling:** Handles Kafka communication timeouts gracefully and returns appropriate HTTP codes.

## How to Run the Project

### Prerequisites

- Git  
- Docker & Docker Compose

### Steps

1. **Clone the repository:**

    ```bash
    git clone https://github.com/Afonso-Simoes/calculator-api.git
    ```

2. **Build services**

    ```bash
    docker-compose build
    ```

    This will:

    - Build images and containers
    - Run tests

3. **Start services**

    ```bash
    docker-compose up
    ```

    This will start:

    - Kafka broker
    - Calculator module
    - REST API module


## API Usage Examples

### Sum

```http
GET http://localhost:8080/sum?a=1.5&b=2.5
```
Response: 
{
  "status": "OK",
  "payload": "4.0"
}

### Subtract

```http
GET http://localhost:8080/subtract?a=1&b=3
```
Response: 
{
  "status": "OK",
  "payload": "-2"
}

### Multiply

```http
GET http://localhost:8080/multiply?a=2&b=3
```
Response: 
{
  "status": "OK",
  "payload": "6"
}

### Divide

```http
GET http://localhost:8080/divide?a=1&b=5
```
Response: 
{
  "status": "OK",
  "payload": "0.2"
}

### Divide by Zero Error

```http
GET http://localhost:8080/divide?a=1&b=0
```
Response: 
{
  "status": "ERROR",
  "payload": "Division by zero is not allowed."
}

## Running Tests

To run tests inside Docker, use:

```bash
docker-compose run --rm rest-module mvn clean test
```
