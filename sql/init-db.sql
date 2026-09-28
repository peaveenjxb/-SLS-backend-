-- Run as a MySQL admin (e.g. `mysql -u root -p < sql/init-db.sql`).
-- Creates the app's database and a dedicated, least-privilege user for it.
-- Change the password before using this anywhere beyond local development.

CREATE DATABASE IF NOT EXISTS jwtauthdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'jwtauth_app'@'localhost' IDENTIFIED BY 'JwtAuth_App_2026!';
GRANT ALL PRIVILEGES ON jwtauthdb.* TO 'jwtauth_app'@'localhost';
FLUSH PRIVILEGES;
