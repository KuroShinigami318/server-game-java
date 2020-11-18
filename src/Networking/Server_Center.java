/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Networking;

import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
/**
 *
 * @author Administrator
 */
public class Server_Center extends JFrame{
    private JLabel connections;
    private boolean flag = false;
    private boolean keepAlive = true;
    private ServerSocket server = null;
    private static int port = 9099;
    private ArrayList<String> clientIDs = new ArrayList();
    private HashMap<String,Client_Transfer> clients = new HashMap<>();
    int __max = 52428800;
    
    public Server_Center(){
        try {
            init();
            server = new ServerSocket(port);            
            while(true){                
                Socket client = server.accept();
                connections.setText("Clients: "+(clientIDs.size()+1));
                ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                new ClientHandler(client, clientIDs, clients, ois, oos).start();                               
            }
        }
        catch (IOException ex) {
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
            ex.printStackTrace();
        }
    }
    
    private void init(){
        setSize(400, 100);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        connections = new JLabel("",JLabel.CENTER);
        add(connections);
        setVisible(true);
    }
    
    private class ClientHandler extends Thread{
        private Socket client;
        private ArrayList<String> clientIDs;
        private String ClientID;
        private HashMap<String,Client_Transfer> clients;
        private ObjectInputStream ois;
        private ObjectOutputStream oos;        

        public Socket getClient() {
            return client;
        }

        public void setClient(Socket client) {
            this.client = client;
        }

        public ArrayList<String> getClientIDs() {
            return clientIDs;
        }

        public void setClientIDs(ArrayList<String> clientIDs) {
            this.clientIDs = clientIDs;
        }

        public HashMap<String, Client_Transfer> getClients() {
            return clients;
        }

        public void setClients(HashMap<String, Client_Transfer> clients) {
            this.clients = clients;
        }

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

        public ClientHandler(Socket client, ArrayList<String> clientIDs, HashMap<String, Client_Transfer> clients) {
            this.client = client;
            this.clientIDs = clientIDs;
            this.clients = clients;
        }

        public ClientHandler(Socket client, ArrayList<String> clientIDs, HashMap<String, Client_Transfer> clients, ObjectInputStream ois, ObjectOutputStream oos) {
            this.client = client;
            this.clientIDs = clientIDs;
            this.clients = clients;
            this.ois = ois;
            this.oos = oos;
        }
        
        private ClientHandler(){
            
        }
        
