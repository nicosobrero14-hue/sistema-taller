@echo off
REM Levanta todo el sistema. Antes hay que copiar .env.ejemplo como .env
REM y completarlo.

if not exist .env (
    echo Falta el archivo .env. Copia .env.ejemplo y completalo.
    exit /b 1
)

for /f "usebackq tokens=1,* delims==" %%a in (".env") do (
    if not "%%a"=="" if not "%%a:~0,1"=="#" set "%%a=%%b"
)

echo Arrancando eureka-server...
start "eureka-server" cmd /c "cd eureka-server && mvnw spring-boot:run"
timeout /t 25 /nobreak > nul

for %%s in (usuarios-service clientes-service taller-service inventario-service pagos-service ventas-service api-gateway) do (
    echo Arrancando %%s...
    start "%%s" cmd /c "cd %%s && mvnw spring-boot:run"
    timeout /t 5 /nobreak > nul
)

echo.
echo Listo. El gateway queda en http://localhost:8080
echo Para la interfaz: cd frontend ^&^& npm run dev
