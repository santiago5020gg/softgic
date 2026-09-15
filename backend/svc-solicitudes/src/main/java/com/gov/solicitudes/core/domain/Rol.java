package com.gov.solicitudes.core.domain;

/**
 * Roles del proceso. La autorización efectiva se aplica en el servidor (resource
 * server) mapeando estos roles desde el JWT de Keycloak (Fase 3).
 */
public enum Rol {
    SOLICITANTE,
    ANALISTA,
    SUPERVISOR
}
