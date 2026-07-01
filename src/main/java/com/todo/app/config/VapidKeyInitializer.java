package com.todo.app.config;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;

@Component
public class VapidKeyInitializer {

    @Value("${vapid.public:}")
    private String vapidPublic;

    @Value("${vapid.private:}")
    private String vapidPrivate;

    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
        
        if (vapidPublic == null || vapidPublic.isBlank() || vapidPublic.contains("YOUR_VAPID") ||
            vapidPrivate == null || vapidPrivate.isBlank() || vapidPrivate.contains("YOUR_VAPID")) {
            
            System.out.println("⚠️ VAPID keys not configured. Generating on-the-fly EC Keypair...");
            generateAndWriteKeys();
        } else {
            System.out.println("✅ VAPID keys loaded successfully.");
        }
    }

    private void generateAndWriteKeys() {
        try {
            ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("prime256v1");
            KeyPairGenerator g = KeyPairGenerator.getInstance("ECDH", "BC");
            g.initialize(ecSpec, new SecureRandom());
            KeyPair pair = g.generateKeyPair();

            // Get uncompressed public key bytes
            ECPublicKey pub = (ECPublicKey) pair.getPublic();
            byte[] x = pub.getW().getAffineX().toByteArray();
            byte[] y = pub.getW().getAffineY().toByteArray();

            byte[] xBytes = new byte[32];
            byte[] yBytes = new byte[32];

            System.arraycopy(x, Math.max(0, x.length - 32), xBytes, Math.max(0, 32 - x.length), Math.min(32, x.length));
            System.arraycopy(y, Math.max(0, y.length - 32), yBytes, Math.max(0, 32 - y.length), Math.min(32, y.length));

            byte[] uncompressed = new byte[65];
            uncompressed[0] = 0x04;
            System.arraycopy(xBytes, 0, uncompressed, 1, 32);
            System.arraycopy(yBytes, 0, uncompressed, 33, 32);

            String publicKey = Base64.getUrlEncoder().withoutPadding().encodeToString(uncompressed);

            // Get private key
            ECPrivateKey priv = (ECPrivateKey) pair.getPrivate();
            byte[] s = priv.getS().toByteArray();
            byte[] sBytes = new byte[32];
            System.arraycopy(s, Math.max(0, s.length - 32), sBytes, Math.max(0, 32 - s.length), Math.min(32, s.length));
            
            String privateKey = Base64.getUrlEncoder().withoutPadding().encodeToString(sBytes);

            System.out.println("==================================================");
            System.out.println("🔑 GENERATED VAPID KEYS:");
            System.out.println("Public Key:  " + publicKey);
            System.out.println("Private Key: " + privateKey);
            System.out.println("==================================================");

            // Write to a text file in project root
            File f = new File("D:/Todo-lists-main/vapid_keys.txt");
            try (FileWriter writer = new FileWriter(f)) {
                writer.write("vapid.public=" + publicKey + "\n");
                writer.write("vapid.private=" + privateKey + "\n");
            }
            System.out.println("✅ Keys saved to D:/Todo-lists-main/vapid_keys.txt");

        } catch (Exception e) {
            System.err.println("❌ Failed to generate VAPID keys: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