        @Override
        public void run(){
            try {
            while(true){                
                communication_transfer pack_mess = (communication_transfer) ois.readObject();                                
                if(pack_mess.getTo_ID().equalsIgnoreCase("server")&&pack_mess.getMessage().equalsIgnoreCase("addToServer")){
                    if(clientIDs.contains(pack_mess.getFrom_ID())) {
                        requestChangeID(oos);
                    } else {
                        ClientID = pack_mess.getFrom_ID();
                        clientIDs.add(pack_mess.getFrom_ID());
                        clients.put(pack_mess.getFrom_ID(), new Client_Transfer(client, client.getInetAddress(), pack_mess.getFrom_Name(), ois, oos,pack_mess.getFrom_ID()));
                        if(clientIDs.indexOf(pack_mess.getFrom_ID())%2==0) {
                            oos.writeObject(new communication_transfer("server", pack_mess.getFrom_ID(), "waiting for opponent"));
                        } else {
                            oos.writeObject(new communication_transfer(clients.get(clientIDs.get(clientIDs.indexOf(pack_mess.getFrom_ID())-1)).getName(),clientIDs.get(clientIDs.indexOf(pack_mess.getFrom_ID())-1), pack_mess.getFrom_ID(), "connected", "connected"));
                            oos = clients.get(clientIDs.get(clientIDs.indexOf(pack_mess.getFrom_ID())-1)).getOos();                            
                            oos.writeObject(new communication_transfer(clients.get(pack_mess.getFrom_ID()).getName(),pack_mess.getFrom_ID(), clientIDs.get(clientIDs.indexOf(pack_mess.getFrom_ID())-1), "connected","connected"));
                            oos = clients.get(pack_mess.getFrom_ID()).getOos();
                        }
                    }
                } else if(!pack_mess.getTo_ID().equalsIgnoreCase("server")) {
                    if(pack_mess.getPurpose().equalsIgnoreCase("file")) {
                        if(pack_mess.getObject() instanceof byte[]) {
                            byte[] file = (byte[]) pack_mess.getObject();
                            BufferedOutputStream bos = null;
                            try {
                                bos = new BufferedOutputStream(new FileOutputStream("C:/Users/Administrator/Documents/SERVER_FILE_UPLOAD/"+pack_mess.getMessage()), 65536);
                                bos.write(file, 0, file.length);
                                oos.writeObject(new communication_transfer("server", pack_mess.getObject(), "server", pack_mess.getFrom_ID(), pack_mess.getMessage(), "upload successful"));
                                System.out.println("upload successful "+file.length);
                            } catch(IOException e) {
                                e.printStackTrace();
                            }
                            finally {
                                if(bos!=null) bos.close();
                            }
                        } if(pack_mess.getObject() instanceof ArrayList) {
                            ArrayList<byte[]> partFiles = (ArrayList) pack_mess.getObject();
                            BufferedOutputStream bos = null;
                            try {
                                bos = new BufferedOutputStream(new FileOutputStream("C:/Users/Administrator/Documents/SERVER_FILE_UPLOAD/"+pack_mess.getMessage()), 65536);
                                int i = 0;
                                int curr = 0;
                                do {
                                    byte[] partFile = partFiles.get(i);
                                    bos.write(partFile, 0, partFile.length);
                                    curr += __max;
                                    i++;
                                } while(i<partFiles.size());
                                oos.writeObject(new communication_transfer("server", null, "server", pack_mess.getFrom_ID(), pack_mess.getMessage(), "upload successful"));
                                System.out.println("upload successful ");
                            } catch(IOException e) {
                                e.printStackTrace();
                            }
                            finally {
                                if(bos!=null) bos.close();
                            }
                        }
                    }
                    clients.get(pack_mess.getTo_ID()).getOos().writeObject(pack_mess);                   
                } else if(pack_mess.getTo_ID().equalsIgnoreCase("server")&&pack_mess.getMessage().equalsIgnoreCase("get number")&&pack_mess.getPurpose().equalsIgnoreCase("request number")){
                    oos.writeObject(new communication_transfer("server", "server", pack_mess.getFrom_ID(), Integer.toString(clientIDs.indexOf(pack_mess.getFrom_ID())), "your number is"));
                } else if(pack_mess.getTo_ID().equalsIgnoreCase("server")&&pack_mess.getPurpose().equalsIgnoreCase("request download")){
                    String url = "C:/Users/Administrator/Documents/SERVER_FILE_UPLOAD/"+pack_mess.getMessage();
                    File file = new File(url);
                    byte[] byteFile = new byte[(int) file.length()];
                    BufferedInputStream bis = null;                    
                        bis = new BufferedInputStream(new FileInputStream(url), 65536);
                        bis.read(byteFile, 0, byteFile.length);
                        oos.writeObject(new communication_transfer("server", byteFile, "server", pack_mess.getFrom_ID(), pack_mess.getMessage(), "download file"));
                        System.out.println("transfer successful ");
                    if(bis!=null) bis.close();
                }
                else if(pack_mess.getTo_ID().equalsIgnoreCase("server")&&pack_mess.getMessage().equalsIgnoreCase("close")&&pack_mess.getPurpose().equalsIgnoreCase("closeClient")) {
                    close(oos, ois, client);
                    RemoveClosedClient(pack_mess.getFrom_ID());
                    connectClient();
                    autoCloseServer();                    
                    break;
                }
                
                Thread.sleep(200);
                }
            } catch (IOException ex) {
                close(oos, ois, client);
                RemoveClosedClient(ClientID);
                connectClient();
                autoCloseServer();                
                //ex.printStackTrace();
            }  catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            } catch (InterruptedException ex) {
                close(oos, ois, client);
                RemoveClosedClient(ClientID);
                connectClient();
                autoCloseServer();
                Logger.getLogger(Server_Center.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }    
    
    private void RemoveClosedClient(String key){
        try {
            connections.setText("Clients: "+(clientIDs.size()-1));
            clients.get(key).getClient().close();
            clientIDs.remove(key);
            clients.remove(key);
            flag = true;           
        } catch (IOException ex) {
            Logger.getLogger(Server_Center.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void connectClient(){
        if(flag){
            for(String key : clients.keySet()){           
                int index = clientIDs.indexOf(key);
                if(index%2==0){
                    if(clientIDs.size()>index+1) {
                        try {
                            clients.get(key).getOos().writeObject(new communication_transfer(clients.get(clientIDs.get(index+1)).getName(),clientIDs.get(index+1), key, "connected","connected"));
                            clients.get(clientIDs.get(index+1)).getOos().writeObject(new communication_transfer(clients.get(key).getName(), key, clientIDs.get(index+1), "connected","connected"));                            
                        } catch (IOException ex) {
                            connections.setText("Clients: "+(clientIDs.size())+"\tERROR1: "+ex.getMessage());
                            Logger.getLogger(Server_Center.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        try {
                            clients.get(key).getOos().writeObject(new communication_transfer("server", key, "waiting for opponent"));
                        } catch(IOException e){
                            connections.setText("Clients: "+(clientIDs.size())+"\tERROR2: "+e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
            flag = false;
        }
    }
    
    private void autoCloseServer(){
        if(clientIDs.isEmpty()){
            try {
                server.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void close(ObjectOutputStream oos, ObjectInputStream ois, Socket client){
        try {
            oos.close();
            ois.close();
            client.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void requestChangeID(ObjectOutputStream oos){
        try {
            communication_transfer pack_mess = new communication_transfer("server", null, "request_to_change_ID");
            oos.writeObject(pack_mess);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public static void main(String[] args){
        new Server_Center();
    }
}
