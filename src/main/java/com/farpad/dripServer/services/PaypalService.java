package com.farpad.dripServer.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Service
@NoArgsConstructor
public class PaypalService {
    @Value("${drip-app.paypal-client-id}")
    private String paypalClientId;
    @Value("${drip-app.paypal-client-secret}")
    private String paypalClientSecret;

    public String getBearer() {
        try {
            URL url = new URI("https://api-m.sandbox.paypal.com/v1/oauth2/token").toURL();
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("POST");
            String auth = paypalClientId + ":" + paypalClientSecret;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeaderValue = "Basic " + new String(encodedAuth);

            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConn.setRequestProperty("Authorization", authHeaderValue);
            httpConn.setRequestProperty("PayPal-Request-Id", "DRIP-DYNASTY-BEARER");

            httpConn.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
            writer.write("grant_type=client_credentials");
            writer.flush();
            writer.close();
            httpConn.getOutputStream().close();

            InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                    ? httpConn.getInputStream()
                    : httpConn.getErrorStream();
            Scanner s = new Scanner(responseStream).useDelimiter("\\A");
            String response = s.hasNext() ? s.next() : "";
            if(!response.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(response);
                response = jsonResponse.get("access_token").asText();
            }
            return response;
        } catch (IOException | URISyntaxException ignored) {
            return "";
        }
    }

    public String createOrder(String refId, String price) {
        try {
            URL url = new URI("https://api-m.sandbox.paypal.com/v2/checkout/orders").toURL();
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("POST");

            httpConn.setRequestProperty("Content-Type", "application/json");
            httpConn.setRequestProperty("Authorization", "Bearer " + getBearer());

            httpConn.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
            writer.write("{ \"intent\": \"CAPTURE\", \"purchase_units\": [ { \"reference_id\": \"" + refId + "\", \"amount\": { \"currency_code\": \"HUF\", \"value\": \"" + price + "\" } } ] }");
            writer.flush();
            writer.close();
            httpConn.getOutputStream().close();

            InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                    ? httpConn.getInputStream()
                    : httpConn.getErrorStream();
            Scanner s = new Scanner(responseStream).useDelimiter("\\A");
            String response = s.hasNext() ? s.next() : "";
            if(!response.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(response);
                response = jsonResponse.get("id").asText();
            }
            return response;
        } catch (IOException | URISyntaxException ignored) {
            return "";
        }
    }

    public boolean validateOrder(String transactionId) {
        try {
            URL url = new URI("https://api-m.sandbox.paypal.com/v2/checkout/orders/" + transactionId).toURL();
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("GET");

            httpConn.setRequestProperty("Content-Type", "application/json");
            httpConn.setRequestProperty("Authorization", "Bearer " + getBearer());

            InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                    ? httpConn.getInputStream()
                    : httpConn.getErrorStream();
            Scanner s = new Scanner(responseStream).useDelimiter("\\A");
            String response = s.hasNext() ? s.next() : "";
            if(!response.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(response);
                response = jsonResponse.get("status").asText();
            }
            return response.equalsIgnoreCase("COMPLETED");
        } catch (IOException | URISyntaxException ignored) {
            return false;
        }
    }
}
