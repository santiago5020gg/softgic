package com.gov.indicadores.infrastructure.adapter.out.persistence;

import com.gov.indicadores.core.ports.out.ModeloLecturaPort;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Adapter de salida (JDBC) del modelo de lectura. Usa JdbcTemplate por ser una carga
 * analítica de upserts y agregaciones, no un dominio persistente.
 */
@Repository
public class ModeloLecturaJdbcAdapter implements ModeloLecturaPort {

    private final JdbcTemplate jdbc;

    public ModeloLecturaJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean yaProcesado(String eventId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM processed_event WHERE event_id = ?", Integer.class, eventId);
        return n != null && n > 0;
    }

    @Override
    public void marcarProcesado(String eventId) {
        jdbc.update("INSERT INTO processed_event (event_id) VALUES (?)", eventId);
    }

    @Override
    public void registrarSolicitud(String aggregateId, String estado, Long categoriaId,
                                   LocalDateTime registradoEn) {
        // Upsert: robusto ante reprocesos legítimos (aunque la guarda de idempotencia ya protege).
        jdbc.update(
                "MERGE solicitud_actual AS t "
                        + "USING (SELECT ? AS aggregate_id) AS s ON t.aggregate_id = s.aggregate_id "
                        + "WHEN MATCHED THEN UPDATE SET estado = ?, categoria_id = ?, registrado_en = ? "
                        + "WHEN NOT MATCHED THEN INSERT (aggregate_id, estado, categoria_id, registrado_en) "
                        + "VALUES (?, ?, ?, ?);",
                aggregateId, estado, categoriaId, registradoEn,
                aggregateId, estado, categoriaId, registradoEn);
    }

    @Override
    public void actualizarEstado(String aggregateId, String estado) {
        jdbc.update("UPDATE solicitud_actual SET estado = ? WHERE aggregate_id = ?", estado, aggregateId);
    }

    @Override
    public void registrarHecho(LocalDateTime fecha, Long categoriaId, String estado) {
        LocalDate dia = fecha.toLocalDate();
        int dimFechaId = dia.getYear() * 10000 + dia.getMonthValue() * 100 + dia.getDayOfMonth();
        // Asegura la dimensión fecha.
        jdbc.update(
                "IF NOT EXISTS (SELECT 1 FROM dim_fecha WHERE id = ?) "
                        + "INSERT INTO dim_fecha (id, fecha, anio, mes, dia) VALUES (?, ?, ?, ?, ?);",
                dimFechaId, dimFechaId, Date.valueOf(dia),
                dia.getYear(), dia.getMonthValue(), dia.getDayOfMonth());
        Integer dimEstadoId = jdbc.queryForObject(
                "SELECT id FROM dim_estado WHERE estado = ?", Integer.class, estado);
        // dim_categoria_id se deja NULL (no se replica el catálogo aquí); la tendencia es por fecha/estado.
        jdbc.update(
                "INSERT INTO fact_transicion (dim_fecha_id, dim_categoria_id, dim_estado_id, dim_actor_rol_id, conteo) "
                        + "VALUES (?, NULL, ?, NULL, 1)",
                dimFechaId, dimEstadoId);
    }
}
