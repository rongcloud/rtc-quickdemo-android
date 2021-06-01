package cn.rongcloud.demo.common;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class MockAppServer {

    private static final String TOKEN_SERVER = "https://api-cn.ronghub.com";

    public static void getToken(final String appKey, final String appSecret, final String userId, final GetTokenCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long timestamp = System.currentTimeMillis();
                int nonce = new Random().nextInt(9999) + 10000;
                String signature = sha1(appSecret + nonce + timestamp);

                InputStream is = null;
                OutputStream out = null;
                ByteArrayOutputStream message = null;
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(TOKEN_SERVER + "/user/getToken.json").openConnection();
                    conn.setRequestMethod("POST");
                    conn.setReadTimeout(5000);
                    conn.setConnectTimeout(5000);
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setUseCaches(false);
                    conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.addRequestProperty("Timestamp", String.valueOf(timestamp));
                    conn.addRequestProperty("Nonce", String.valueOf(nonce));
                    conn.addRequestProperty("Signature", signature);
                    conn.addRequestProperty("App-Key", appKey);

                    String data = "userId=" + userId;
                    out = conn.getOutputStream();
                    out.write(data.getBytes());
                    out.flush();
                    if (conn.getResponseCode() == 200) {
                        is = conn.getInputStream();
                        message = new ByteArrayOutputStream();
                        int len = 0;
                        byte[] buffer = new byte[1024];
                        while ((len = is.read(buffer)) != -1) {
                            message.write(buffer, 0, len);
                        }

                        String msg = new String(message.toByteArray());
                        JSONObject jsonObject = new JSONObject(msg);
                        if (jsonObject.optInt("code") == 200) {
                            callback.onGetTokenSuccess(jsonObject.optString("token"));
                        } else {
                            callback.onGetTokenFailed("code = " + jsonObject.optInt("code"));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onGetTokenFailed(e.getMessage());
                } finally {
                    if (null != is) {
                        try {
                            is.close();
                        } catch (IOException ignored) {
                        }
                    }

                    if (null != out) {
                        try {
                            out.close();
                        } catch (IOException ignored) {
                        }
                    }

                    if (null != message) {
                        try {
                            message.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        }).start();
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfByte = (b >>> 4) & 0x0F;
            int twoHalfs = 0;
            do {
                buf.append(halfByte <= 9 ? (char) ('0' + halfByte) : (char) ('a' + halfByte - 10));
                halfByte = b & 0x0F;
            } while (twoHalfs++ < 1);
        }
        return buf.toString();
    }

    private static String sha1(String text) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        md.update(text.getBytes(StandardCharsets.ISO_8859_1), 0, text.length());
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    public interface GetTokenCallback {

        void onGetTokenSuccess(String token);

        void onGetTokenFailed(String err);
    }
}
