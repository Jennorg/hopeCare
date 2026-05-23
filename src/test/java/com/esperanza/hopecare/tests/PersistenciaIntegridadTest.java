package com.esperanza.hopecare.tests;

import com.esperanza.hopecare.util.DatabaseConnection;
import com.esperanza.hopecare.dao.PacienteDAO;
import com.esperanza.hopecare.model.Paciente;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PersistenciaIntegridadTest {

    private static PacienteDAO pacienteDAO;
    private static int idPacientePrueba;

    @BeforeAll
    public static void setUp() throws SQLException {
        pacienteDAO = new PacienteDAO();
        // Limpiar base de datos para pruebas
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM paciente");
            stmt.execute("DELETE FROM persona");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='paciente' OR name='persona'");
        }
    }

    @Test
    @Order(1)
    public void testCicloCRUD() {
        // 1. Inserción
        Paciente p = new Paciente();
        p.setNombre("Prueba");
        p.setApellido("JUnit");
        p.setDocumentoIdentidad("TEST-999");
        p.setHistoriaClinica("HC-TEST");
        p.setActivo(1);

        assertTrue(pacienteDAO.insertar(p), "Inserción debería ser exitosa");
        idPacientePrueba = p.getIdPaciente();
        assertTrue(idPacientePrueba > 0, "Debería generar un ID");

        // 2. Lectura
        List<Paciente> lista = pacienteDAO.listarTodos();
        boolean encontrado = lista.stream().anyMatch(pac -> pac.getDocumentoIdentidad().equals("TEST-999"));
        assertTrue(encontrado, "El paciente insertado debería ser recuperado de la BD");

        // 3. Actualización
        p.setNombre("Prueba Actualizada");
        assertTrue(pacienteDAO.actualizar(p), "Actualización debería ser exitosa");
        
        List<Paciente> listaAct = pacienteDAO.listarTodos();
        String nombreAct = listaAct.stream()
                .filter(pac -> pac.getIdPaciente() == idPacientePrueba)
                .findFirst()
                .get()
                .getNombre();
        assertEquals("Prueba Actualizada", nombreAct, "El nombre debería haberse actualizado en la BD");

        // 4. Borrado Lógico (Dar de alta)
        assertTrue(pacienteDAO.darDeAlta(idPacientePrueba), "El borrado lógico (alta) debería ser exitoso");
        List<Paciente> activos = pacienteDAO.listarActivos();
        boolean sigueActivo = activos.stream().anyMatch(pac -> pac.getIdPaciente() == idPacientePrueba);
        assertFalse(sigueActivo, "El paciente no debería figurar en la lista de activos tras el borrado lógico");
    }

    @Test
    @Order(2)
    public void testRestriccionesUnicidad() {
        // Intentar registrar un paciente con el mismo documento
        Paciente pDuplicado = new Paciente();
        pDuplicado.setNombre("Duplicado");
        pDuplicado.setApellido("Test");
        pDuplicado.setDocumentoIdentidad("TEST-999"); // Ya existe
        pDuplicado.setHistoriaClinica("HC-NEW");

        // El DAO captura excepciones y devuelve false
        boolean ok = pacienteDAO.insertar(pDuplicado);
        assertFalse(ok, "El sistema no debería permitir duplicar el documento de identidad");
        
        // Intentar registrar un paciente con la misma historia clínica
        Paciente pHCDuplicada = new Paciente();
        pHCDuplicada.setNombre("Duplicado HC");
        pHCDuplicada.setApellido("Test");
        pHCDuplicada.setDocumentoIdentidad("TEST-000");
        pHCDuplicada.setHistoriaClinica("HC-TEST"); // Ya existe

        boolean okHC = pacienteDAO.insertar(pHCDuplicada);
        assertFalse(okHC, "El sistema no debería permitir duplicar la historia clínica");
    }
}
