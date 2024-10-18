package com.example.lucky_social_network.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImgurTokenRetriever {
    public static void main(String[] args) {
        try {
            String url = "https://api.imgur.com/oauth2/token";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Ваши Client ID и Client Secret
            String clientId = "9a20435fe3d92ba"; // Ваш Client ID
            String clientSecret = "c6b259f06edd161a21677c4c017ed691968998db"; // Ваш Client Secret
            String authCode = "c47e50a5b7e7f8ef90c8436d3d0803e08516f141"; // Ваш код авторизации

            // Параметры запроса
            String urlParameters = "client_id=" + clientId +
                    "&client_secret=" + clientSecret +
                    "&grant_type=authorization_code" +
                    "&code=" + authCode;

            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                os.write(urlParameters.getBytes());
                os.flush();
            }

            int responseCode = con.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            // Обработка ответа
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    // Выводим успешный ответ
                    System.out.println("Response: " + response.toString());
                }
            } else {
                System.out.println("Error: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}