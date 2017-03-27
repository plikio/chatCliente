/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redesii.service;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import redesii.bean.MensagemBean;

/**
 *
 * @author Plinio
 */
public class ClienteSocket {
    private Socket socket;
    private ObjectOutputStream saida;
    
    public Socket conectar(String ip, int porta){
        try {
            socket = new Socket(ip,porta);
            saida = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(ClienteSocket.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException e){
            
        } 
        return socket;
    } 
    
    public void enviar(MensagemBean msgBean){
        try {
            saida.writeObject(msgBean);
        } catch (IOException ex) {
            Logger.getLogger(ClienteSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
