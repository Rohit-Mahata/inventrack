@echo off
setlocal

set "CP=out;lib\sqlite-jdbc-3.45.1.0.jar;lib\slf4j-api-1.7.36.jar;lib\slf4j-simple-1.7.36.jar"

if not exist "out" (
  echo [ERROR] out folder not found. Compile first.
  exit /b 1
)

if not exist "lib\sqlite-jdbc-3.45.1.0.jar" (
  echo [ERROR] Missing lib\sqlite-jdbc-3.45.1.0.jar
  exit /b 1
)

if not exist "lib\slf4j-api-1.7.36.jar" (
  echo [ERROR] Missing lib\slf4j-api-1.7.36.jar
  exit /b 1
)

if not exist "lib\slf4j-simple-1.7.36.jar" (
  echo [ERROR] Missing lib\slf4j-simple-1.7.36.jar
  exit /b 1
)

echo Running with classpath:
echo %CP%
java -cp "%CP%" com.inventory.main.App
