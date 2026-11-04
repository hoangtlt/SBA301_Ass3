-- Author: HoangTLT
-- Run in SQL Server Management Studio before starting the SQL Server profile.
-- Replace StudentName_ClassCode with your own student name and class code.
USE master;
GO
IF DB_ID(N'A3StudentName_ClassCode') IS NULL
BEGIN
    CREATE DATABASE [A3StudentName_ClassCode];
END;
GO
USE [A3StudentName_ClassCode];
GO
-- Spring Data JPA/Hibernate creates the five tables and foreign keys on startup.
-- No manual CREATE TABLE is needed (the assignment explicitly requests JPA).
