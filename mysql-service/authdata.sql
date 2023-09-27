USE authdb;
INSERT INTO oauth_client_details (client_id, client_secret, web_server_redirect_uri, 
scope, access_token_validity, refresh_token_validity, resource_ids, 
authorized_grant_types, additional_information) VALUES ('mobile', 
'{bcrypt}$2a$10$gPhlXZfms0EpNHX0.HHptOhoFD1AoxSr/yUIdTqA8vtjeP4zi0DDu', 
'http://localhost:8080/code', 'READ,WRITE', '3600', '10000', 
'microservice', 'authorization_code,password,refresh_token,implicit', '{}');

 INSERT INTO permission (NAME) VALUES
 ('create'),
 ('read'),
 ('update'),
 ('delete');

 INSERT INTO role (NAME) VALUES
		('ROLE_admin'),('ROLE_user');

 INSERT INTO permission_role (PERMISSION_ID, ROLE_ID) VALUES
     (1,1), /*create-> admin */
     (2,1), /* read admin */
     (3,1), /* update admin */
     (4,1), /* delete admin */
     (2,2),  /* read User */
     (3,2),  /* update User */
     (1,2);  /* create User */


 insert into user (id, username,password, email, enabled, accountNonExpired, 
 credentialsNonExpired, accountNonLocked) VALUES ('1', 'admin',
 '{bcrypt}$2a$12$xVEzhL3RTFP1WCYhS4cv5ecNZIf89EnOW4XQczWHNB/Zi4zQAnkuS', '07.mohini@gmail.com', 
 '1', '1', '1', '1');
 
 insert into  user (id, username,password, email, enabled, accountNonExpired, 
 credentialsNonExpired, accountNonLocked) VALUES ('2', 'user', 
 '{bcrypt}$2a$12$udISUXbLy9ng5wuFsrCMPeQIYzaKtAEXNJqzeprSuaty86N4m6emW','07.mohini@gmail.com', 
 '1', '1', '1', '1');
 /*
 passowrds:
 admin - admin
 user - user
 */


INSERT INTO role_user (ROLE_ID, USER_ID)
    VALUES
    (1, 1), 
    (2, 2);