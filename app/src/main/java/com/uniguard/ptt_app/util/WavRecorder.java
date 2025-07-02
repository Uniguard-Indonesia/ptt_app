//package com.uniguard.ptt.util;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.media.AudioFormat;
//import android.media.AudioRecord;
//import android.media.MediaRecorder;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
//import com.uniguard.ptt.data.models.response.ActivityResponse;
//import com.uniguard.ptt.repository.UserRepository;
//
//public class WavRecorder {
//    private final Context context;
//    private final UserRepository userRepository;
//    String filePath = null;
//    final int bpp = 16;
//    int sampleRate = 16000;
//    int channel = AudioFormat.CHANNEL_IN_MONO;
//    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
//    AudioRecord recorder = null;
//    int bufferSize = 0;
//    Thread recordingThread;
//    boolean isRecording = false;
//    FileOutputStream wavOutputStream;
//
//    public WavRecorder(Context context, UserRepository userRepository) {
//        this.context = context;
//        this.userRepository = userRepository;
//        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, audioEncoding);
//        bufferSize = Math.max(bufferSize, 4096);
//    }
//
//    private File getFile(String name) {
//        File dir = context.getExternalFilesDir(null); // Use external files directory
//        return new File(dir, name);
//    }
//
//    private void writeAudioDataToWav() {
//        try {
//            byte[] data = new byte[bufferSize];
//            if (wavOutputStream != null) {
//                while (isRecording) {
//                    int read = recorder.read(data, 0, bufferSize);
//                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
//                        wavOutputStream.write(data, 0, read);
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void wavHeader(FileOutputStream fileOutputStream, long totalAudioLen, long totalDataLen, int channels, long byteRate) {
//        try {
//            byte[] header = new byte[44];
//            // RIFF/WAVE header
//            header[0] = 'R';
//            header[1] = 'I';
//            header[2] = 'F';
//            header[3] = 'F';
//            header[4] = (byte) (totalDataLen & 0xff);
//            header[5] = (byte) ((totalDataLen >> 8) & 0xff);
//            header[6] = (byte) ((totalDataLen >> 16) & 0xff);
//            header[7] = (byte) ((totalDataLen >> 24) & 0xff);
//            header[8] = 'W';
//            header[9] = 'A';
//            header[10] = 'V';
//            header[11] = 'E';
//            header[12] = 'f';
//            header[13] = 'm';
//            header[14] = 't';
//            header[15] = ' ';
//            header[16] = 16;
//            header[17] = 0;
//            header[18] = 0;
//            header[19] = 0;
//            header[20] = 1;
//            header[21] = 0;
//            header[22] = (byte) channels;
//            header[23] = 0;
//            header[24] = (byte) (sampleRate & 0xff);
//            header[25] = (byte) ((sampleRate >> 8) & 0xff);
//            header[26] = (byte) ((sampleRate >> 16) & 0xff);
//            header[27] = (byte) ((sampleRate >> 24) & 0xff);
//            header[28] = (byte) (byteRate & 0xff);
//            header[29] = (byte) ((byteRate >> 8) & 0xff);
//            header[30] = (byte) ((byteRate >> 16) & 0xff);
//            header[31] = (byte) ((byteRate >> 24) & 0xff);
//            header[32] = (byte) (2 * bpp / 8);
//            header[33] = 0;
//            header[34] = bpp;
//            header[35] = 0;
//            header[36] = 'd';
//            header[37] = 'a';
//            header[38] = 't';
//            header[39] = 'a';
//            header[40] = (byte) (totalAudioLen & 0xff);
//            header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
//            header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
//            header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
//            fileOutputStream.write(header, 0, 44);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    public void startRecording(String filename) {
//        try {
//            File file = getFile(filename + ".wav");
//            wavOutputStream = new FileOutputStream(file);
//
//            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channel, audioEncoding, bufferSize);
//            if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
//                recorder.startRecording();
//                isRecording = true;
//
//                // Write WAV header initially with placeholders
//                wavHeader(wavOutputStream, 0, 0, 2, sampleRate * 2 * bpp / 8);
//
//                recordingThread = new Thread(this::writeAudioDataToWav);
//                recordingThread.start();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void stopRecording(String filename, String token, String activityName) {
//        try {
//            if (recorder != null) {
//                isRecording = false;
//                if (recorder.getState() == AudioRecord.RECORDSTATE_RECORDING) {
//                    recorder.stop();
//                }
//                recorder.release();
//                recordingThread = null;
//
//                File file = getFile(filename + ".wav");
//                if (file.exists()) {
//                    long totalAudioLen = wavOutputStream.getChannel().size() - 44;  // Subtract the header size
//                    long totalDataLen = totalAudioLen + 36;
//                    long byteRate = bpp * sampleRate * 2 / 8;
//
//                    // Seek to the beginning of the file and rewrite the header
//                    wavOutputStream.getChannel().position(0);
//                    wavHeader(wavOutputStream, totalAudioLen, totalDataLen, 2, byteRate);
//
//                    wavOutputStream.close();
//
//                    // Post activity after stopping recording
//                    userRepository.postActivity(token, activityName, file, new UserRepository.ActivityCallback() {
//                        @Override
//                        public void onSuccess(ActivityResponse response) {
//                            // Handle success
//                            System.out.println("Voice record posted successfully.");
//
//                            if (file.exists()) {
//                                if (file.delete()) {
//                                    System.out.println("Voice record file deleted successfully.");
//                                } else {
//                                    System.out.println("Failed to delete voice record file.");
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onError(Throwable t) {
//                            // Handle error
//                            System.out.println("Failed to post activity: " + t.getMessage());
//                        }
//                    });
//                } else {
//                    System.out.println("File not found: " + file.getAbsolutePath());
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
//
package com.uniguard.ptt_app.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.uniguard.ptt_app.data.models.response.ActivityResponse;
import com.uniguard.ptt_app.repository.UserRepository;

public class WavRecorder implements Runnable {
    private final Context context;
    private final UserRepository userRepository;

    private final int bpp = 16;
    private final int sampleRate = 16000;
    private final int channel = AudioFormat.CHANNEL_IN_STEREO;
    private final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private Thread recordingThread;
    private boolean isRecording = false;
    private OutputStream wavOutputStream;
    private Uri fileUri;

    private final Object bufferLock = new Object();

    public WavRecorder(Context context, UserRepository userRepository) {
        this.context = context;
        this.userRepository = userRepository;
        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, audioEncoding);
    }

    private Uri createAudioFile(String name) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, name + ".wav");
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav");
        values.put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/Recordings");
        } else {
            File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            File recordingsDir = new File(musicDir, "Recordings");
            if (!recordingsDir.exists() && !recordingsDir.mkdirs()) {
                Log.e("WavRecorder", "Failed to create Recordings directory");
                return null;
            }
            File audioFile = new File(recordingsDir, name + ".wav");
            values.put(MediaStore.Audio.Media.DATA, audioFile.getAbsolutePath());
        }

        ContentResolver contentResolver = context.getContentResolver();
        return contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void writeAudioDataToWav() {
        try {
            byte[] data = new byte[bufferSize];

            // Set thread priority for audio recording
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            if (wavOutputStream != null && recorder != null) {
                while (isRecording) {
                    int read;
                    synchronized (bufferLock) {
                        read = recorder.read(data, 0, bufferSize);
                    }

                    if (read == AudioRecord.ERROR_INVALID_OPERATION) {
                        Log.e("WavRecorder", "Invalid operation during read");
                    } else if (read > 0) {
                        synchronized (bufferLock) {
                            wavOutputStream.write(data, 0, read);
                        }
                    } else {
                        Log.e("WavRecorder", "Unexpected read error: " + read);
                    }
                }
            } else {
                Log.e("WavRecorder", "wavOutputStream or recorder is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void wavHeader(OutputStream outputStream, long totalAudioLen, long totalDataLen, int channels, long byteRate) {
        try {
            byte[] header = new byte[44];
            header[0] = 'R';  // RIFF/WAVE header
            header[1] = 'I';
            header[2] = 'F';
            header[3] = 'F';
            header[4] = (byte) (totalDataLen & 0xff);
            header[5] = (byte) ((totalDataLen >> 8) & 0xff);
            header[6] = (byte) ((totalDataLen >> 16) & 0xff);
            header[7] = (byte) ((totalDataLen >> 24) & 0xff);
            header[8] = 'W';
            header[9] = 'A';
            header[10] = 'V';
            header[11] = 'E';
            header[12] = 'f';  // 'fmt ' chunk
            header[13] = 'm';
            header[14] = 't';
            header[15] = ' ';
            header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
            header[17] = 0;
            header[18] = 0;
            header[19] = 0;
            header[20] = 1;  // format = 1
            header[21] = 0;
            header[22] = (byte) channels;
            header[23] = 0;
            header[24] = (byte) (sampleRate & 0xff);
            header[25] = (byte) ((sampleRate >> 8) & 0xff);
            header[26] = (byte) ((sampleRate >> 16) & 0xff);
            header[27] = (byte) ((sampleRate >> 24) & 0xff);
            header[28] = (byte) (byteRate & 0xff);
            header[29] = (byte) ((byteRate >> 8) & 0xff);
            header[30] = (byte) ((byteRate >> 16) & 0xff);
            header[31] = (byte) ((byteRate >> 24) & 0xff);
            header[32] = (byte) (2 * 16 / 8);  // block align
            header[33] = 0;
            header[34] = bpp;  // bits per sample
            header[35] = 0;
            header[36] = 'd';
            header[37] = 'a';
            header[38] = 't';
            header[39] = 'a';
            header[40] = (byte) (totalAudioLen & 0xff);
            header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
            header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
            header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
            outputStream.write(header, 0, 44);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    public void startRecording(String filename) {
        Log.d(WavRecorder.class.getName(), "startRecording: RECORD: START");
        try {
            if (filename == null || filename.isEmpty()) {
                throw new IllegalArgumentException("Filename cannot be null or empty");
            }
            fileUri = createAudioFile(filename);
            if (fileUri == null) {
                Log.e("WavRecorder", "Failed to create file URI");
                return;
            }

            wavOutputStream = context.getContentResolver().openOutputStream(fileUri);
            if (wavOutputStream == null) {
                Log.e("WavRecorder", "Failed to open OutputStream for fileUri: " + fileUri.toString());
                return;
            }

            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channel, audioEncoding, bufferSize);
            Log.d("WavRecorder", "startRecording: " + recorder.getState());
            if (recorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
                Log.e("WavRecorder", "AudioRecord initialization failed");
                return;
            } else {
                recorder.startRecording();
                isRecording = true;

                // Write WAV header initially with placeholders
                wavHeader(wavOutputStream, 0, 0, 2, sampleRate * 2 * bpp / 8);

                recordingThread = new Thread(this); // Using this class as the Runnable
                recordingThread.start();
            }
        } catch (IOException e) {
            Log.e("WavRecorder", "IOException occurred: " + e.getMessage(), e);
        }
    }


    public void stopRecording(String filename, String token, String activityName) {
        Log.d(WavRecorder.class.getName(), "stopRecording: RECORD: STOP");
        try {
            if (recorder != null) {
                isRecording = false;
                if (recorder.getState() == AudioRecord.RECORDSTATE_RECORDING) {
                    recorder.stop();
                }
                recorder.release();
                recorder = null;
                recordingThread = null;

                if (wavOutputStream != null) {
                    long totalAudioLen = wavOutputStream instanceof FileOutputStream ?
                            ((FileOutputStream) wavOutputStream).getChannel().size() - 44 : 0;
                    long totalDataLen = totalAudioLen + 36;
                    long byteRate = bpp * sampleRate * 2 / 8;

                    if (wavOutputStream instanceof FileOutputStream) {
                        ((FileOutputStream) wavOutputStream).getChannel().position(0);
                        wavHeader(wavOutputStream, totalAudioLen, totalDataLen, 2, byteRate);
                    }

                    wavOutputStream.close();

                    File file = new File(getRealPathFromURI(fileUri));
                    if (file.exists()) {
                        userRepository.postActivity(token, activityName, file, new UserRepository.ActivityCallback() {
                            @Override
                            public void onSuccess(ActivityResponse response) {
                                System.out.println("Voice record posted successfully.");
                                if (file.delete()) {
                                    System.out.println("Voice record file deleted successfully.");
                                } else {
                                    System.out.println("Failed to delete voice record file.");
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                System.out.println("Failed to post activity: " + t.getMessage());
                            }
                        });
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getRealPathFromURI(Uri uri) {
        String filePath = null;
        String[] projection = {MediaStore.Audio.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            cursor.moveToFirst();
            filePath = cursor.getString(columnIndex);
            cursor.close();
        }
        return filePath;
    }

    @Override
    public void run() {
        if (isRecording) {
            writeAudioDataToWav();
        }
    }
}

