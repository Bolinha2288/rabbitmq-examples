This project consists of multiple services (`email-manager`, `account-manager`, and `user-manager`) that can be deployed using Docker and Docker Compose.

## Prerequisites

1. Install [Docker](https://docs.docker.com/get-docker/).
2. Install [Docker Compose](https://docs.docker.com/compose/install/).
3. Ensure the JAR files for each service are built using Maven:
   ```bash
   cd account-manager
   mvn clean package
   ``` 
   ```bash
   cd email-manager
   mvn clean package
   ```
   ```bash
   cd users-manager
   mvn clean package
   ```
## Running the Services with Docker Compose

   **Note** For send email correctly just configure `docker-compose.yaml`, modify email-manager-service adding your service SMTP and authetication data.

1. Navigate to the **infrastructure** directory where the `docker-compose.yaml` file is located:
    ```bash
    cd infrastructure
    ```
2. Start the services using Docker Compose:
    ```bash
    docker-compose up --build -d
    ```
3. Checking the logs all services are running:
   ```bash
    docker-compose logs -f
    ```
## Accessing the Services


1. Create new user through swagger:

    - **User Manager:** http://localhost:8090/swagger-ui/index.html


2. Checking a topic created, consumers created, and messages received in Apache Kafka:

    - **Kafka UI:** http://localhost:8000
