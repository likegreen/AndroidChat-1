package com.example.AndroidChatClient;

import java.io.*;
import java.net.Socket;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ClientInterface extends Activity {

    private static Socket socket;

    private static InputStreamReader serverIsr;
    private static BufferedReader bufferedReader;
    private static PrintStream printStream;

    private static TextView chatWindow;
    private static EditText userInput;
    private static Button buttonSendMessage;
	
	private static final int SHOW_DIALOG=0;

    private static final String SERVER_ADDRESS = "192.168.115.196";
    private static final int PORT = 5000;

    private static final String END_COMMAND = "$end";
    private static final String START_COMMAND = "$start";

	
	OnClickListener sendMessageListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(!userInput.getText().toString().equals("")) {
                if(!(userInput.getText().toString().equals(END_COMMAND)) && !(userInput.getText().toString().equals(START_COMMAND)) && socket != null && socket.isConnected()) {
                    sendMessage(userInput.getText().toString());
                    userInput.setText("");
                } else if((userInput.getText().toString().equals(END_COMMAND)) && !(userInput.getText().toString().equals(START_COMMAND)) && socket != null && socket.isConnected()) {
                        sendMessage(userInput.getText().toString());
                        userInput.setText("");
                } else if(!(userInput.getText().toString().equals(END_COMMAND)) && (userInput.getText().toString().equals(START_COMMAND)) && socket != null && socket.isConnected()) {
                    try {
                        closeStreams();
                        runClient();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    showMessage("\nSomething went wrong\n");
                    userInput.setText("");
                }
            }
        }
	};

    public void sendMessage(final String message){
        new Thread(new Runnable() {
            @Override
            public void run() {
                printStream.println(message);
                printStream.flush();
            }
        }).start();
    }
	
	
	@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.client_layout);
			
			chatWindow = (TextView) findViewById(R.id.user_text_area);
			userInput = (EditText) findViewById(R.id.user_text);
			buttonSendMessage = (Button) findViewById(R.id.button_send_message);

			buttonSendMessage.setOnClickListener(sendMessageListener);
			runClient();
		}
	
	public void runClient(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }

                    showMessage("\nConnecting...");
                    socket = new Socket(SERVER_ADDRESS, PORT);
                    showMessage("\nConnected with address: "+socket.getInetAddress()+" on port "+socket.getPort());

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }

                    showMessage("\nSetting up streams...");

                    serverIsr = new InputStreamReader(socket.getInputStream());
                    bufferedReader = new BufferedReader(serverIsr);
                    printStream = new PrintStream(socket.getOutputStream());
                    printStream.flush();

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }

                    showMessage("\nStreams are set up\n");

                    showMessage("\n--------------------------------------");

                    String message = "";

                    try {
                        while (!(message = (String) (bufferedReader.readLine())).equals("Server> "+END_COMMAND) && socket != null && socket.isConnected() ) {
                            if(message != null) showMessage("\n" + message);
                            else continue;
                        }
                    } catch (NullPointerException e){
                        e.printStackTrace();
                        closeStreams();
                    }

                    closeStreams();

                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
	}

    public void closeStreams() throws IOException {
        showMessage("\n--------------------------------------\n");

        showMessage("\n\nClosing streams...\n");

        bufferedReader = null;
        serverIsr = null;
        printStream = null;

        socket = null;

        showMessage("Streams are closed\n");

        showMessage("\n--------------------------------------\n");
    }
	
	public void showMessage(final String message){
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				chatWindow.append(message);
			}
		});
	}
	
	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case SHOW_DIALOG:
			return new AlertDialog.Builder(this).setMessage("Exception").create();
        }
		
		return super.onCreateDialog(id);
	}

    @Override
    protected void onPause() {
        super.onPause();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socket != null) sendMessage(END_COMMAND);
                    if (socket != null) closeStreams();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
