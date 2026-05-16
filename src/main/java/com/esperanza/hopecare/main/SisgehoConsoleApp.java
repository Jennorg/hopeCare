package com.esperanza.hopecare.main;

import com.esperanza.hopecare.common.db.DatabaseConnection;
import com.esperanza.hopecare.modules.pacientes_medicos.dao.MedicoDAO;
import com.esperanza.hopecare.modules.pacientes_medicos.model.Medico;
import com.esperanza.hopecare.modules.citas_consultas.presenter.CitaPresenter;
import com.esperanza.hopecare.modules.citas_consultas.view.impl.CitaConsoleView;
import com.esperanza.hopecare.modules.facturacion.service.FacturacionService;
import com.esperanza.hopecare.modules.medicamentos_lab.facade.GestionClinicaFacade;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Scanner;

public class SisgehoConsoleApp {
    private static Scanner scanner = new Scanner(System.in);
    private static MedicoDAO medicoDAO = new MedicoDAO();
    private static FacturacionService facturacionService = new FacturacionService();
    private static GestionClinicaFacade facade = new GestionClinicaFacade();

    public static void main(String[] args) {
        System.out.println("=== SISGEHO - Sistema de Gestión Hospitalaria ===\n");
        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            int opcion = Integer.parseInt(scanner.nextLine());
            switch (opcion) {
                case 1:
                    registrarMedico();
                    break;
                case 2:
                    agendarCita();
                    break;
                case 3:
                    generarFactura();
                    break;
                case 4:
                    procesarEntregaMedicamento();
                    break;
                case 5:
                    consultarDashboard();
                    break;
                case 0:
                    salir = true;
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        }
        System.out.println("¡Hasta luego!");
    }

    private static void mostrarMenu() {
        System.out.println("\n--- Menú Principal ---");
        System.out.println("1. Registrar nuevo médico");
        System.out.println("2. Agendar cita");
        System.out.println("3. Generar factura a paciente");
        System.out.println("4. Procesar entrega de medicamento (Farmacia)");
        System.out.println("5. Ver Dashboard (estadísticas)");
        System.out.println("0. Salir");
        System.out.print("Elija una opción: ");
    }

    private static void registrarMedico() {
        System.out.println("\n--- Registro de Médico ---");
        System.out.print("Documento de identidad: ");
        String documento = scanner.nextLine();
        System.out.print("ID de especialidad (1:Medicina General, 2:Pediatría, 3:Traumatología): ");
        int idEsp = Integer.parseInt(scanner.nextLine());
        System.out.print("Registro médico (ej. RM12345): ");
        String registro = scanner.nextLine();

        Medico medico = new Medico(documento, idEsp, registro);
        boolean ok = medicoDAO.insertarMedico(medico);
        if (ok) {
            System.out.println("Médico registrado exitosamente. ID Persona: " + medico.getIdPersona());
        } else {
            System.out.println("Error al registrar médico.");
        }
    }

    private static void agendarCita() {
        System.out.println("\n--- Agenda de Cita ---");
        CitaConsoleView view = new CitaConsoleView();
        CitaPresenter presenter = new CitaPresenter(view);
        // Primero obtener horarios disponibles
        System.out.print("ID del médico: ");
        int idMedico = Integer.parseInt(scanner.nextLine());
        System.out.print("Fecha (YYYY-MM-DD): ");
        LocalDate fecha = LocalDate.parse(scanner.nextLine());
        presenter.actualizarHorariosDisponibles(idMedico, fecha);
        // Luego reservar
        presenter.reservarCita();
    }

    private static void generarFactura() {
        System.out.println("\n--- Generar Factura ---");
        System.out.print("ID del paciente: ");
        int idPaciente = Integer.parseInt(scanner.nextLine());
        var facturaDTO = facturacionService.generarFactura(idPaciente);
        if (facturaDTO != null) {
            System.out.printf("Factura generada: Subtotal=%.2f, Impuesto=%.2f, Total=%.2f\n",
                    facturaDTO.getSubtotal(), facturaDTO.getImpuesto(), facturaDTO.getTotal());
            System.out.println("Detalles:");
            facturaDTO.getDetalles().forEach(d -> System.out.printf("  - %s: $%.2f\n", d.getConcepto(), d.getMonto()));
        } else {
            System.out.println("No hay conceptos pendientes para facturar.");
        }
    }

    private static void procesarEntregaMedicamento() {
        System.out.println("\n--- Entrega de Medicamento (Farmacia) ---");
        System.out.print("ID del paciente: ");
        int idPaciente = Integer.parseInt(scanner.nextLine());
        System.out.print("ID del medicamento: ");
        int idMedicamento = Integer.parseInt(scanner.nextLine());
        System.out.print("Cantidad: ");
        int cantidad = Integer.parseInt(scanner.nextLine());
        System.out.print("Presentó receta (S/N): ");
        boolean presenteReceta = scanner.nextLine().equalsIgnoreCase("S");
        System.out.print("Rol del usuario (ADMIN / FARMACIA): ");
        String rol = scanner.nextLine();

        boolean ok = facade.procesarEntregaMedicamento(idPaciente, idMedicamento, cantidad, presenteReceta, rol);
        if (ok) {
            System.out.println("Entrega registrada y stock actualizado.");
        } else {
            System.out.println("Error: verifique stock o permisos.");
        }
    }

    private static void consultarDashboard() {
        System.out.println("\n--- Dashboard (datos actuales) ---");
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM cita WHERE DATE(fecha_hora) = DATE('now')");
            if (rs.next()) System.out.println("Citas de hoy: " + rs.getInt(1));
            rs = stmt.executeQuery("SELECT COALESCE(SUM(total),0) FROM factura WHERE estado_pago='PAGADO' AND strftime('%Y-%m', fecha_emision) = strftime('%Y-%m', 'now')");
            if (rs.next()) System.out.printf("Ingresos del mes: $%.2f\n", rs.getDouble(1));
            rs = stmt.executeQuery("SELECT nombre FROM medicamento WHERE stock_actual < stock_minimo");
            System.out.println("Medicamentos con stock bajo:");
            while (rs.next()) System.out.println("  - " + rs.getString(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
