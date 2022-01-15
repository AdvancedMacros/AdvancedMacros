package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ThreeArgFunction;
import java.io.IOException;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.lang.Integer;
import java.net.UnknownHostException;

public class SOCKET extends ThreeArgFunction {
	@Override
	public LuaValue call(LuaValue valueL, LuaValue addressL, LuaValue portL) {
        String value = valueL.tojstring();
		String address = addressL.tojstring();
        String port = portL.tojstring();
        
        try {
            // need host and port, we want to connect to the ServerSocket at port 7777
            Socket socket = new Socket(address, Integer.parseInt(port));
            System.out.println("Connected!");

            // get the output stream from the socket.
            OutputStream outputStream = socket.getOutputStream();
            // create a data output stream from the output stream so we can send data through it
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            System.out.println("Sending string to the ServerSocket");

            // write the message we want to send
            dataOutputStream.writeUTF(value);
            dataOutputStream.flush(); // send the message
            dataOutputStream.close(); // close the output stream when we're done.

            System.out.println("Closing socket and terminating program.");
            socket.close();
        }catch(UnknownHostException u) {
            u.printStackTrace();
        }catch(IOException e) {
            e.printStackTrace();
        }

        return valueL;
	}
}
