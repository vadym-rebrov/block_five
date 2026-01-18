This project is a dedicated microservice designed to handle asynchronous email notifications. It leverages Apache Kafka as a message broker for reliable event consumption and Elasticsearch for message storage and status tracking.
Technologies 
- Spring Boot
- Apache Kafka
- Elasticsearch
- Docker & Docker Compose

Getting Started

Follow these steps to set up the infrastructure and run the services.
1. Create a Shared Network
Since the services interact via Docker, you must create a shared network to allow containers from different projects to communicate.
```bash
docker network create shared-kafka-net
```
2. Setup the Producer Service (Block Two)<br/>
   2.1 Clone the repository : https://github.com/vadym-rebrov/blocktwo<br/>
   2.2 Create a .env file based on .env-example, specifying the administrator's email address.<br/>
   2.3 Run:<br/>
   
```bash
docker-compose up --build -d
```
3. Setup the Consumer Service:<br/>
   3.1 Create a .env file based on .env-example, specifying the mail service credentials.<br/>
   3.2 Run:<br/>
```bash
docker-compose up --build -d
```

To test message creation, use the following endpoint:
``` POST http://localhost:8080/api/movie ```
Body: 
```
{
  "title" : "The Way of All Flesh",
  "released" : "1999-01-21",
  "genresId" : [ 1, 2, 3, 4 ],
  "rating" : 5.4,
  "directorId" : 5,
  "awards" : [ "Palme d’Or" ]
}
```
