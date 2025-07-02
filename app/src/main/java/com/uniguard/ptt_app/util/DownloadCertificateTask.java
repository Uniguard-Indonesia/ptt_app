
package com.uniguard.ptt_app.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.uniguard.ptt_app.R;

public class DownloadCertificateTask {
    private Context context;
    private String fileName;
    private DownloadListener listener;
    private ExecutorService executorService;

    public interface DownloadListener {
        void onDownloadComplete(String filePath);
        void onDownloadFailed();
    }

    public DownloadCertificateTask(Context context, String fileName, DownloadListener listener) {
        this.context = context;
        this.fileName = fileName;
        this.listener = listener;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void execute(String url) {
        Future<String> future = executorService.submit(new DownloadTask(url));
        executorService.shutdown();

        try {
            String filePath = future.get();
            onPostExecute(filePath);
        } catch (Exception e) {
            Log.e("DownloadTask", "Error executing task", e);
            listener.onDownloadFailed();
        }
    }

    private class DownloadTask implements Callable<String> {
        private String url;

        public DownloadTask(String url) {
            this.url = url;
        }

        @Override
        public String call() {
            String filePath = null;
            try {
//                setupSSLSocketFactory();
                disableSSLCertificateChecking();
                URL url = new URL(this.url);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                InputStream input = connection.getInputStream();
                filePath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + fileName;
                FileOutputStream output = new FileOutputStream(filePath);

                byte[] data = new byte[4096];
                int count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }

                output.close();
                input.close();
            } catch (Exception e) {
                Log.e("DownloadTask", "Download error: " + e.getMessage());
            }
            return filePath;
        }
    }

    private void onPostExecute(String filePath) {
        if (filePath != null) {
            listener.onDownloadComplete(filePath);
        } else {
            listener.onDownloadFailed();
        }
    }

    public void setupSSLSocketFactory() {
        InputStream openRawResource;
        try {
            // Load the server certificate
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = context.getResources().openRawResource(R.raw.ptt_cert); // Replace with your certificate resource
            X509Certificate ca;
            try {
                ca = (X509Certificate) cf.generateCertificate(caInput);
            } finally {
                caInput.close();
            }
            // Create a KeyStore containing our trusted CAs
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

            // Set the default SSLSocketFactory
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (Exception e) {
            Log.e("SSLSetup", "SSL setup error: " + e.getMessage());
        }
    }

    private static void disableSSLCertificateChecking() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }
        } };

        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

}
