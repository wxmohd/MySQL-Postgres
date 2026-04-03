-- PostgreSQL ID Generation Test Tables
-- Run this in PostgreSQL

-- Table 1: SERIAL (auto-increment)
DROP TABLE IF EXISTS test_serial;
CREATE TABLE test_serial (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table 2: UUID (v4 random)
DROP TABLE IF EXISTS test_uuid;
CREATE TABLE test_uuid (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table 3: SEQUENCE with allocation size
DROP SEQUENCE IF EXISTS test_seq_1;
CREATE SEQUENCE test_seq_1 START 1 INCREMENT 1;

DROP TABLE IF EXISTS test_sequence;
CREATE TABLE test_sequence (
    id INT PRIMARY KEY,
    name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- For allocation size test (batch size 50)
DROP SEQUENCE IF EXISTS test_seq_50;
CREATE SEQUENCE test_seq_50 START 1 INCREMENT 50;

DROP TABLE IF EXISTS test_batch_sequence;
CREATE TABLE test_batch_sequence (
    id INT PRIMARY KEY,
    name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table 4: IDENTITY (similar to MySQL auto_increment)
DROP TABLE IF EXISTS test_identity;
CREATE TABLE test_identity (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Enable extension for UUID if not exists
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Sequence for allocation size testing
DROP SEQUENCE IF EXISTS test_seq_alloc;
CREATE SEQUENCE test_seq_alloc START 1 INCREMENT 1;

DROP TABLE IF EXISTS test_sequence_alloc;
CREATE TABLE test_sequence_alloc (
    id INT PRIMARY KEY,
    name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_test_identity_created_at ON test_identity(created_at);
CREATE INDEX IF NOT EXISTS idx_test_uuid_created_at ON test_uuid(created_at);
CREATE INDEX IF NOT EXISTS idx_test_serial_created_at ON test_serial(created_at);
CREATE INDEX IF NOT EXISTS idx_test_sequence_created_at ON test_sequence(created_at);
CREATE INDEX IF NOT EXISTS idx_test_sequence_alloc_created_at ON test_sequence_alloc(created_at);
