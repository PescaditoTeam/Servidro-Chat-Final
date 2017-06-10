package com.socket;

import java.io.Serializable;

public class Mensaje implements Serializable{
    
    private static final long serialVersionUID = 1L;
    public String tipo, remitente, contenido, destinatario;
    
    
    public Mensaje(String _tipo, String _remitente, String _contenido, String _destinatario){
        this.tipo = _tipo; this.remitente = _remitente; this.contenido = _contenido; this.destinatario = _destinatario;
    }
    
    
    @Override
    public String toString(){
        return "{Tipo='"+tipo+"', Remitente='"+remitente+"', Contenido='"+contenido+"', Destinatario='"+destinatario+"'}";
    }
}
