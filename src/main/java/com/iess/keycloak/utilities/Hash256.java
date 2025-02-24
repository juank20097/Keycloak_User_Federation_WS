package com.iess.keycloak.utilities;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash256 {

	public  String hashPasswordWithSHA256(String password) throws NoSuchAlgorithmException {
        // Crear una instancia de MessageDigest con el algoritmo SHA-256
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // Aplicar el hash al password
        byte[] hashedBytes = digest.digest(password.getBytes());

        // Convertir el arreglo de bytes a una cadena hexadecimal
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashedBytes) {
            // Convertir cada byte en su formato hexadecimal
            hexString.append(String.format("%02x", b));
        }

        // Devolver el hash como una cadena hexadecimal
        return hexString.toString();
    }
}
