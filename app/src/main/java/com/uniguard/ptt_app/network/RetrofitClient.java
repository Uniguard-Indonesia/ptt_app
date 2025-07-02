package com.uniguard.ptt_app.network;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.uniguard.ptt_app.BuildConfig;
import com.uniguard.ptt_app.Constants;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            if(BuildConfig.DEBUG){
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            }else{
                logging.setLevel(HttpLoggingInterceptor.Level.NONE);
            }

            // Header Interceptor
            Interceptor headerInterceptor = chain -> {
                Request originalRequest = chain.request();
                Request requestWithHeaders = originalRequest.newBuilder()
                        .header("Accept", "application/json")
                        .build();
                return chain.proceed(requestWithHeaders);
            };

            try{
                // Buat Certificate dari string
//                String certificate = "-----BEGIN CERTIFICATE-----\n" +
//                        "MIIDhjCCAwygAwIBAgISBLqWGhAhunvakmR3rNSubquPMAoGCCqGSM49BAMDMDIx\n" +
//                        "CzAJBgNVBAYTAlVTMRYwFAYDVQQKEw1MZXQncyBFbmNyeXB0MQswCQYDVQQDEwJF\n" +
//                        "NTAeFw0yNDA3MDkwMjA5MTFaFw0yNDEwMDcwMjA5MTBaMB0xGzAZBgNVBAMTEnB0\n" +
//                        "dC51bmlndWFyZC5jby5pZDBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABAH2zXdS\n" +
//                        "spKnmN7ALsKFAgjU6sBv9MA/OAjJSXtogQG8nctBYDEIWqygM2r5l4t89/fBB4GP\n" +
//                        "TkMepZb79E9iJpWjggIVMIICETAOBgNVHQ8BAf8EBAMCB4AwHQYDVR0lBBYwFAYI\n" +
//                        "KwYBBQUHAwEGCCsGAQUFBwMCMAwGA1UdEwEB/wQCMAAwHQYDVR0OBBYEFLoOCWau\n" +
//                        "hPpyJIur6D9s6X4d0qeTMB8GA1UdIwQYMBaAFJ8rX888IU+dBLftKyzExnCL0tcN\n" +
//                        "MFUGCCsGAQUFBwEBBEkwRzAhBggrBgEFBQcwAYYVaHR0cDovL2U1Lm8ubGVuY3Iu\n" +
//                        "b3JnMCIGCCsGAQUFBzAChhZodHRwOi8vZTUuaS5sZW5jci5vcmcvMB0GA1UdEQQW\n" +
//                        "MBSCEnB0dC51bmlndWFyZC5jby5pZDATBgNVHSAEDDAKMAgGBmeBDAECATCCAQUG\n" +
//                        "CisGAQQB1nkCBAIEgfYEgfMA8QB2AEiw42vapkc0D+VqAvqdMOscUgHLVt0sgdm7\n" +
//                        "v6s52IRzAAABkJV3c2MAAAQDAEcwRQIhAKyPuDqoQhR4gKQaJwAAVvo4SwyfXraF\n" +
//                        "zUyyJGLGHSasAiAp99/h4RoPV+1dp9DJRqY38nliOMAhZ0l7T/eT82ZIoAB3AHb/\n" +
//                        "iD8KtvuVUcJhzPWHujS0pM27KdxoQgqf5mdMWjp0AAABkJV3c5cAAAQDAEgwRgIh\n" +
//                        "AIxCFnxGfxlzXZceZxB6oL672Trh9r5bFJ6n9J9MsByUAiEA5NjAOttdIxF/OcFo\n" +
//                        "Eid1uNoJJqOhjvgZ47LVDApdExUwCgYIKoZIzj0EAwMDaAAwZQIxAPq2SvCfJxa0\n" +
//                        "3OXjzuiJ4rtyKgHGhkWUhw7ya+FpkIheFsZhD/z+D9BMOkx4iQoz0gIwe49jNelR\n" +
//                        "tPkWmxoSbrpU5ZHfU8TiAn1N9QHk6dRqijc6ixwSDyZOFdxzr25b4u/2\n" +
//                        "-----END CERTIFICATE-----";
//
//                // Buat Certificate dari string
//                CertificateFactory cf = CertificateFactory.getInstance("X.509");
//                ByteArrayInputStream certInputStream = new ByteArrayInputStream(certificate.getBytes());
//                Certificate ca = cf.generateCertificate(certInputStream);
//
//                // Membuat KeyStore dan menambahkan Certificate
//                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
//                keyStore.load(null, null);
//                keyStore.setCertificateEntry("ca", ca);
//
//                // Membuat TrustManager yang menggunakan KeyStore
//                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//                tmf.init(keyStore);
//
//                // Membuat SSLContext dengan TrustManager
//                SSLContext sslContext = SSLContext.getInstance("TLS");
//                sslContext.init(null, tmf.getTrustManagers(), null);
//
//                // Create TrustManager from TrustManagerFactory
//                TrustManager[] trustManagers = tmf.getTrustManagers();
//                if (trustManagers.length == 0) {
//                    throw new RuntimeException("No TrustManagers found");
//                }
//
//                // Explicitly specify the TrustManager
//                X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                                // No need to implement
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                                // No need to implement
                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[]{};
                            }
                        }
                };

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());


                OkHttpClient client = new OkHttpClient.Builder()
//                        .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                        .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                        .hostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String hostname, SSLSession session) {
                                return true; // Trust all hostnames
                            }
                        })
                        .addInterceptor(logging)
                        .addInterceptor(headerInterceptor)
                        .build();

                retrofit = new Retrofit.Builder()
                        .baseUrl(Constants.API_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                         .client(client)
                        .build();

            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                e.printStackTrace();
            }
//            catch (KeyStoreException e) {
//                e.printStackTrace();
//            } catch (CertificateException e) {
//                throw new RuntimeException(e);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
        }
        return retrofit;
    }

}
