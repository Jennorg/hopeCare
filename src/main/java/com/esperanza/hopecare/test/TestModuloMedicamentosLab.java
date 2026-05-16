package com.esperanza.hopecare.test;

import com.esperanza.hopecare.common.db.DatabaseConnection;
import com.esperanza.hopecare.modules.medicamentos_lab.dao.*;
import com.esperanza.hopecare.modules.medicamentos_lab.model.*;
import com.esperanza.hopecare.modules.medicamentos_lab.service.*;
import com.esperanza.hopecare.modules.medicamentos_lab.facade.GestionClinicaFacade;
import java.sql.Connection;
import java.sql.SQLException;

public class TestModuloMedicamentosLab {

    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("=== TEST MÓDULO MEDICAMENTOS Y LABORATORIO ===\n");

            testListarMedicamentos();
            testListarExamenes();
            testStockBajo();
            testListarEntregas();
            testSolicitarExamen(conn);
            testListarSolicitudesPendientes();
            testProcesarEntrega();
            testRegistrarResultado();
            testVerificarStock();

            System.out.println("\n=== TODAS LAS PRUEBAS COMPLETADAS ===");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void testListarMedicamentos() {
        System.out.println("\n[1] Listar medicamentos:");
        InventarioService svc = new InventarioService();
        var lista = svc.listarMedicamentos();
        if (lista.isEmpty()) {
            System.out.println("  FALLO: No hay medicamentos");
        } else {
            for (Medicamento m : lista) {
                System.out.println("  OK: " + m.getNombreComercial() + " (stock: " + m.getStockActual() + ", min: " + m.getStockMinimo() + ")");
            }
        }
    }

    static void testListarExamenes() {
        System.out.println("\n[2] Listar exámenes:");
        ExamenService svc = new ExamenService();
        var lista = svc.listarExamenes();
        if (lista.isEmpty()) {
            System.out.println("  FALLO: No hay exámenes");
        } else {
            for (ExamenLaboratorio e : lista) {
                System.out.println("  OK: " + e.getNombreExamen() + " ($" + e.getPrecio() + ")");
            }
        }
    }

    static void testStockBajo() {
        System.out.println("\n[3] Stock bajo:");
        InventarioService svc = new InventarioService();
        var lista = svc.listarStockBajo();
        for (Medicamento m : lista) {
            System.out.println("  OK (stock bajo): " + m.getNombreComercial() + " (" + m.getStockActual() + "/" + m.getStockMinimo() + ")");
        }
        if (lista.isEmpty()) {
            System.out.println("  INFO: Ningún medicamento con stock bajo");
        }
    }

    static void testListarEntregas() {
        System.out.println("\n[4] Listar entregas de medicamentos:");
        EntregaMedicamentoDAO dao = new EntregaMedicamentoDAO();
        var lista = dao.listarTodas();
        if (lista.isEmpty()) {
            System.out.println("  INFO: No hay entregas registradas");
        } else {
            for (EntregaMedicamento e : lista) {
                System.out.println("  OK: Entrega #" + e.getIdEntrega() + " - Paciente: " + e.getPacienteNombreCompleto() + " - Medicamento: " + e.getMedicamentoNombre());
            }
        }
    }

    static void testSolicitarExamen(Connection conn) throws SQLException {
        System.out.println("\n[5] Solicitar examen:");
        ExamenService svc = new ExamenService();
        boolean ok = svc.solicitarExamen(1, 1);
        if (ok) {
            System.out.println("  OK: Solicitud de Hemograma creada para paciente 1");
        } else {
            System.out.println("  FALLO: No se pudo solicitar examen");
        }
    }

    static void testListarSolicitudesPendientes() {
        System.out.println("\n[6] Listar solicitudes pendientes:");
        SolicitudExamenDAO dao = new SolicitudExamenDAO();
        var lista = dao.listarPendientes();
        if (lista.isEmpty()) {
            System.out.println("  INFO: No hay solicitudes pendientes");
        } else {
            for (SolicitudExamen s : lista) {
                System.out.println("  OK: Solicitud #" + s.getIdSolicitud() + " - Paciente: " + s.getPacienteNombreCompleto() + " - Examen: " + s.getExamenNombre() + " - Estado: " + s.getEstado());
            }
        }
    }

    static void testProcesarEntrega() {
        System.out.println("\n[7] Procesar entrega (facade):");
        GestionClinicaFacade facade = new GestionClinicaFacade();
        boolean ok = facade.procesarEntregaMedicamento(1, 1, 2, true, "FARMACIA");
        if (ok) {
            System.out.println("  OK: Entrega registrada para paciente #1 (med #1, cant 2)");
        } else {
            System.out.println("  FALLO: No se pudo procesar entrega (¿stock insuficiente?)");
        }
    }

    static void testRegistrarResultado() {
        System.out.println("\n[8] Registrar resultado examen (facade):");
        GestionClinicaFacade facade = new GestionClinicaFacade();
        SolicitudExamenDAO sdao = new SolicitudExamenDAO();
        var pendientes = sdao.listarPendientes();
        if (pendientes.isEmpty()) {
            System.out.println("  SKIP: No hay solicitudes pendientes");
            return;
        }
        int idSolicitud = pendientes.get(0).getIdSolicitud();
        boolean ok = facade.registrarResultadoExamen(idSolicitud, "Resultado normal. Glucosa: 85 mg/dL", "COMPLETADO", "LABORATORIO");
        if (ok) {
            System.out.println("  OK: Resultado registrado para Solicitud #" + idSolicitud);
        } else {
            System.out.println("  FALLO: No se pudo registrar resultado");
        }
    }

    static void testVerificarStock() {
        System.out.println("\n[9] Verificar stock:");
        InventarioService svc = new InventarioService();
        boolean hayStock = svc.verificarStock(1, 5);
        System.out.println("  OK: ¿Hay 5 unidades del medicamento #1? " + hayStock);
        boolean sinStock = svc.verificarStock(1, 999);
        System.out.println("  OK: ¿Hay 999 unidades? " + sinStock);
    }
}