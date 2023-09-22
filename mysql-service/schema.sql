CREATE DATABASE bus_reservation_db;
USE bus_reservation_db;
CREATE TABLE bus_route (
  bus_number VARCHAR(10) PRIMARY KEY,
  start_point VARCHAR(500) NOT NULL,
  end_point VARCHAR(500) NOT NULL,
  price_per_ticket DOUBLE(5,2) NOT NULL,
  total_seats INT NOT NULL
);

CREATE TABLE bus_inventory (
  bus_number VARCHAR(10) PRIMARY KEY,
  available_seats INT NOT NULL,
  last_updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  foreign key(bus_number) references bus_route(bus_number)
);

CREATE TABLE booking_details (
  booking_number INT PRIMARY KEY,
  bus_number VARCHAR(10) NOT NULL,
  start_point VARCHAR(500) NOT NULL,
  end_point VARCHAR(500) NOT NULL,
  no_of_seats INT NOT NULL,
  booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  foreign key(bus_number) references bus_route(bus_number)
);

CREATE TABLE booking_status (
  status_id INT PRIMARY KEY,
  booking_number INT NOT NULL,
  booking_status VARCHAR(50) NOT NULL,
  created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  foreign key(booking_number) references booking_details(booking_number)
);

CREATE TABLE passenger_details (
  passenger_id INT PRIMARY KEY,
  booking_number INT NOT NULL,
  foreign key(booking_number) references booking_details(booking_number)
);


CREATE TABLE payment_details (
  payment_number INT PRIMARY KEY,
  booking_number INT NOT NULL,
  payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  amount DOUBLE(5,2) NOT NULL,
  foreign key(booking_number) references booking_details(booking_number)
);


CREATE TABLE payment_status (
  status_id INT PRIMARY KEY,
  payment_number INT NOT NULL,
  payment_status VARCHAR(50) NOT NULL,
  created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  foreign key(payment_number) references payment_details(payment_number)
);

CREATE TABLE inventory_update_log (
  log_id INT PRIMARY KEY,
  booking_number INT NOT NULL,
  no_of_seats INT NOT NULL,
  operation VARCHAR(50) NOT NULL,
  created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  foreign key(booking_number) references booking_details(booking_number)
);