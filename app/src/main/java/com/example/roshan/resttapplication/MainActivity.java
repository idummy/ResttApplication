package com.example.roshan.resttapplication;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 0;
   // private boolean HAS_WRITE_PERMISSION = false;
    final String[] jsonString = new String[1];
    String appDirectoryPath;
    MyAdapter adapter;
    Button btn;
    ListView list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = findViewById(R.id.list);
        btn = findViewById(R.id.show);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //Direct asking for permission is not a standard way, ideally here should condition to check Users Previous Responses. This can be done
            //by ActivityCompat.shouldShowRequestPermissionRationale method.
            //ActivityCompat.shouldShowRequestPermissionRationale(this , Manifest.permission.WRITE_EXTERNAL_STORAGE);
            System.out.println("Storage Access Permission not granted..");
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_WRITE_EXTERNAL_STORAGE);
            //here PERMISSION_WRITE_EXTERNAL_STORAGE is an integer constant to map with onRequestPermissionResult callback method
        }
        else{
            System.out.println("Access Permission Granted!");
            appDirectoryPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RestApi/";
            File file = new File(appDirectoryPath);
            file.mkdir();
        }
        try {
            final ArrayList<FileObject> files = new ArrayList<>();
            jsonString[0] = generateJsonStr("listroot", "root");
            threadStart();
            String response = new String(NetworkThread.bytes);
            JSONObject jResponse = new JSONObject(response);
            JSONArray fJson = jResponse.getJSONArray("files");
            for (int i = 0; i < fJson.length(); i++) {
                JSONObject json = fJson.getJSONObject(i);
                FileObject fileObject = new FileObject();
                fileObject.setPath((String) json.get("name"));
                fileObject.setType((String) json.get("type"));
                fileObject.setName();
                files.add(fileObject);
            }
            //creating an Adapter Object and setting it on list View.
            adapter = new MyAdapter(this, R.layout.list_row, files);
            list.setAdapter(adapter);

            //Handling the single click
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    FileObject item = files.get(position);
                    if(item.getType().equalsIgnoreCase("dir")){
                        //make a list call to api with parent absolute path
                        try {
                            jsonString[0] = generateJsonStr("list",item.getPath());
                            threadStart();
                            String response = new String(NetworkThread.bytes);
                            System.out.println(response);
                            JSONObject jResponse = new JSONObject(response);
                            JSONArray fJson = jResponse.getJSONArray("files");
                            files.clear();
                            adapter.notifyDataSetChanged();
                            for (int i = 0; i < fJson.length(); i++) {
                                JSONObject json = fJson.getJSONObject(i);
                                FileObject fileObject = new FileObject();
                                fileObject.setPath((String) json.get("file"));
                                fileObject.setType((String) json.get("type"));
                                files.add(fileObject);
                            }
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    //Condition to download a file in case when item is a file, not a directory.
                    else{
                        Toast toast = Toast.makeText(getApplicationContext(),"DownloadingFile",Toast.LENGTH_LONG);
                        toast.show();
                        try {
                            jsonString[0] = generateJsonStr("download",item.getPath());
                            threadStart();
                            item.setName();
                            String fileName = item.getName();
                            File file = new File(appDirectoryPath,fileName);
                            if(!file.createNewFile()){
                                Log.d("File","file not exists");
                            }
                            if(writeByteStreamToFile(file,NetworkThread.bytes)){
                                Toast t = Toast.makeText(getApplicationContext(),"Download Completed",Toast.LENGTH_LONG);
                                t.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                return true;
            }
        });
    }


    private void threadStart() throws InterruptedException {
        NetworkThread networkThread = new NetworkThread(jsonString[0]);
        Thread backThread = new Thread(networkThread);
        backThread.start();
        backThread.join();
//       System.out.println("finished");
    }

    private String generateJsonStr(String request, String source) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("request", request);
        json.put("source", source);
        return json.toString();
    }

    private String generateJsonStr(String request, String source, String dest) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("request", request);
        json.put("source", source);
        json.put("target", dest);
        return json.toString();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_WRITE_EXTERNAL_STORAGE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                   // HAS_WRITE_PERMISSION = true;
                    appDirectoryPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RestApi/";
                    File file = new File(appDirectoryPath);
                    file.mkdir();
                }
            }
        }
    }

    private boolean writeByteStreamToFile(File file , byte[] inputBytes) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        out.write(inputBytes);
        return true;
    }
}
    /*    btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    jsonString[0] = generateJsonStr("listroot", source);
                    threadStart();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        btnRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    jsonString[0] = generateJsonStr("rename", source, dest);
                    threadStart();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    jsonString[0] = generateJsonStr("copy", source, dest);
                    threadStart();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    jsonString[0] = generateJsonStr("download", source);
                    threadStart();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    jsonString[0] = generateJsonStr("delete", source);
                    threadStart();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });*/