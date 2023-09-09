CREATE DATABASE bus_master_data;
USE bus_master_data;
CREATE TABLE bus_route (
  bus_number VARCHAR(10) PRIMARY KEY,
  start_point VARCHAR(500) NOT NULL,
  end_point VARCHAR(5000) NOT NULL,
  price DOUBLE(5,2) NOT NULL,
  total_seats INT NOT NULL
);