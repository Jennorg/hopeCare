package com.esperanza.hopecare.dao;

import com.esperanza.hopecare.model.DetalleFacturaDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DetalleFacturaDAO {
    public boolean insertarDetalle(int idFactura, DetalleFacturaDTO detalle, Connection conn) throws SQLException {
        String sql = "INSERT INTO detalle_factura (id_factura, concepto, id_referencia, tipo_referencia, monto) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idFactura);
            ps.setString(2, detalle.getConcepto());
            ps.setInt(3, detalle.getIdReferencia());
            ps.setString(4, detalle.getTipoReferencia());
            ps.setDouble(5, detalle.getMonto());
            return ps.executeUpdate() == 1;
        }
    }
}
