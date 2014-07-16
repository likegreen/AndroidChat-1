package com.gabi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server extends JFrame {

    private static TextArea userEntry;
    private static TextField serverInput;

    private static ServerSocket serverSocket;

    private static Socket[] clientSockets = new Socket[1024];

    private static InputStreamReader[] clientStreamReaders = new InputStreamReader[1024];
    private static BufferedReader[] bufferedReaders = new BufferedReader[1024];
    private static PrintStream[] printStreams = new PrintStream[1024];


    private static BufferedReader fileBufferedReader;

    private static FileWriter fileWriter;

    private static int usersOnline =0;

    private static final String END_COMMAND = "$end";
    private static final String START_COMMAND = "$start";

    private static final String SERVER_ACCEPT = "accept";
    private static final String SERVER_REJECT = "reject";

    private static int maxUserId=0;

    public Server(){
        super("Java Server for Android Chat");
        setDefaultCloseOperation(Server.EXIT_ON_CLOSE);
        userEntry = new TextArea();
        add(userEntry, BorderLayout.CENTER);
        serverInput = new TextField();
        serverInput.addActionListener(sendMessageListener);
        add(serverInput, BorderLayout.SOUTH);
        this.setSize(640, 480);
        this.setVisible(true);
    }

    ActionListener sendMessageListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if(!serverInput.getText().toString().isEmpty() && !serverInput.getText().toString().equals(END_COMMAND) && !serverInput.getText().toString().equals(START_COMMAND) && usersOnline >0) {
                showMessage("\nServer> "+serverInput.getText().toString());

                int counter=0;
                int i=0;

                while(counter < usersOnline && i<=maxUserId) {
                    if (clientSockets[i] != null && clientSockets[i].isConnected()) {
                        sendMessage(serverInput.getText().toString(), i);
                        counter++;
                    }
                    i++;
                }

                serverInput.setText("");

            } else if(serverInput.getText().toString().equals(END_COMMAND) && !serverInput.getText().toString().equals(START_COMMAND) && usersOnline ==0 && serverSocket != null){
                try {
                    closeServer();
                    serverInput.setText("");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if(serverInput.getText().toString().equals(END_COMMAND) && !serverInput.getText().toString().equals(START_COMMAND) && usersOnline >0 && serverSocket != null) {
                try {
                    int counter=0;
                    int i=0;

                    while(counter< usersOnline && i<=maxUserId) {
                        if (clientSockets[i] != null && clientSockets[i].isConnected()) {
                            closeStreams(i);
                            counter++;
                        }
                        i++;
                    }

                    closeServer();
                    serverInput.setText("");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if(!serverInput.getText().toString().equals("") && !serverInput.getText().toString().equals(END_COMMAND) && !serverInput.getText().toString().equals(START_COMMAND) && usersOnline ==0) {
                showMessage("\nServer> "+serverInput.getText().toString());
                serverInput.setText("");
            } else if(serverInput.getText().toString().equals(START_COMMAND) && usersOnline ==0 && serverSocket == null){
                serverInput.setText("");

                try {
                    int counter = 0;
                    int i = 0;

                    while (counter< usersOnline && i<=maxUserId) {
                        if (clientSockets[i] != null && clientSockets[i].isConnected()) {
                            closeStreams(i);
                            counter++;
                        }
                        i++;
                    }

                    runServer(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void loadUsersList(){
        try {

            fileWriter = new FileWriter("usersList.txt", true);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addUser(String username, String password) throws IOException {

        StringTokenizer stringTokenizer;
        String userAndPass = "";

        boolean exista=false;

        fileBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("usersList.txt")));

        while ((userAndPass = fileBufferedReader.readLine()) != null) {
            stringTokenizer = new StringTokenizer(userAndPass);
            if(username.equals(stringTokenizer.nextToken())){
                exista=true;
                break;
            }
        }

        if(exista==false) {
            fileWriter.append(username + " " + password + String.format("%n"));
            fileWriter.flush();
            showMessage(username + " has been added to the list\n");
        }
    }

    public boolean checkForLogin(String user, String password) {
        StringTokenizer stringTokenizer;
        String userAndPass = "";

        String userTemp = "";
        String passwordTemp = "";

        try {

            fileBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("usersList.txt")));

            while ((userAndPass = fileBufferedReader.readLine()) != null) {
                stringTokenizer = new StringTokenizer(userAndPass);
                userTemp = stringTokenizer.nextToken();
                passwordTemp = stringTokenizer.nextToken();
                if(user.equals(userTemp) && password.equals(passwordTemp)){
                    return true;
                }
            }

        } catch (IOException e){
            e.printStackTrace();
        }

        return false;
    }

    public boolean checkForRegistration(String user){
        StringTokenizer stringTokenizer;
        String userAndPass = "";

        try {

            fileBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("usersList.txt")));

            while ((userAndPass = fileBufferedReader.readLine()) != null) {
                stringTokenizer = new StringTokenizer(userAndPass);
                if(user.equals(stringTokenizer.nextToken())){
                    return true;
                }
            }

        } catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    public void getUsersList(){
        String userAndPass="";

        StringTokenizer stringTokenizer;

        String user="";

        showMessage("\n----------------------------------------------\n");

        try {

            fileBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("usersList.txt")));

            while ((userAndPass = fileBufferedReader.readLine()) != null) {

                stringTokenizer = new StringTokenizer(userAndPass);

                user = stringTokenizer.nextToken();

                showMessage(user+"\n");
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            showMessage("----------------------------------------------\n");

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void sendMessage(String message, int userId) {
        printStreams[userId].println("Server> " + message);
        printStreams[userId].flush();
    }

    public void sendMessageToAllUsers(String message, int userId){

        int counter=0;
        int i=0;
        while(counter< usersOnline && i<=maxUserId) {
            if (clientSockets[i] != null && clientSockets[i].isConnected()) {
                printStreams[i].println("Client" + userId + "> " + message);
                printStreams[i].flush();
                counter++;
            }
            i++;
        }
    }

    public void showMessage(final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                userEntry.append(message);
            }
        }).start();
        try {
            Thread.sleep(75);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void runServer(final int userId){

        try {
            if(serverSocket == null) {
                showMessage("\nSetting up server...\n");

                serverSocket = new ServerSocket(5000);

                showMessage("\nServer started at address " + serverSocket.getInetAddress() + " on port " + serverSocket.getLocalPort() + "\n");

                showMessage("\nSetting users list...\n");

                loadUsersList();

                addUser("admin", "admin");
            }

            showMessage("\nUsers online: "+ usersOnline +"\n");

            getUsersList();

            showMessage("\nWaiting client...\n");

            if(clientSockets[userId] == null) {

                clientSockets[userId] = serverSocket.accept();
                usersOnline++;
                if (clientSockets[userId].isConnected() && userId >= maxUserId) maxUserId = userId;

                showMessage("\nClient " + userId + " has connected to the server\n");

                //
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runServer(userId + 1);
                    }
                }).start();
                //

                showMessage("\nConnected with address " + clientSockets[userId].getInetAddress() + " on port " + clientSockets[userId].getPort() + "\n");

                showMessage("\nSetting up streams...\n");

                setupStreams(userId);

                showMessage("\nStreams are set up\n");

                showMessage("\n----------------------------------------------\n");

                String message = "";

                try {
                    while (bufferedReaders[userId] != null && (message = (bufferedReaders[userId].readLine())) != null && clientSockets[userId] != null) {
                        if (message.contains(END_COMMAND) && clientSockets[userId].isConnected()) {
                            showMessage("\n----------------------------------------------\n");
                            showMessage("\nClient ended connection\n");
                            showMessage("\n----------------------------------------------\n");
                            usersOnline--;
                            resetStreamsAndResumeRunnning(userId);
                        }
                        if (message != null && !message.contains("&") && !message.contains("%")) {
                            showMessage("\nClient" + userId + "> " + message);
                            sendMessageToAllUsers(message, userId);
                        }

                        // feedback (succes/not) la logarea userului ///////////////////////////////////////////////////////

                        if (message.contains("&") && message != null && !message.contains("%") && clientSockets[userId].isConnected()) {

                            String username = "";
                            String password = "";

                            StringTokenizer stringTokenizer = new StringTokenizer(message);

                            username = stringTokenizer.nextToken("&");
                            password = stringTokenizer.nextToken("&");

                            if (checkForLogin(username, password)) {
                                showMessage("\nClient with the address " + clientSockets[userId].getInetAddress() + " logged in with username: " + username + " and password " + password + "\n");
                                printStreams[userId].println(SERVER_ACCEPT);
                                printStreams[userId].flush();
                                usersOnline--;
                                resetStreamsAndResumeRunnning(userId);
                                break;
                            } else {
                                showMessage("\nClient with the address " + clientSockets[userId].getInetAddress() + " attempted to log in with username: " + username + " and password " + password + "\n");
                                printStreams[userId].println(SERVER_REJECT);
                                printStreams[userId].flush();
                                usersOnline--;
                                resetStreamsAndResumeRunnning(userId);
                                break;
                            }
                        }

                        // inregistrare user cu feedback ///////////////////////////////////////////////////////////////////

                        if (message.contains("%") && message != null && !message.contains("&") && clientSockets[userId].isConnected()) {

                            String username = "";
                            String password = "";

                            StringTokenizer stringTokenizer = new StringTokenizer(message);

                            username = stringTokenizer.nextToken("%");
                            password = stringTokenizer.nextToken("%");

                            if (!checkForRegistration(username)) {
                                addUser(username, password);
                                printStreams[userId].println(SERVER_ACCEPT);

                                showMessage("\nClient with the address " + clientSockets[userId].getInetAddress() + " succefully registered with username: " + username + " and password " + password + "\n");
                                usersOnline--;
                                resetStreamsAndResumeRunnning(userId);
                                break;
                            } else {
                                showMessage("\nClient with the address " + clientSockets[userId].getInetAddress() + " unsuccefully registered with username: " + username + " and password " + password + "\n");
                                printStreams[userId].println(SERVER_REJECT);
                                usersOnline--;
                                resetStreamsAndResumeRunnning(userId);
                                break;
                            }
                        }
                        ////////////////////////////////////////////////////////////////////////////////////////////////////
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                if (bufferedReaders[userId] != null && clientStreamReaders[userId] != null && printStreams[userId] != null && clientSockets[userId] != null && clientSockets[userId].isConnected()) {

                    showMessage("\n----------------------------------------------\n");

                    showMessage("\n\nClosing streams...\n");

                    closeStreams(userId);
                    usersOnline--;

                    showMessage("\nStreams are closed\n");

                }
            } else {
                //
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runServer(userId + 1);
                    }
                }).start();
                //
            }

        } catch (IOException e){}
    }

    public void resetStreamsAndResumeRunnning(int userId){
        try {

            closeStreams(userId);

            runServer(userId);

        } catch (IOException e) {}
    }

    public void closeStreams(int userId) throws IOException {
        if(bufferedReaders[userId] != null && clientStreamReaders[userId] != null && printStreams[userId] != null && clientSockets[userId] != null) {
            bufferedReaders[userId].close();
            clientStreamReaders[userId].close();
            printStreams[userId].close();

            clientSockets[userId].close();

            bufferedReaders[userId] = null;
            clientStreamReaders[userId] = null;
            printStreams[userId] = null;

            clientSockets[userId] = null;
        }
    }

    public void closeServer() throws IOException {
        serverSocket.close();
        serverSocket = null;
        showMessage("\nServer ended connection\n");
    }

    public void setupStreams(int userId) throws IOException {
        clientStreamReaders[userId] = new InputStreamReader(clientSockets[userId].getInputStream());
        bufferedReaders[userId] = new BufferedReader(clientStreamReaders[userId]);
        printStreams[userId] = new PrintStream(clientSockets[userId].getOutputStream());
        printStreams[userId].flush();
    }

    public static void main(String[] args) {
        final Server server = new Server();
        server.runServer(0);
    }
}