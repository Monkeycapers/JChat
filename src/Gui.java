/**
 * Created by Evan on 8/21/2016.
 */
import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.event.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

public class Gui {

    Settings settings;

    Client client;

    JFrame container;

    int width;
    int height;



    JTextArea textArea;

    JTextField textField;

    JScrollPane scrollPane;

    String title;

    boolean pendingMessage = false;
    boolean pendingCommand = false;
    boolean pendingDisconnect = false;
    public static boolean readyToConnect = false;
    public static boolean readyToDisconnect = false;
    public static boolean isConnected = false;
    String message = "Test";
    String nick = "Anon";

    int portNumber;
    String hostName;

    public Gui (Settings settings) {

        this.width = settings.GUI_WIDTH;
        this.height = settings.GUI_HEIGHT;
        this.title = settings.GUI_TITLE;
        readyToConnect = settings.autoConnect;
        this.portNumber = settings.portNumber;
        this.hostName = settings.hostName;

    }

    public void load () {
        container = new JFrame(title);

        textArea = new JTextArea(width, height);
        textArea.setText(title + " " + new java.util.Date());
        textArea.setVisible(true);
        textArea.setAutoscrolls(true);
        textArea.setEditable(false);
        scrollPane = new JScrollPane(textArea);
        scrollPane.setAutoscrolls(true);
        container.add(scrollPane, BorderLayout.NORTH);

        Action action = new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {

                message = textField.getText();
                if (message.toLowerCase().startsWith("/nick")) {
                    try {
                        nick = message.substring(6);
                        addText("Changed nick to: " + nick);
                    }
                    catch (Exception ex) {
                        addText("Could not change nickname.");
                    }
                }
                else if (message.toLowerCase().startsWith("/connect")) {
                    readyToConnect = !readyToConnect;
                }
                else if (message.toLowerCase().startsWith("/set host")) {
                    try {
                        hostName = message.substring(10);
                        addText("Set hostname to: " + hostName);
                        //Todo: This is broken
                        //client = new Client(hostName, portNumber);
                        //client.run();
                        //settings.hostName = hostName;
                    }
                    catch (Exception ex) {
                        addText("Could not set host");
                    }
                }
                else if (message.toLowerCase().startsWith("/set port")) {
                    try {
                        portNumber = Integer.parseInt(message.substring(10));
                        addText("Set the port number to: " + portNumber);
                        //Todo: This is broken
                        //client = new Client(hostName, portNumber);
                        //client.run();
                        //settings.portNumber = portNumber;
                    }
                    catch (Exception ex) {
                        addText("Could not set port");
                    }
                }
                else if (message.toLowerCase().startsWith("/status")) {
                    addText("NickName: " + nick + "\n" + "Host: " + hostName + "\n" + "Port: " + portNumber + "\n" + "Connected: " + isConnected);

                }
                else if (message.toLowerCase().startsWith("/alluserlist")) {
                    message = nick + ",alluserlist";
                    pendingCommand = true;
                }
                else if (message.toLowerCase().startsWith("/userlist")) {
                    message = nick + ",userlist";
                    pendingCommand = true;
                }
                else if (message.toLowerCase().startsWith("/stop")) {
                    message = nick + ",stop";
                    pendingCommand = true;
                }
                else if (message.toLowerCase().startsWith("/auth")) {
                    try {
                        String[] split = message.split(" ");
                        String user = split[1];
                        String pass = split[2];
                        System.out.println(pass);
                        message = nick + ",auth," + user + "," + pass;
                        pendingCommand = true;
                    }
                    catch (Exception ex) {
                        addText("invalid usage of command: /auth");
                    }
                }
                else if (message.toLowerCase().startsWith("/signup")) {
                    try {
                        String[] split = message.split(" ");
                        String user = split[1];
                        String pass = split[2];
                        message = nick + ",signup," + user + "," + pass;

                        pendingCommand = true;
                    }
                    catch (Exception ex) {
                        addText("invalid usage of command: /auth");
                    }
                }
                else if (message.toLowerCase().startsWith("/promote")) {
                    String[] split = message.split(" ");
                    String user = split[1];
                    String rank = split[2];
                    message = nick + ",promote," + user + "," + rank;
                    pendingCommand = true;
                }
                else if (message.toLowerCase().startsWith("/pm")) {
                    String[] split = message.split(" ");
                    String user = split[1];
                    String msg = split[2];
                    message = nick + ",pm," + user + "," +  msg;
                    pendingCommand = true;
                }
                else if (message.toLowerCase().startsWith("/delete")) {
                    String[] split = message.split(" ");
                    String user = split[1];
                    //String rank = split[2];
                    message = nick + ",delete," + user;
                    pendingCommand = true;
                }
                else if (message.toLowerCase().startsWith("/disconnect")) {
                    message = nick + ",disconnect,";
                    pendingCommand = true;
                    pendingDisconnect = true;
                }
                else {
                    pendingMessage = true;
                }
                textField.setText("");
                //scrollPane.scrollRectToVisible(textArea.getBounds());

            }
        };
        textField = new JTextField();
        textField.addActionListener(action);
        container.add(textField, BorderLayout.SOUTH);

