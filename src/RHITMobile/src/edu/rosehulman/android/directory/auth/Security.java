package edu.rosehulman.android.directory.auth;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.provider.Settings.Secure;
import android.util.Base64;
import android.util.Log;
import edu.rosehulman.android.directory.C;

public class Security {
	
	private static final String KEY_PART = "MiNwYzj3qAjLXtUhRCs1wZbhDG5eTY957Vbo";
	
	private static final String KEYGEN_ALGORITHM = "PBEWITHSHAAND256BITAES-CBC-BC";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateIV() {
    	byte[] iv = new byte[16];
    	RANDOM.nextBytes(iv);
    	return toString(iv);
    }
    
	public static String generateKeyPart() {
		try {
			KeyGenerator keygen = KeyGenerator.getInstance("AES");
			keygen.init(128);
			byte key[] = keygen.generateKey().getEncoded();
			return toString(key);
			
		} catch (Exception e) {
			throw new RuntimeException("Invalid environment", e);
		}
	}
	
	public static String encrypt(Context context, String keyPart, String iv, String password) {
		
		byte salt[] = new byte[6];
		RANDOM.nextBytes(salt);
		
		Cipher c;
		try {
			KeySpec keySpec = new PBEKeySpec(getKey(context, keyPart), salt, 1024, 256);
			
			SecretKeyFactory factory = SecretKeyFactory.getInstance(KEYGEN_ALGORITHM);
		
			SecretKey tmp = factory.generateSecret(keySpec);
			SecretKey key = new SecretKeySpec(tmp.getEncoded(), "AES");
		
			c = Cipher.getInstance(CIPHER_ALGORITHM);
			c.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(fromString(iv)));
			
		} catch (GeneralSecurityException e) {
			throw new RuntimeException("Invalid environment", e);
		}
		
		try {
			return toString(salt) + 
					toString(c.doFinal(password.getBytes("UTF-8")));
			
		} catch (GeneralSecurityException e) {
           Log.e(C.TAG, "Error encrypting: " + e.getLocalizedMessage(), e);
           return null;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Invalid environment", e);
		}	
	}
	
	public static String decrypt(Context context, String keyPart, String iv, String encrypted) {
		if (keyPart == null || iv == null || encrypted == null) {
			return null;
		}
		
		byte salt[] = fromString(encrypted.substring(0, 8));
		
		String encryptedPassword = encrypted.substring(8); 
		
		Cipher c;
		try {
			KeySpec keySpec = new PBEKeySpec(getKey(context, keyPart), salt, 1024, 256);
			
			SecretKeyFactory factory = SecretKeyFactory.getInstance(KEYGEN_ALGORITHM);
		
			SecretKey tmp = factory.generateSecret(keySpec);
			SecretKey key = new SecretKeySpec(tmp.getEncoded(), "AES");
		
			c = Cipher.getInstance(CIPHER_ALGORITHM);
			c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(fromString(iv)));
			
		} catch (GeneralSecurityException e) {
			throw new RuntimeException("Invalid environment", e);
		}
		
		try {
			return new String(c.doFinal(fromString(encryptedPassword)), "UTF-8");
			
		} catch (GeneralSecurityException e) {
            Log.e(C.TAG, "Error decrypting: " + e.getLocalizedMessage(), e);
            return null;
		} catch (IllegalArgumentException e) {
            Log.e(C.TAG, "Error decrypting: " + e.getLocalizedMessage(), e);
            return null;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Invalid environment", e);
		}	
	}

	private static String toString(byte[] bytes) {
		return Base64.encodeToString(bytes, Base64.NO_WRAP);
	}
	
	private static byte[] fromString(String data) {
		return Base64.decode(data, Base64.NO_WRAP);
	}
	
	private static char[] getKey(Context context, String keyPart) {
        String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        String packageName = context.getPackageName();

        return (keyPart + deviceId + packageName + KEY_PART).toCharArray();
    }
}
