package com.example.AndroidChatClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import java.io.*;
import java.net.Socket;

public class LoginScreen extends Activity {
	
	private static EditText usernameInput;
    private static EditText passwordInput;

    private static Button buttonLogin;
    private static Button buttonRegister;

    private static String username;
    private static String password;

    private static final int SHOW_DIALOG=0;

    private static final String SERVER_ADDRESS = "192.168.115.196";
    private static final int PORT = 5000;

    private static final String END_COMMAND = "$end";
    private static final String START_COMMAND = "$start";
    private static final String SERVER_ACCEPT = "accept";

    private static final String START_CLIENT_INTENT = "android.intent.action.CLIENTINTERFACE";
    private static final String REGISTER_INTENT = "android.intent.action.REGISTERSCREEN";

    private static Socket socket;

    private static InputStreamReader inputStreamReader;
    private static BufferedReader bufferedReader;
    private static PrintStream printStream;

    private static String feedback;

    private static boolean gata=false;

    public void run(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(SERVER_ADDRESS, PORT);

                    inputStreamReader = new InputStreamReader(socket.getInputStream());
                    bufferedReader = new BufferedReader(inputStreamReader);
                    printStream = new PrintStream(socket.getOutputStream());

                    printStream.println(username+"&"+password);

                    feedback = "";

                    feedback = bufferedReader.readLine().toString();

                    bufferedReader.close();
                    inputStreamReader.close();
                    printStream.close();

                    socket.close();

                    gata=true;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
	
	OnClickListener loginListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {

            gata = false;

            username = usernameInput.getText().toString();
            password = passwordInput.getText().toString();

            if(!username.isEmpty() && !password.isEmpty() && !username.contains(END_COMMAND)  && !username.contains(START_COMMAND) &&
                    !password.contains(END_COMMAND)  && !password.contains(START_COMMAND) && !username.contains("&") && !username.contains("%")
                    && !password.contains("&") && !password.contains("%")) {

                run();

                while (!gata) ;

                if (feedback.equals(SERVER_ACCEPT)) {
                    startActivity(new Intent(START_CLIENT_INTENT));
                } else {
                    showDialog(SHOW_DIALOG);
                }
            } else {
                usernameInput.setText("");
                passwordInput.setText("");
                showDialog(SHOW_DIALOG);
            }
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		usernameInput = (EditText) findViewById(R.id.user_name);
		passwordInput = (EditText) findViewById(R.id.password);
		
		buttonLogin = (Button) findViewById(R.id.button_login);
		buttonLogin.setOnClickListener(loginListener);

        buttonRegister = (Button) findViewById(R.id.button_register_activity);
        buttonRegister.setOnClickListener(registerListener);
	}
	
	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {
		switch(id){
		case SHOW_DIALOG:
            return new AlertDialog.Builder(this).setMessage("Invalid username or password!").setPositiveButton("Ok", null).create();
    }
		return super.onCreateDialog(id);
	}

    OnClickListener registerListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(REGISTER_INTENT));
        }
    };
}