        container.pack();
        container.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        container.setVisible(true);
        container.setResizable(true);


        //TODO: Grab from settings
        client = new Client(hostName, portNumber);
        client.run();


    }

    public void addText(String text) {
        textArea.append("\n" + text);
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }



    private class Client implements Runnable {
        String hostName;
        int portNumber;
        public Client(String hostName, int portNumber) {
            this.hostName = hostName;
            this.portNumber = portNumber;
        }
        public void run() {
            while (true) {

                if (readyToConnect) {
                    //Get a Socket connection to the host
                    try (
                            Socket echoSocket = new Socket(hostName, portNumber);     //new InputStreamReader(System.in))
                            OutputStream outToServer = echoSocket.getOutputStream();
                            DataOutputStream out = new DataOutputStream(outToServer);
                            InputStream inFromServer = echoSocket.getInputStream();
                            DataInputStream in = new DataInputStream(inFromServer);
                    ) {
                        //TODO: Verify if the server is a JChat server
                        addText("Connected to a JChat server on " + hostName + ":" + portNumber);

                        String line = "";
                        boolean running = true;
                        isConnected = true;
                        String oldLine = "";
                        while (running) {
                            try {
                                 oldLine = "";
                                //if (!(line.startsWith("[Server]"))) {
                                    oldLine = line;
                                //}


                                line = in.readUTF();
                                if (!(oldLine.equals(line)) && !line.equals("")) {
                                    DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                                    Date date = new Date();
                                    addText( "(" + dateFormat.format(date) + ") " + line);
                                }

                            } catch (IOException e) {
                                System.out.println("Could not receive a message");
                                running = false;
                            }

                            try {
                                if (pendingMessage) {
                                    System.out.println("sent a message");
                                    pendingMessage = false;
                                    out.writeUTF("<" + nick + ">  " + message);
                                }
                                else if (pendingCommand) {

                                    System.out.println("sent a command");
                                    pendingCommand = false;
                                    out.writeUTF(message);
                                    if (pendingDisconnect) {
                                        pendingDisconnect = false;
                                        running = false;
                                        readyToConnect = false;
                                        pendingMessage = false;
                                        isConnected = false;
                                    }
                                }
                                else {
                                    out.writeUTF(nick + ",Alive");
                                }
                            } catch (IOException e) {
                                System.out.println("Could not send a message");
                                running = false;
                            }

                            try {
                                Thread.sleep(1000);
                            } catch (Exception ex) {
                            }

                        }

                        //
                        //Error in connecting to the host
                    } catch (UnknownHostException e) {
                        addText("Don't know about host " + hostName);
                        System.exit(-1);
                    } catch (IOException e) {
                        //This can happen if the server has an error or closes
                        addText("Couldn't get I/O for the connection to " + hostName);
                        readyToConnect = false;
                    } catch (Exception e) {
                        addText("Unknown error");
                        System.exit(-1);
                    }

                }
                else {
                    try {
                        Thread.sleep(settings.delay);
                    } catch (Exception ex) {
                    }
                }
            }
        }
    }
}
