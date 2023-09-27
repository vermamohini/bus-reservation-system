# Introduction
This project is a simple implementation of a bus booking system which is built using concepts of microservices. Each service in this microservice architecture is created using spring boot and uses rabbit mq as message broker. The database is created using mysql. All the services are containerized using docker. These microservices uses choreographed SAGA patttern to handle any transaction merrors in the chain of communication.

# Technology Stack
* Java 8
* Spring Boot
* MySQL
* Rabbit-MQ
* Docker

# Core Services
This section outlines different services that are used for the bus booking services.

## Admin-Service
The purpose of this service is to provide endpoints to manage bus and route details. User can perform the following for bus and route details
* fetch 
* add 
* edit
* delete

This service does not produces or consumes any message to other service.

## Booking-Service
The purpose of this service to add a new booking and cancel any existing booking.

While creating booking, this service calls "busInventory/seats/{busNumber}" endpoint on inventory-service to check if there is avaiable seats for particular bus number.  In case inventory-service is not avaiable then a retry is done five times after which a fallback inventory of 0 is returned. This is implemented using resilience4j. 

This service produces message to different queues. The details are listed below

### Queues where messages are produced
#### payment-process-queue 
As soon as a new booking is made the booking-service publish a message in payment-process-queue, so that payment-service can process the payment for the booking. At this point the booking status is marked pending.

#### payment-rollback-queue
As soon as an existing booking is canceled, the booking-service publish a message in payment-rollback-queue, so that the payment-service can process the refund for the booking.

#### inventory-credit-queue
As soon as an existing booking is canceled, the booking-service publish a message in inventory-credit-queue, so that the inventory-service can add number of seats canceled to inventory (avaialable number of seats).

### Queues from where messages are consumed
#### booking-reject-queue
The message in booking-reject-queue will come when anything fails while processing the booking e.g. payment failed or number of avaialble seats are less than seats in booking. As soon as a message appears in this queue, the booking-service consumes the message and marks the booking as rejected.

#### booking-confirm-queue
The message in booking-reject-queue will come when everything succeed while processing the booking e.g. payment processed and seats are reserved in inventory. As soon as a message appears in this queue, the booking-service consumes the message and marks the booking as confirmed.

## Inventory-Service ##
The purpose of this service to maintain the number of available seats for a particular bus on a particular route (also called as inventory). As soon as a booking is made the number of seats are reduced by the number of seats in the booking. Similarly if a booking is canceled, the number of seats canceled are added back to the inventory.

This service produces message to different queues. The details are listed below:

### Queues where messages are produced
#### booking-reject-queue
If for any reason if the inventory cannot be deducted for a given booking, then this service publish a message to the booking-reject-queue so that booking service can mark the booking as rejected.

#### booking-confirm-queue
if the inventory is deducted for a given booking, then this service publish a message to the booking-confirm-queue so that booking service can mark the booking as confirmed.


#### payment-rollback-queue
If for any reason if the inventory cannot be deducted for a given booking,, the inventory-service publish a message in payment-rollback-queue, so that the payment-service can process the refund for the booking.

### Queues from where messages are consumed
#### inventory-credit-queue
The message in this queue will come if a booking is canceled. The inventory-service consumes this message and add number of seats canceled to inventory (avaialable number of seats) for the given booking.

#### inventory-debit-queue
The message in this queue will come if payment against a given booking is successful. The inventory-service consumes this message and reduces number of seats booked from inventory (avaialable number of seats) for the given booking.

## Payment-Service
The purpose of this service to record the payment against a booking. Since this is just a demo project, it does not integrate with any payment gateway and it is only limited to make an entry of payment amount in database along with status  
* sucess, indicating a successful payment and 
* refunded, indicating a successful refund.

This service produces message to different queues. The details are listed below
### Queues where messages are produced
#### inventory-debit-queue
As soon as the payment for a given booking is processed successfully, this payment-service produces a message in this queue. The inventory-service consumes this message and reduces number of seats booked from inventory (avaialable number of seats) for the given booking.

#### booking-reject-queue
If for any reason if the payment for a given booking is not successful, then this service publish a message to the booking-reject-queue so that booking service can mark the booking as rejected.


### Queues from where messages are consumed

#### payment-process-queue 
As soon as a new booking is made the booking-service publish a message in payment-process-queue, the payment-service reads from this queue and  process the payment for the given booking.

#### payment-rollback-queue
The message in this queue comes if the booking is canceled or the inventory-service cannot process the booking. The payment-service reads from this queue and process the refund for the given booking.

# Other Services
These are the services which are acting as an enablers for the microservice architecture

## Config-Service
The purpose of this service is to provide a centralized configuration to different services used in the microservice architecture.

## Eureka-Naming-Service
The purpose of this service is to enable service discovery for different services in the microservice architecture.

## Gateway-Service
The purpose of this service is to provide a single point of entry for accessing multiple service within the microservice architecture. It is repsonsible to redirect the request to the correct service and provide the response obtained from the underlying service to the caller.

## MySql Service
The purpose of this service is to provide the database for persisting the data. 

## RabbitMQ-Service
This service acts as a message broker for processing different messages and enables asynchronous calls between different microservices.

# Service Interaction Disagram

This section provides a pictorial representation of how different micorservices interacts through different queues of rabbit mq.

## Scenario where the booking is successful

![picture for sucessful booking](image.png)

## Scenario where the booking is cancelled

![picture for scessful cancellation of booking](image-2.png)


## Scenario where the booking is failed due to error in payment service

![picture where error in payment service while booking](image-6.png)


## Scenario where the booking is failed due to error in inventory service

![picture where the booking is rejected due to error in inventory service](image-5.png)

# bus-reservation-system
