package com.gov.indicadores.infrastructure.adapter.out.persistence;

import com.gov.indicadores.core.dto.ConteoCategoria;
import com.gov.indicadores.core.dto.ConteoEstado;
import com.gov.indicadores.core.dto.PuntoTendencia;
import com.gov.indicadores.core.ports.out.IndicadorReadPort;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Adapter de salida (JDBC) de las consultas agregadas del modelo analítico. */
@Repository
public class IndicadorReadJdbcAdapter implements IndicadorReadPort {

    private final JdbcTemplate jdbc;

    public IndicadorReadJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<ConteoEstado> conteoPorEstado() {
        return jdbc.query(
                "SELECT estado, COUNT(*) AS total FROM solicitud_actual GROUP BY estado ORDER BY estado",
                (rs, i) -> new ConteoEstado(rs.getString("estado"), rs.getLong("total")));
    }

    @Override
    public List<ConteoCategoria> conteoPorCategoria() {
        return jdbc.query(
                "SELECT categoria_id, COUNT(*) AS total FROM solicitud_actual "
                        + "WHERE categoria_id IS NOT NULL GROUP BY categoria_id ORDER BY categoria_id",
                (rs, i) -> new ConteoCategoria(rs.getLong("categoria_id"), rs.getLong("total")));
    }

    @Override
    public List<PuntoTendencia> tendenciaDiaria() {
        return jdbc.query(
                "SELECT f.fecha AS fecha, SUM(ft.conteo) AS total "
                        + "FROM fact_transicion ft JOIN dim_fecha f ON ft.dim_fecha_id = f.id "
                        + "GROUP BY f.fecha ORDER BY f.fecha",
                (rs, i) -> new PuntoTendencia(rs.getDate("fecha").toLocalDate().toString(), rs.getLong("total")));
    }
}
