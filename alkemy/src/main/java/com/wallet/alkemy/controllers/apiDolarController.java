package com.wallet.alkemy.controllers;

import java.util.Collections;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api") // 🎯 ¡CLAVE!: Agregamos el prefijo /api que le faltaba a la clase
public class apiDolarController {

    @GetMapping("/public/dolar-rates") // Ruta final unificada: /api/public/dolar-rates
    public ResponseEntity<String> getDolarRates() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String urlApiExterna = "https://dolarapi.com/v1/dolares";
            
            // 🛡️ CONFIGURACIÓN DE CABECERAS: Evita el bloqueo simulando un navegador legítimo
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36");
            
            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
            
            // Ejecutamos la petición segura incluyendo el User-Agent
            ResponseEntity<String> response = restTemplate.exchange(urlApiExterna, HttpMethod.GET, entity, String.class);
            
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            System.err.println("Error crítico en puente DolarApi: " + e.getMessage()); 
            return ResponseEntity.status(500).body("Error al consultar las cotizaciones externas");
        }
    }
}
