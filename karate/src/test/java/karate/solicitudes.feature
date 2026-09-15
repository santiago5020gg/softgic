Feature: Solicitudes — A1 (registro) y recorrido REGISTRADA -> EN_ATENCION -> RESUELTA

Background:
  * url solicitudesBase

Scenario: A1 - un SOLICITANTE registra una solicitud valida y luego se atiende y resuelve
  # A1: crear -> 201 REGISTRADA
  Given path 'solicitudes'
  And header Authorization = 'Bearer ' + tokenSolicitante
  And request { asunto: 'Falla de red en piso 2', descripcion: 'Sin conexion en la sala 204', categoriaId: 1, prioridad: 'ALTA' }
  When method post
  Then status 201
  And match response.estado == 'REGISTRADA'
  And match response.codigo == '#present'
  And match response.historial == '#[_ > 0]'
  * def id = response.id

  # Recorrido: ANALISTA toma -> 200 EN_ATENCION
  Given path 'solicitudes', id, 'asignaciones'
  And header Authorization = 'Bearer ' + tokenAnalista
  When method post
  Then status 200
  And match response.estado == 'EN_ATENCION'

  # ANALISTA resuelve -> 200 RESUELTA
  Given path 'solicitudes', id, 'transiciones'
  And header Authorization = 'Bearer ' + tokenAnalista
  And request { accion: 'RESOLVER', observacion: 'Se restablecio el enlace' }
  When method post
  Then status 200
  And match response.estado == 'RESUELTA'

Scenario: El catalogo de categorias solo devuelve activas
  Given path 'categorias'
  And header Authorization = 'Bearer ' + tokenSolicitante
  When method get
  Then status 200
  And match each response contains { activo: true }
