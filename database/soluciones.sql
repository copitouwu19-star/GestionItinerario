-- ============================================================
-- Base de datos: soluciones
-- Ejecutar en phpMyAdmin o MySQL Workbench (XAMPP)
-- ============================================================

CREATE DATABASE IF NOT EXISTS soluciones
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE soluciones;

-- ============================================================
-- Tabla: usuarios
-- Espejo de Firebase Auth para control local
-- ============================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    firebase_uid    VARCHAR(128)    NOT NULL UNIQUE,
    nombre          VARCHAR(100)    NOT NULL,
    apellido        VARCHAR(100)    DEFAULT NULL,
    email           VARCHAR(150)    NOT NULL UNIQUE,
    telefono        VARCHAR(20)     DEFAULT NULL,
    rol             ENUM('admin','tecnico','cliente') DEFAULT 'tecnico',
    empresa         VARCHAR(100)    DEFAULT NULL,
    activo          TINYINT(1)      NOT NULL DEFAULT 1,
    fecha_creacion  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso   DATETIME        DEFAULT NULL,

    INDEX idx_firebase_uid (firebase_uid),
    INDEX idx_email        (email),
    INDEX idx_rol          (rol),
    INDEX idx_activo       (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
