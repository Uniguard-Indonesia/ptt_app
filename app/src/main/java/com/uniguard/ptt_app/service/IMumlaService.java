package com.uniguard.ptt_app.service;

import java.util.List;

import com.uniguard.humla.IHumlaService;
import com.uniguard.humla.protocol.AudioHandler;

/**
 * Created by andrew on 28/02/17.
 */
public interface IMumlaService extends IHumlaService {
    void setOverlayShown(boolean showOverlay);

    boolean isOverlayShown();

    void clearChatNotifications();

    void markErrorShown();

    boolean isErrorShown();

    void onTalkKeyDown();

    void onTalkKeyUp();

    List<IChatMessage> getMessageLog();

    void clearMessageLog();

    void setSuppressNotifications(boolean suppressNotifications);

    AudioHandler getAudioHandler();
}
