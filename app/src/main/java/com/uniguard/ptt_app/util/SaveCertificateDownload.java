//package com.uniguard.ptt.util;
//
//import android.content.Context;
//import android.os.AsyncTask;
//import android.util.Log;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//
//import com.uniguard.ptt.Settings;
//import com.uniguard.ptt.db.DatabaseCertificate;
//import com.uniguard.ptt.db.MumlaDatabase;
//import com.uniguard.ptt.db.MumlaSQLiteDatabase;
//
//public class SaveCertificateDownload extends AsyncTask<Void, Void, Boolean> {
//    private Context context;
//    private String filePath;
//    private MumlaDatabase database;
//
//    public SaveCertificateDownload(Context context, String filePath) {
//        this.context = context;
//        this.filePath = filePath;
//        this.database = new MumlaSQLiteDatabase(context);
//    }
//
//    @Override
//    protected Boolean doInBackground(Void... voids) {
//        try {
//            database.open();
//            File file = new File(filePath);
//            Log.d("SaveCertificateTask", "File path: " + filePath);
//            if (!file.exists()) {
//                Log.e("SaveCertificateTask", "File does not exist");
//                return false;
//            }
//
//            InputStream inputStream = new FileInputStream(file);
//            byte[] certificateBytes = new byte[(int) file.length()];
//            inputStream.read(certificateBytes);
//            inputStream.close();
//
//            Log.d("SaveCertificateTask", "Certificate bytes length: " + certificateBytes.length);
//
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
//            String formattedDate = dateFormat.format(new Date());
//            String fileName = "PTT_UNIGUARD_" + formattedDate + ".p12";
//            DatabaseCertificate certificate = database.addCertificate(fileName, certificateBytes);
//            if (certificate != null) {
//                Settings settings = Settings.getInstance(context);
//                settings.setDefaultCertificateId(certificate.getId());
//                Log.d("SaveCertificateTask", "Certificate saved with ID: " + certificate.getId());
//                return true;
//            } else {
//                Log.e("SaveCertificateTask", "Failed to add certificate to database");
//            }
//        } catch (IOException e) {
//            Log.e("SaveCertificateTask", "Failed to save certificate", e);
//        } finally {
//            database.close();
//        }
//        return false;
//    }
//
//    @Override
//    protected void onPostExecute(Boolean result) {
//        if (result) {
//            Log.d("SaveCertificateTask", "Certificate saved and set as default");
//        } else {
//            Log.d("SaveCertificateTask", "Failed to save certificate");
//        }
//        database.close();
//    }
//}
//


package com.uniguard.ptt_app.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.uniguard.ptt_app.Settings;
import com.uniguard.ptt_app.db.DatabaseCertificate;
import com.uniguard.ptt_app.db.MumlaDatabase;
import com.uniguard.ptt_app.db.MumlaSQLiteDatabase;

public class SaveCertificateDownload {
    private Context context;
    private String filePath;
    private MumlaDatabase database;
    private ExecutorService executorService;

    public SaveCertificateDownload(Context context, String filePath) {
        this.context = context;
        this.filePath = filePath;
        this.database = new MumlaSQLiteDatabase(context);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void save() {
        Future<Boolean> future = executorService.submit(new SaveCertificateTask());
        executorService.shutdown();

        try {
            boolean result = future.get();
            onPostExecute(result);
        } catch (Exception e) {
            Log.e("SaveCertificateTask", "Error executing task", e);
        }
    }

    private class SaveCertificateTask implements Callable<Boolean> {
        @Override
        public Boolean call() {
            try {
                database.open();
                File file = new File(filePath);
                Log.d("SaveCertificateTask", "File path: " + filePath);
                if (!file.exists()) {
                    Log.e("SaveCertificateTask", "File does not exist");
                    return false;
                }

                InputStream inputStream = new FileInputStream(file);
                byte[] certificateBytes = new byte[(int) file.length()];
                inputStream.read(certificateBytes);
                inputStream.close();

                Log.d("SaveCertificateTask", "Certificate bytes length: " + certificateBytes.length);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
                String formattedDate = dateFormat.format(new Date());
                String fileName = "PTT_UNIGUARD_" + formattedDate + ".p12";
                DatabaseCertificate certificate = database.addCertificate(fileName, certificateBytes);
                if (certificate != null) {
                    Settings settings = Settings.getInstance(context);
                    settings.setDefaultCertificateId(certificate.getId());
                    Log.d("SaveCertificateTask", "Certificate saved with ID: " + certificate.getId());
                    return true;
                } else {
                    Log.e("SaveCertificateTask", "Failed to add certificate to database");
                }
            } catch (IOException e) {
                Log.e("SaveCertificateTask", "Failed to save certificate", e);
            } finally {
                database.close();
            }
            return false;
        }
    }

    private void onPostExecute(Boolean result) {
        if (result) {
            Log.d("SaveCertificateTask", "Certificate saved and set as default");
        } else {
            Log.d("SaveCertificateTask", "Failed to save certificate");
        }
        database.close();
    }
}
