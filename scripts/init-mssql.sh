#!/usr/bin/env bash
# Espera a que SQL Server esté disponible y crea las dos bases de datos del reto.
# Se ejecuta como contenedor de un solo uso (mssql-init) antes de arrancar los servicios.
set -euo pipefail

SQLCMD=/opt/mssql-tools/bin/sqlcmd
HOST=sqlserver
USER=sa

echo "Esperando a SQL Server en ${HOST}..."
for i in $(seq 1 60); do
  if "$SQLCMD" -S "$HOST" -U "$USER" -P "$SA_PASSWORD" -Q "SELECT 1" >/dev/null 2>&1; then
    echo "SQL Server disponible."
    break
  fi
  echo "  intento $i/60..."
  sleep 2
done

echo "Creando bases de datos (si no existen)..."
"$SQLCMD" -S "$HOST" -U "$USER" -P "$SA_PASSWORD" -Q "IF DB_ID(N'solicitudes') IS NULL CREATE DATABASE solicitudes;"
"$SQLCMD" -S "$HOST" -U "$USER" -P "$SA_PASSWORD" -Q "IF DB_ID(N'indicadores') IS NULL CREATE DATABASE indicadores;"
echo "Bases de datos listas: solicitudes, indicadores."
