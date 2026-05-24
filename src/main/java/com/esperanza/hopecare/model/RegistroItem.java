package com.esperanza.hopecare.model;

public class RegistroItem {
    private String displayText;
    private String tipo;
    private int id;

    public RegistroItem(String displayText, String tipo, int id) {
        this.displayText = displayText;
        this.tipo = tipo;
        this.id = id;
    }

    public String getDisplayText() { return displayText; }
    public String getTipo() { return tipo; }
    public int getId() { return id; }
}
