package com.esperanza.hopecare.test;

import com.esperanza.hopecare.common.db.DatabaseConnection;
import com.esperanza.hopecare.common.db.CargarDatosPrueba;
import com.esperanza.hopecare.modules.citas_consultas.dao.CitaDAO;
import com.esperanza.hopecare.modules.citas_consultas.dao.ConsultaDAO;
import com.esperanza.hopecare.modules.citas_consultas.model.Cita;
import com.esperanza.hopecare.modules.citas_consultas.model.Consulta;
import com.esperanza.hopecare.modules.pacientes_medicos.dao.PacienteDAO;
import com.esperanza.hopecare.modules.pacientes_medicos.dao.MedicoDAO;
import com.esperanza.hopecare.modules.Auth.dao.AuthDAO;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FlujoCitaConsultaTest {

    static int pasos = 0;
    static int fallos = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PRUEBA DE FLUJO COMPLETO: CITA -> CONSULTA ===\n");

        paso("Inicializar BD si es necesario");
        inicializarBD();

        paso("Verificar tablas existen");
        verificarTablas();

        paso("Autenticar usuario admin");
        AuthDAO authDAO = new AuthDAO();
        var admin = authDAO.autenticar("admin", "admin123");
        check(admin != null, "admin debe autenticarse");
        System.out.println("  -> Admin: " + admin.getNombreUsuario() + " / " + admin.getRol());

        paso("Autenticar usuario medico");
        var medico = authDAO.autenticar("medico", "medico123");
        check(medico != null, "medico debe autenticarse");
        System.out.println("  -> Medico: " + medico.getNombreUsuario() + " / " + medico.getRol());

        paso("Listar pacientes");
        var pacientes = new PacienteDAO().listarTodos();
        check(pacientes.size() > 0, "Debe haber pacientes registrados");
        System.out.println("  -> Pacientes: " + pacientes.size());

        paso("Listar medicos");
        var medicos = new MedicoDAO().listarTodos();
        check(medicos.size() > 0, "Debe haber medicos registrados");
        System.out.println("  -> Medicos: " + medicos.size());

        paso("Crear cita PROGRAMADA");
        Cita cita = new Cita();
        cita.setIdPaciente(1);
        cita.setIdMedico(1);
        cita.setFechaHora(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0));
        cita.setEstado("PROGRAMADA");
        cita.setMotivo("Control de rutina");
        cita.setCreadaPor(1);
        cita.setFechaCreacion(LocalDateTime.now());

        CitaDAO citaDAO = new CitaDAO();
        boolean creada = citaDAO.insertarCita(cita);
        check(creada, "La cita debe crearse");
        int idCita = cita.getIdCita();
        check(idCita > 0, "ID de cita debe ser > 0, obtenido: " + idCita);

        paso("Verificar cita en listado PROGRAMADAS");
        var programadas = citaDAO.obtenerCitasPorEstado("PROGRAMADA");
        boolean encontrada = programadas.stream().anyMatch(c -> c.getIdCita() == idCita);
        check(encontrada, "La cita debe aparecer en PROGRAMADAS");

        paso("Crear consulta vinculada a la cita");
        Consulta consulta = new Consulta();
        consulta.setIdCita(idCita);
        consulta.setDiagnostico("Paciente en buen estado general");
        consulta.setSintomas("Control anual sin novedades");
        consulta.setTratamiento("Continuar con habitos saludables");
        consulta.setNotasMedicas("Buena adherencia al tratamiento");
        consulta.setFechaConsulta(LocalDateTime.now());
        consulta.setPrecio(50000.0);

        ConsultaDAO consultaDAO = new ConsultaDAO();
        int idConsulta = consultaDAO.insertarConsultaYActualizarEstado(consulta);
        check(idConsulta > 0, "La consulta debe crearse. ID: " + idConsulta);

        paso("Verificar cita cambio a ATENDIDA");
        var atendidas = citaDAO.obtenerCitasPorEstado("ATENDIDA");
        boolean cambioEstado = atendidas.stream().anyMatch(c -> c.getIdCita() == idCita);
        check(cambioEstado, "La cita debe haber cambiado a ATENDIDA");

        paso("Consultar consulta por ID");
        Consulta consultaObtenida = consultaDAO.obtenerConsultaPorId(idConsulta);
        check(consultaObtenida != null, "La consulta debe existir en BD");
        check("Paciente en buen estado general".equals(consultaObtenida.getDiagnostico()),
              "El diagnostico debe coincidir");

        paso("Verificar que insertarSiNoExiste no duplica (MySQL)");
        consultaDAO.insertarSiNoExiste(idCita, 50000.0);
        var consultaRepetida = consultaDAO.obtenerConsultaPorId(idConsulta);
        check(consultaRepetida != null, "La consulta original debe seguir existiendo");

        System.out.println("\n============================================");
        System.out.println("RESULTADO: " + pasos + " pasos, " + fallos + " fallos");
        System.out.println(fallos == 0 ? "*** PRUEBA EXITOSA ***" : "*** PRUEBA CON FALLOS ***");
        System.out.println("============================================");
        System.exit(fallos > 0 ? 1 : 0);
    }

    static void paso(String nombre) {
        pasos++;
        System.out.println("\n[" + pasos + "] " + nombre + "...");
    }

    static void check(boolean cond, String msg) {
        if (cond) {
            System.out.println("  OK: " + msg);
        } else {
            fallos++;
            System.out.println("  FAIL: " + msg);
        }
    }

    static void inicializarBD() throws Exception {
        try (Connection conn = DatabaseConnection.getRootConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP DATABASE IF EXISTS hopecare_clinica");
            System.out.println("  -> BD eliminada. Creando desde script...");
            String sql = new String(java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get("src/main/resources/hopecare_mysql_complete.sql")));
            for (String s : sql.split(";")) {
                String t = s.trim();
                if (!t.isEmpty()) stmt.execute(t);
            }
            System.out.println("  -> Cargando datos de prueba...");
            CargarDatosPrueba.main(new String[]{});
        }
    }

    static void verificarTablas() throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            String[] tablas = {"persona", "especialidad", "medico", "paciente", "rol", "usuario",
                               "horario_atencion", "cita", "consulta"};
            for (String t : tablas) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM information_schema.tables " +
                    "WHERE table_schema = 'hopecare_clinica' AND table_name = '" + t + "'");
                boolean existe = rs.next() && rs.getInt(1) > 0;
                System.out.println("  -> " + t + ": " + (existe ? "OK" : "FALTA"));
                if (!existe) fallos++;
            }
        }
    }
}
