/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Networking;
import java.net.InetAddress;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
/**
 *
 * @author Administrator
 */
public class Client_Transfer {
    private Socket client;
    private InetAddress IP;
    private String name;

    public Client_Transfer(Socket client, InetAddress IP, String name, ObjectInputStream ois, ObjectOutputStream oos, String ID) {
        this.client = client;
        this.IP = IP;
        this.name = name;
        this.ois = ois;
        this.oos = oos;
        this.ID = ID;
    }
    private ObjectInputStream ois;

    public ObjectInputStream getOis() {
        return ois;
    }

    public void setOis(ObjectInputStream ois) {
        this.ois = ois;
    }

    public ObjectOutputStream getOos() {
        return oos;
    }

    public void setOos(ObjectOutputStream oos) {
        this.oos = oos;
    }
    private ObjectOutputStream oos;
    
    
    public Socket getClient() {
        return client;
    }

    public void setClient(Socket client) {
        this.client = client;
    }

    public InetAddress getIP() {
        return IP;
    }

    public void setIP(InetAddress IP) {
        this.IP = IP;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
    private String ID;

    public Client_Transfer() {
    }
}
