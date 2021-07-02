package com.springboot.common;


/**
 * Created by yzn00 on 2021/6/28.
 */


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class BusInfoAccessToken {

    private static final String KEY_MAC_SHA1 = "HmacSHA1";

    private static final String CHARSET_UTF8 = "UTF-8";


    public static void main(String[] args) throws Exception {
        String appSecret = "757929A0F66E4AADB9C187F20C937899";
        String appId = "yuelai_zhibohui";
        String nonce = "15";

        Long startTs = System.currentTimeMillis();
        System.out.println(startTs);

        String param = nonce + startTs + appSecret;
        System.out.println(param);
        System.out.println(hmacSha1Encrypt(param, appSecret));


    }

    //二行制转字符串
    public static String byte2hex(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b != null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1)
                hs.append('0');
            hs.append(stmp);
        }
        return hs.toString().toLowerCase();
    }

    /*
    使用 HmacSha1 加密
 */
    public static String hmacSha1Encrypt(String encryptText, String encryptKey) {
        try {
            byte[] text = encryptText.getBytes(CHARSET_UTF8);
            byte[] keyData = encryptKey.getBytes(CHARSET_UTF8);

            SecretKeySpec secretKey = new SecretKeySpec(keyData, KEY_MAC_SHA1);
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            return byte2hex(mac.doFinal(text));
        } catch (Exception e) {
            System.err.println("字符串:" + encryptText + "使用" + KEY_MAC_SHA1 + "加密失败");
        }
        return null;
    }
}
