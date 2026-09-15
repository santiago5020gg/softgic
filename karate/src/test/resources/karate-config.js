function fn() {
  var env = karate.env || 'local';
  karate.log('karate.env =', env);

  var solicitudesBase = java.lang.System.getenv('SOLICITUDES_URL') || 'http://svc-solicitudes:8080/api/v1';
  var tokenUrl = java.lang.System.getenv('TOKEN_URL')
    || 'http://keycloak:8080/realms/solicitudes/protocol/openid-connect/token';

  var config = {
    solicitudesBase: solicitudesBase,
    tokenUrl: tokenUrl
  };

  // Obtiene un access_token por password grant para el usuario dado (client publico).
  function token(user) {
    var body = 'grant_type=password&client_id=solicitudes-shell'
      + '&username=' + user + '&password=Password123!';
    var res = karate.http(tokenUrl)
      .header('Content-Type', 'application/x-www-form-urlencoded')
      .post(body);
    if (res.status !== 200) {
      throw new Error('No se pudo obtener token para ' + user + ' (status ' + res.status + ')');
    }
    return res.body.access_token;
  }

  config.tokenSolicitante = token('ana.solicitante');
  config.tokenAnalista = token('carlos.analista');
  config.tokenSupervisor = token('sofia.supervisor');

  karate.configure('connectTimeout', 10000);
  karate.configure('readTimeout', 10000);
  return config;
}
