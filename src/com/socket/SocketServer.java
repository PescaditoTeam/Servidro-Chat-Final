package com.socket;

import java.io.*;
import java.net.*;
/*
 * Esta clase se encarga de escuchar los comandos del pipe.
 */
public class SocketServer implements Runnable {
    
    public ServerThread clientes[];
    public ServerSocket server = null;
    public Thread       thread = null;
    public int cantClientes = 0;
    //esto deberia leerlo del archivo de properties.
    public int puerto = 1500;
    public ServerFrame ui;
    public InformacionDeUsuarios db;

    public SocketServer(ServerFrame frame){
       
        clientes = new ServerThread[50];
        ui = frame;
        db = new InformacionDeUsuarios(ui.filePath);
        
	try{  
	    server = new ServerSocket(puerto);
            puerto = server.getLocalPort();
	    ui.jTextArea1.append("Servidor iniciado..");
	    start(); 
        }
	catch(IOException ioe){  
            ui.jTextArea1.append("Imposible usar el puerto, reintentando"); 
            ui.RetryStart(0);
	}
    }
    
    public SocketServer(ServerFrame frame, int _puerto){
       //Parametrizar la cantidad maxima de clientes
        clientes = new ServerThread[20];
        ui = frame;
        puerto = _puerto;
        db = new InformacionDeUsuarios(ui.filePath);
        
	try{  
	    server = new ServerSocket(puerto);
            puerto = server.getLocalPort();
	    ui.jTextArea1.append("Servidor iniciado..");
	    start(); 
        }
	catch(IOException ioe){  
            ui.jTextArea1.append("\nNo se puede utilizar el puerto seleccionado"); 
	}
    }
	
    public void run(){  
	while (thread != null){  
            try{  
		ui.jTextArea1.append("\nServidor a la espera de clientes.."); 
	        agregarHilo(server.accept()); 
	    }
	    catch(Exception ioe){ 
                ui.jTextArea1.append("\nError en Accept() \n");
                ui.RetryStart(0);
	    }
        }
    }
	
    public void start(){  
    	if (thread == null){  
            thread = new Thread(this); 
	    thread.start();
	}
    }
    
    @SuppressWarnings("deprecation")
    public void stop(){  
        if (thread != null){  
            thread.stop(); 
	    thread = null;
	}
    }
    
