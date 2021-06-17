package org.jobtests;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;

public class KeyKeeper {
    private static KeyKeeper instance;
    private static Key key;
    private KeyKeeper() {}
    public static KeyKeeper getInstance() {
        if (instance == null){
            instance = new KeyKeeper();
            key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }
        return instance;
    }
    public static Key getKey() {
        if (key == null)
            getInstance().getKey();
        return key;
    }
}
