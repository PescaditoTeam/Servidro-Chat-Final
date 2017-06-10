package com.socket;

import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
/*
 * Esta Clase representa una mini base de datos en XML. Se maneja igual que un DOM en HTML
 */
public class InformacionDeUsuarios {
    
    public String rutaArchivo;
    
    public InformacionDeUsuarios(String ruta){
        this.rutaArchivo = ruta;
    }
    
    public boolean existeUsuario(String nombreDeUsuario){
        
        try{
            File archivoXML = new File(rutaArchivo);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(archivoXML);
            doc.getDocumentElement().normalize();
            
            NodeList nList = doc.getElementsByTagName("user");
            
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element elemento = (Element) nNode;
                    if(getTagValue("username", elemento).equals(nombreDeUsuario)){
                        return true;
                    }
                }
            }
            return false;
        }
        catch(Exception ex){
            System.out.println("Error InformacionDeUsuario userExists()");
            return false;
        }
    }
    
    public boolean checkLogin(String _nombreUsuario, String _password){
        
        if(!existeUsuario(_nombreUsuario)){ return false; }
        
        try{
            File fileXML = new File(rutaArchivo);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fileXML);
            doc.getDocumentElement().normalize();
            
            NodeList nList = doc.getElementsByTagName("user");
            
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element elemento = (Element) nNode;
                    if(getTagValue("username", elemento).equals(_nombreUsuario) && getTagValue("password", elemento).equals(_password)){
                        return true;
                    }
                }
            }
           
            return false;
        }
        catch(Exception ex){
            System.out.println("Error InformacionDeUsuarios usuarioYaExiste()");
            return false;
        }
    }
    
    public void addUser(String _nombreDeUsuario, String _password){
        
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(rutaArchivo);
 
            Node data = doc.getFirstChild();
            
            Element nuevoUsuario = doc.createElement("user");
            Element nuevoNombreDeUsuario = doc.createElement("username");
            nuevoNombreDeUsuario.setTextContent(_nombreDeUsuario);
            Element nuevoPassword = doc.createElement("password");
            nuevoPassword.setTextContent(_password);
            
            nuevoUsuario.appendChild(nuevoNombreDeUsuario);
            nuevoUsuario.appendChild(nuevoPassword);
            data.appendChild(nuevoUsuario);
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(rutaArchivo));
            transformer.transform(source, result);
 
	   } 
           catch(Exception ex){
		System.out.println("Error al modificiar xml");
	   }
	}
    
    public static String getTagValue(String tag, Element elemento) {
	NodeList nlList = elemento.getElementsByTagName(tag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
	return nValue.getNodeValue();
  }
}
