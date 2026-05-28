package com.esperanza.hopecare.model;

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
    private double tarifa;

    public RegistroDTO() {}

    // Getters and Setters
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public String getRegistroMedico() { return registroMedico; }
    public void setRegistroMedico(String registroMedico) { this.registroMedico = registroMedico; }

    public double getTarifa() { return tarifa; }
    public void setTarifa(double tarifa) { this.tarifa = tarifa; }
}
