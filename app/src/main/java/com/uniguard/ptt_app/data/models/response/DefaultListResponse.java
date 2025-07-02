package com.uniguard.ptt_app.data.models.response;

import android.os.Parcelable;
import java.util.List;


public class DefaultListResponse<T extends Parcelable> {
    private boolean status;
    private String message;
    private List<T> data;

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<T> getData() {
        return data;
    }
}

