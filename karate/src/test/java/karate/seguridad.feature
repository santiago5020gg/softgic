Feature: Seguridad — A3 (autorizacion por rol) y 401 sin token

Background:
  * url solicitudesBase

Scenario: A3 - un SOLICITANTE no puede cerrar una solicitud (403), sin cambio
  # Prepara una solicitud (creada por el solicitante)
  Given path 'solicitudes'
  And header Authorization = 'Bearer ' + tokenSolicitante
  And request { asunto: 'Intento de cierre no autorizado', descripcion: 'RBAC', categoriaId: 1, prioridad: 'BAJA' }
  When method post
  Then status 201
  * def id = response.id

  # El SOLICITANTE intenta cerrar -> 403
  Given path 'solicitudes', id, 'transiciones'
  And header Authorization = 'Bearer ' + tokenSolicitante
  And request { accion: 'CERRAR' }
  When method post
  Then status 403

  # La solicitud sigue en REGISTRADA (no hubo cambio)
  Given path 'solicitudes', id
  And header Authorization = 'Bearer ' + tokenSolicitante
  When method get
  Then status 200
  And match response.estado == 'REGISTRADA'

Scenario: Sin token la API responde 401
  Given path 'solicitudes'
  When method get
  Then status 401
