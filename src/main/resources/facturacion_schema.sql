-- Tabla: factura
CREATE TABLE IF NOT EXISTS factura (
    id_factura INTEGER PRIMARY KEY AUTOINCREMENT,
    id_paciente INTEGER NOT NULL,
    fecha_emision DATETIME DEFAULT CURRENT_TIMESTAMP,
    subtotal REAL NOT NULL,
    impuesto REAL NOT NULL,
    total REAL NOT NULL,
    estado_pago TEXT DEFAULT 'PENDIENTE' -- PENDIENTE, PAGADA, ANULADA
);

-- Tabla: detalle_factura
CREATE TABLE IF NOT EXISTS detalle_factura (
    id_detalle INTEGER PRIMARY KEY AUTOINCREMENT,
    id_factura INTEGER NOT NULL,
    concepto TEXT NOT NULL,
    id_referencia INTEGER,
    tipo_referencia TEXT, -- CONSULTA, EXAMEN, etc.
    monto REAL NOT NULL,
    FOREIGN KEY (id_factura) REFERENCES factura(id_factura)
);
