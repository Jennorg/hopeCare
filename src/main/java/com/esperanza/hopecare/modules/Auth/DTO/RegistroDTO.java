package com.esperanza.hopecare.modules.Auth.dto;

import java.time.LocalDate;

public class RegistroDTO {
    private String nombreUsuario;
    private String contrasena;
    private String rol;
    private String nombre;
    private String apellido;
    private String documento;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String genero;
    private String email;
    private String direccion;
    private String especialidad;
    private String registroMedico;

    public RegistroDTO() {}

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String v) { this.nombreUsuario = v; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String v) { this.contrasena = v; }
    public String getRol() { return rol; }
    public void setRol(String v) { this.rol = v; }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }
    public String getApellido() { return apellido; }
    public void setApellido(String v) { this.apellido = v; }
    public String getDocumento() { return documento; }
    public void setDocumento(String v) { this.documento = v; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate v) { this.fechaNacimiento = v; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String v) { this.telefono = v; }
    public String getGenero() { return genero; }
    public void setGenero(String v) { this.genero = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String v) { this.direccion = v; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String v) { this.especialidad = v; }
    public String getRegistroMedico() { return registroMedico; }
    public void setRegistroMedico(String v) { this.registroMedico = v; }
}