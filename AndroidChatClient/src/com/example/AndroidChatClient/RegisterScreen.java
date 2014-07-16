package com.example.AndroidChatClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Created by gabriel.mercea on 7/8/2014.
 */

public class RegisterScreen extends Activity {

    private static EditText newUsername;
    private static EditText newPassword;

    private static Button buttonRegisterNewUser;

    private static Socket socket;
    private static InputStreamReader inputStreamReader;
    private static BufferedReader bufferedReader;
    private static PrintStream printStream;

    private static String username;
    private static String password;

    private static String feedback="";

    private static boolean gata=false;

    private static final int ERROR_DIALOG = 0;
    private static final int ERROR_CREATE_USER_DIALOG = 1;
    private static final int SUCCES_DIALOG = 2;

    private static final String SERVER_ADDRESS = "192.168.115.196";
    private static final int PORT = 5000;

    private static final String END_COMMAND = "$end";
    private static final String START_COMMAND = "$start";

    private static final String SERVER_ACCEPT = "accept";
    private static final String SERVER_REJECT = "reject";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        newUsername = (EditText) findViewById(R.id.register_username);
        newPassword = (EditText) findViewById(R.id.register_password);

        buttonRegisterNewUser = (Button) findViewById(R.id.button_register);
        buttonRegisterNewUser.setOnClickListener(registerListener);
    }

    View.OnClickListener registerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            gata=false;

            username = newUsername.getText().toString();
            password = newPassword.getText().toString();

            if(username.contains("&") || password.contains("&") || username.contains("%") || password.contains("%") || username.isEmpty() || password.isEmpty() ||
                    username.contains(END_COMMAND) || password.contains(END_COMMAND) || username.contains(START_COMMAND) || password.contains(START_COMMAND)) {
                gata=true;
                showDialog(ERROR_DIALOG);
            } else getRegisterSucces();

            while(!gata);

            if(feedback.equals(SERVER_ACCEPT)) {
                newUsername.setText("");
                newPassword.setText("");
                showDialog(SUCCES_DIALOG);
            } else if (feedback.equals(SERVER_REJECT)) {
                newUsername.setText("");
                newPassword.setText("");
                showDialog(ERROR_CREATE_USER_DIALOG);
            }
        }
    };

    public void getRegisterSucces(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(SERVER_ADDRESS, PORT);

                    inputStreamReader = new InputStreamReader(socket.getInputStream());
                    bufferedReader = new BufferedReader(inputStreamReader);
                    printStream = new PrintStream(socket.getOutputStream());

                    printStream.println(username+"%"+password);

                    feedback = bufferedReader.readLine();

                    bufferedReader.close();
                    printStream.close();
                    socket.close();

                    gata = true;

                } catch (IOException e){}

            }
        }).start();
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        switch (id) {
            case ERROR_DIALOG:
                return new AlertDialog.Builder(this).setMessage("Invalid username or password!").setPositiveButton("Ok", null).create();
            case ERROR_CREATE_USER_DIALOG:
                return new AlertDialog.Builder(this).setMessage("That username already exists!").setPositiveButton("Ok", null).create();
            case SUCCES_DIALOG:
                return new AlertDialog.Builder(this).setMessage("User registration completed!").setPositiveButton("Ok", null).create();
        }

        return super.onCreateDialog(id);
    }
}
