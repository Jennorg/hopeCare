package com.esperanza.hopecare.modules.dashboard.model;

import java.util.List;
import java.util.Map;

public class DashboardData {

    private int citasHoy;
    private int pacientesAtendidosHoy;
    private double ingresosMes;
    private double porcentajeAsistencia;
    private Map<String, Integer> estadoCitas;
    private List<String> medicamentosStockBajo;
    private int solicitudesLabPendientes;
    private int facturasPendientes;
    private double ingresosConsultas;
    private double ingresosFarmacia;
    private double ingresosLaboratorio;
    private List<String> registrosRecientes;
    private int totalPacientes;
    private int totalMedicos;
    private int totalMedicamentos;
    private int totalExamenes;

    public DashboardData() {}

    public int getCitasHoy() { return citasHoy; }
    public void setCitasHoy(int citasHoy) { this.citasHoy = citasHoy; }

    public int getPacientesAtendidosHoy() { return pacientesAtendidosHoy; }
    public void setPacientesAtendidosHoy(int pacientesAtendidosHoy) { this.pacientesAtendidosHoy = pacientesAtendidosHoy; }

    public double getIngresosMes() { return ingresosMes; }
    public void setIngresosMes(double ingresosMes) { this.ingresosMes = ingresosMes; }

    public double getPorcentajeAsistencia() { return porcentajeAsistencia; }
    public void setPorcentajeAsistencia(double porcentajeAsistencia) { this.porcentajeAsistencia = porcentajeAsistencia; }

    public Map<String, Integer> getEstadoCitas() { return estadoCitas; }
    public void setEstadoCitas(Map<String, Integer> estadoCitas) { this.estadoCitas = estadoCitas; }

    public List<String> getMedicamentosStockBajo() { return medicamentosStockBajo; }
    public void setMedicamentosStockBajo(List<String> medicamentosStockBajo) { this.medicamentosStockBajo = medicamentosStockBajo; }

    public int getSolicitudesLabPendientes() { return solicitudesLabPendientes; }
    public void setSolicitudesLabPendientes(int solicitudesLabPendientes) { this.solicitudesLabPendientes = solicitudesLabPendientes; }

    public int getFacturasPendientes() { return facturasPendientes; }
    public void setFacturasPendientes(int facturasPendientes) { this.facturasPendientes = facturasPendientes; }

    public double getIngresosConsultas() { return ingresosConsultas; }
    public void setIngresosConsultas(double ingresosConsultas) { this.ingresosConsultas = ingresosConsultas; }

    public double getIngresosFarmacia() { return ingresosFarmacia; }
    public void setIngresosFarmacia(double ingresosFarmacia) { this.ingresosFarmacia = ingresosFarmacia; }

    public double getIngresosLaboratorio() { return ingresosLaboratorio; }
    public void setIngresosLaboratorio(double ingresosLaboratorio) { this.ingresosLaboratorio = ingresosLaboratorio; }

    public List<String> getRegistrosRecientes() { return registrosRecientes; }
    public void setRegistrosRecientes(List<String> registrosRecientes) { this.registrosRecientes = registrosRecientes; }

    public int getTotalPacientes() { return totalPacientes; }
    public void setTotalPacientes(int totalPacientes) { this.totalPacientes = totalPacientes; }

    public int getTotalMedicos() { return totalMedicos; }
    public void setTotalMedicos(int totalMedicos) { this.totalMedicos = totalMedicos; }

    public int getTotalMedicamentos() { return totalMedicamentos; }
    public void setTotalMedicamentos(int totalMedicamentos) { this.totalMedicamentos = totalMedicamentos; }

    public int getTotalExamenes() { return totalExamenes; }
    public void setTotalExamenes(int totalExamenes) { this.totalExamenes = totalExamenes; }
}
