package com.example.roshan.resttapplication;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkThread implements Runnable {
    private String jsonRequest;
    static byte[] bytes;
    NetworkThread(String jsonRequest) {
        this.jsonRequest = jsonRequest;
    }

    public void run() {
        byte[] buffer = new byte[8192];

        try {
            URL apiURL = new URL("http://192.168.1.9:8080/DemoRestfulAPI/fileservlet");
            HttpURLConnection connection = (HttpURLConnection) apiURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            OutputStream out = connection.getOutputStream();
            out.write(this.jsonRequest.getBytes());
            out.close();
            InputStream in = connection.getInputStream();
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            int r;
            while ((r = in.read(buffer, 0, buffer.length)) != -1) {
                byteOut.write(buffer, 0, r);
            }
            byteOut.close();
//            System.out.println("I am running*********");
//            System.out.println(byteOut.toString());
            NetworkThread.bytes = byteOut.toByteArray();
//            System.out.print("Json In Thread: ");
//            System.out.println(NetworkThread.out);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
