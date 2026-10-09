package ru.runa.gpd.aichat.utils;

import com.openai.client.okhttp.OpenAIOkHttpClient;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class InsecureSslClient {
    private static TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }
    };

    private static HostnameVerifier unsafeHostnameVerifier() {
        return (hostname, session) -> true;
    }

    public static OpenAIOkHttpClient.Builder getClient(OpenAIOkHttpClient.Builder client) throws NoSuchAlgorithmException, KeyManagementException {
        X509TrustManager trustManager = (X509TrustManager) trustAllCerts[0];
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());

        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        return client.sslSocketFactory(sslSocketFactory)
                .hostnameVerifier(unsafeHostnameVerifier())
                .trustManager(trustManager);
    }
}
