package com.socket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class ServerThread extends Thread { 
	
    public SocketServer server = null;
    public Socket socket = null;
    public int ID = -1;
    public String nombreUsuario = "";
    public ObjectInputStream entradaObjeto  =  null;
    public ObjectOutputStream salidaObjeto = null;
    public ServerFrame ui;

    public ServerThread(SocketServer _server, Socket _socket){  
    	super();
        server = _server;
        socket = _socket;
        ID     = socket.getPort();
        ui = _server.ui;
    }
    
    public void enviarMensaje(Mensaje msg){
        try {
            salidaObjeto.writeObject(msg);
            salidaObjeto.flush();
        } 
        catch (IOException ex) {
            System.out.println("Error SocketServer enviarMensaje()");
        }
    }
    
    public int getID(){  
	    return ID;
    }
   
    @SuppressWarnings("deprecation")
	public void run(){  
    	//ui.jTextArea1.append("\nServer Thread " + ID + " running.");
        while (true){  
    	    try{  
    	    	//Aca se bloquea esperando el objeto. por eso el hilo
                Mensaje mensaje = (Mensaje) entradaObjeto.readObject();
    	    	server.handle(ID, mensaje);
            }
            catch(Exception e){  
            	System.out.println(" ERROR de lectura:"+ e.getMessage());
                server.eliminarUsuario(ID);
                stop();
            }
        }
    }
    
    public void inicializarObjetosLecturaEscritura() throws IOException {  
        salidaObjeto = new ObjectOutputStream(socket.getOutputStream());
        salidaObjeto.flush();
        entradaObjeto = new ObjectInputStream(socket.getInputStream());
    }
    
    public void close() throws IOException {  
    	if (socket != null)    socket.close();
        if (entradaObjeto != null)  entradaObjeto.close();
        if (salidaObjeto != null) salidaObjeto.close();
    }
}

