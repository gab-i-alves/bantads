package br.ufpr.dac.bantads.ms_auth.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Hashing de senha SHA256+SALT (NF11 da spec BANTADS).
 *
 * O esquema precisa ser idêntico em todos os caminhos (seed, criação na saga e
 * verificação no login), senão o login das contas seedadas e da senha temporária
 * da saga deixa de validar. Por isso a ordem de concatenação e o encoding ficam
 * centralizados aqui como única fonte de verdade.
 *
 * Esquema: hash = hex(SHA-256(salt + senha)), salt em hex, ambos UTF-8.
 */
public final class PasswordHasher {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SALT_BYTES = 16;

    private PasswordHasher() {
    }

    // salt aleatório de 16 bytes via SecureRandom, devolvido em hex pra caber
    // em String sem precisar de encoding extra no Mongo.
    public static String gerarSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return toHex(salt);
    }

    // ordem de concatenação fixa: salt + senha (a MESMA usada na verificação).
    public static String hash(String senhaClara, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] entrada = (salt + senhaClara).getBytes(StandardCharsets.UTF_8);
            return toHex(digest.digest(entrada));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 é exigido pela plataforma Java, então isso não acontece em runtime.
            throw new IllegalStateException("algoritmo SHA-256 indisponível", e);
        }
    }

    // comparação constant-time pra não vazar timing na verificação da senha.
    public static boolean verificar(String senhaClara, String salt, String hashEsperado) {
        if (salt == null || hashEsperado == null) {
            return false;
        }
        String calculado = hash(senhaClara, salt);
        return MessageDigest.isEqual(
                calculado.getBytes(StandardCharsets.UTF_8),
                hashEsperado.getBytes(StandardCharsets.UTF_8));
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
