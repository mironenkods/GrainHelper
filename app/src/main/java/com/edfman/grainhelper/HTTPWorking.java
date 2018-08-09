package com.edfman.grainhelper;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HTTPWorking {

    public class request_answer {
        public int code;
        public String Body;

        public request_answer(int code, String Body) {
            this.code = code;
            this.Body = Body;
        }

        public int getCode() {
            return code;
        }

        public String getBody() {
            return Body;
        }
    }

    request_answer callWebService(String serviceURL, String method){
        // http get client
        URL url = null;
        request_answer answer=null;
        InputStream in_stream = null;
        try {
            url = new URL(serviceURL);
        } catch (MalformedURLException e) {
            Log.e("URISyntaxException", e.toString());
        }
        HttpURLConnection urlConnection = null;
        try {
            if (url != null){
                urlConnection = (HttpURLConnection) (url.openConnection());
                /*if (method.equals("POST")) {
                    //urlConnection.setDoOutput(true);
                    //urlConnection.setDoInput(true);
                }
                else {
                    urlConnection.setRequestMethod(method);
                    //urlConnection.setDoInput(true);
                    //urlConnection.setDoOutput(false);
                }*/
                urlConnection.connect();
            }

        } catch (IOException e){
            Log.e("URISyntaxException", e.toString());

        }

        try {
            if (urlConnection != null) {
//                in_stream = new BufferedInputStream(urlConnection.getInputStream());
                try {
                    in_stream = urlConnection.getInputStream();
                }
                catch (FileNotFoundException e){
                    Log.e("FileNotFoundException", e.toString());
                }

                //readStream(in);
            }
        }
        catch (IOException e) {
            Log.e("IOException", e.toString());

        }
//        finally {
//            if (urlConnection != null) {
//                urlConnection.disconnect();
//            }
//        }

        // Создаем BufferedReader дял чтения ответа
        BufferedReader in=null;
        // и HttpResponse для получения ответа
//        try{
        //// выполняем запрос
//            response= client.execute(getRequest);
//        } catch(ClientProtocolException e){
//            Log.e("ClientProtocolException", e.toString());
//        } catch(IOException e){
//            Log.e("IO exception", e.toString());
//        }
        StringBuffer buff = new StringBuffer("");
        request_answer req = null;
        if (in_stream != null) {

            try {
                in = new BufferedReader(new InputStreamReader(in_stream));

                String line = "";
                while ((line = in.readLine()) != null) {
                    buff.append(line);
                }
            } catch (IllegalStateException e) {
                Log.e("IllegalStateException", e.toString());
            } catch (IOException e) {
                Log.e("IO exception", e.toString());
                return new request_answer(500, e.getMessage());
            }
            finally{
                try {
                    if (in != null) in.close();
                } catch (IOException e) {

                }
            }
        }

        // возвращаем ответ в виде строки текста
        try {
            req = new request_answer(urlConnection.getResponseCode(), buff.toString());
            urlConnection.disconnect();
        } catch (IOException e) {
            Log.e("IO exception", e.toString());
            urlConnection.disconnect();
        }
        return req;
    }



}
