/**
 * Created by Evan on 8/21/2016.
 */
import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.event.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.io.*;

public class Gui {

    Client client;

    JFrame container;

    int width;
    int height;

    JTextArea textArea;

    JTextField textField;

    JScrollPane scrollPane;

    String title;

    boolean pendingMessage = false;
    String message = "Test";
    String nick = "Anon";

    public Gui (Settings settings) {

        this.width = settings.GUI_WIDTH;
        this.height = settings.GUI_HEIGHT;
        this.title = settings.GUI_TITLE;

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
                if (message.startsWith("/nick")) {
                    try {
                        nick = message.substring(6);
                    }
                    catch (Exception ex) {
                        addText("Could not change nickname.");
                    }
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



        client = new Client("localhost", 16000);
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
            addText("Starting JChat internal server...");
            while (true) {
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
                    while (running) {
                        try {
                            String oldLine = line;
                            line = in.readUTF();
                            if (!(oldLine.equals(line))) {
                                addText(line);
                            }

                        }
                        catch (IOException e) {
                            System.out.println("Could not receive a message");
                            running = false;
                        }

                        try {
                            if (pendingMessage) {
                                System.out.println("sent a message");
                                pendingMessage = false;
                                out.writeUTF("<" + nick + ">  " + message);
                            }
                            else {
                                out.writeUTF(nick + ",Alive");
                            }
                        }
                        catch (IOException e) {
                            System.out.println("Could not send a message");
                            running = false;
                        }

                        try {Thread.sleep(1000); } catch (Exception ex) { }

                    }

                    //
                    //Error in connecting to the host
                } catch (UnknownHostException e) {
                    addText("Don't know about host " + hostName);
                    System.exit(-1);
                } catch (IOException e) {
                    //This can happen if the server has an error or closes
                    addText("Couldn't get I/O for the connection to " + hostName);
                } catch (Exception e) {
                    addText("Unknown error");
                    System.exit(-1);
                }

            }

            //


        }

    }

    private class MessageReceiver implements Runnable {
        DataInputStream in;
        public MessageReceiver(DataInputStream in) {
            this.in = in;
        }
        public void run() {
            String line = "";
            boolean running = true;
            while (running) {
                try {
                    String oldLine = line;
                    line = in.readUTF();
                    if (!(oldLine.equals(line))) {
                        addText(line);
                    }

                }
                catch (IOException e) {
                    System.out.println("Could not receive a message");
                    running = false;
                }

            }
        }
    }

    private class MessageSender implements Runnable {
        DataOutputStream out;
        public MessageSender(DataOutputStream out) {
            this.out = out;
        }
        public void run() {
            boolean running = true;
            while (running) {
                try {
                    if (pendingMessage) {
                        System.out.println("sent a message");
                        pendingMessage = false;
                        out.writeUTF("<" + nick + "> :" + message);
                    }
                    else {
                        out.writeUTF(nick + ",Alive");
                    }
                }
                catch (IOException e) {
                    System.out.println("Could not send a message");
                    running = false;
                }
            }
        }
    }



}