    private int buscarCliente(int ID){  
    	for (int i = 0; i < cantClientes; i++){
        	if (clientes[i].getID() == ID){
                    return i;
                }
	}
	return -1;
    }
	/////////////-------------------------------------------------------
    public synchronized void handle(int ID, Mensaje mensaje){  
	if (mensaje.contenido.equals(".bye")){
            enviarATodos("SALIR", "SERVER", mensaje.remitente);
            eliminarUsuario(ID); 
	}
	else{
            if(mensaje.tipo.equals("LOGIN")){
                if(buscarThreadDeUsuario(mensaje.remitente) == null){
                    if(db.checkLogin(mensaje.remitente, mensaje.contenido)){
                        clientes[buscarCliente(ID)].nombreUsuario = mensaje.remitente;
                        clientes[buscarCliente(ID)].enviarMensaje(new Mensaje("LOGIN", "SERVER", "TRUE", mensaje.remitente));
                        enviarATodos("NUEVO_USUARIO", "SERVER", mensaje.remitente);
                        enviarListaDeUsuarios(mensaje.remitente);
                    }
                    else{
                        clientes[buscarCliente(ID)].enviarMensaje(new Mensaje("LOGIN", "SERVER", "FALSE", mensaje.remitente));
                    } 
                }
                else{
                    clientes[buscarCliente(ID)].enviarMensaje(new Mensaje("LOGIN", "SERVER", "FALSE", mensaje.remitente));
                }
            }
            else if(mensaje.tipo.equals("MENSAJE")){
                if(mensaje.destinatario.equals("A TODOS")){
                    enviarATodos("MENSAJE", mensaje.remitente, mensaje.contenido);
                }
                else{
                    buscarThreadDeUsuario(mensaje.destinatario).enviarMensaje(new Mensaje(mensaje.tipo, mensaje.remitente, mensaje.contenido, mensaje.destinatario));
                    clientes[buscarCliente(ID)].enviarMensaje(new Mensaje(mensaje.tipo, mensaje.remitente, mensaje.contenido, mensaje.destinatario));
                }
            }
            else if(mensaje.tipo.equals("TEST")){
                clientes[buscarCliente(ID)].enviarMensaje(new Mensaje("TEST", "SERVER", "OK", mensaje.remitente));
            }
            else if(mensaje.tipo.equals("REGISTRARSE")){
                if(buscarThreadDeUsuario(mensaje.remitente) == null){
                    if(!db.existeUsuario(mensaje.remitente)){
                        db.addUser(mensaje.remitente, mensaje.contenido);
                        clientes[buscarCliente(ID)].nombreUsuario = mensaje.remitente;
                        clientes[buscarCliente(ID)].enviarMensaje(new Mensaje("REGISTRARSE", "SERVER", "TRUE", mensaje.remitente));
                        clientes[buscarCliente(ID)].enviarMensaje(new Mensaje("LOGIN", "SERVER", "TRUE", mensaje.remitente));
                        enviarATodos("NUEVO_USUARIO", "SERVER", mensaje.remitente);
                        enviarListaDeUsuarios(mensaje.remitente);
                    }
                    else{
                        clientes[buscarCliente(ID)].enviarMensaje(new Mensaje("REGISTRARSE", "SERVER", "FALSE", mensaje.remitente));
                    }
                }
                else{
                    clientes[buscarCliente(ID)].enviarMensaje(new Mensaje("REGISTRARSE", "SERVER", "FALSE", mensaje.remitente));
                }
            }
            
            /*
            else if(mensaje.tipo.equals("upload_req")){
                if(mensaje.destinatario.equals("All")){
                    clientes[buscarCliente(ID)].enviarMensaje(new Mensaje("message", "SERVER", "Uploading to 'All' forbidden", mensaje.remitente));
                }
                else{
                    buscarThreadDeUsuario(mensaje.destinatario).enviarMensaje(new Mensaje("upload_req", mensaje.remitente, mensaje.contenido, mensaje.destinatario));
                }
            }
            else if(mensaje.tipo.equals("upload_res")){
                if(!mensaje.contenido.equals("NO")){
                    String IP = buscarThreadDeUsuario(mensaje.remitente).socket.getInetAddress().getHostAddress();
                    buscarThreadDeUsuario(mensaje.destinatario).enviarMensaje(new Mensaje("upload_res", IP, mensaje.contenido, mensaje.destinatario));
                }
                else{
                    buscarThreadDeUsuario(mensaje.destinatario).enviarMensaje(new Mensaje("upload_res", mensaje.remitente, mensaje.contenido, mensaje.destinatario));
                }
            }*/
	}
    }
    
    public void enviarATodos(String tipo, String emisor, String contenido){
        Mensaje mensaje = new Mensaje(tipo, emisor, contenido, "A TODOS");
        for(int i = 0; i < cantClientes; i++){
            clientes[i].enviarMensaje(mensaje);
        }
    }
    
    public void enviarListaDeUsuarios(String aQuien){
        for(int i = 0; i < cantClientes; i++){
            buscarThreadDeUsuario(aQuien).enviarMensaje(new Mensaje("NUEVO_USUARIO", "SERVER", clientes[i].nombreUsuario, aQuien));
        }
    }
    
    public ServerThread buscarThreadDeUsuario(String _usuario){
        for(int i = 0; i < cantClientes; i++){
            if(clientes[i].nombreUsuario.equals(_usuario)){
                return clientes[i];
            }
        }
        return null;
    }
	
    @SuppressWarnings("deprecation")
    public synchronized void eliminarUsuario(int ID){  
    int pos = buscarCliente(ID);
        if (pos >= 0){  
            ServerThread hiloAEliminar = clientes[pos];
            ui.jTextArea1.append("\nEliminando hilo de usuario");
	    if (pos < cantClientes-1){
                for (int i = pos+1; i < cantClientes; i++){
                    clientes[i-1] = clientes[i];
	        }
	    }
	    cantClientes--;
	    try{  
	      	hiloAEliminar.close(); 
	    }
	    catch(IOException ioe){  
	      	ui.jTextArea1.append("\nError closing thread: " + ioe); 
	    }
	    hiloAEliminar.stop(); 
	}
    }
    
    private void agregarHilo(Socket socket){  
	if (cantClientes < clientes.length){  
            ui.jTextArea1.append("\nCliente Aceptado:");
	    clientes[cantClientes] = new ServerThread(this, socket);
	    try{  
	      	clientes[cantClientes].inicializarObjetosLecturaEscritura(); 
	        clientes[cantClientes].start();  
	        cantClientes++; 
	    }
	    catch(IOException e){  
	      	ui.jTextArea1.append("\nError Abriendo Thread: " + e); 
	    } 
	}
	else{
            ui.jTextArea1.append("\nMaximo de clientes alcanzado");
	}
    }
}
