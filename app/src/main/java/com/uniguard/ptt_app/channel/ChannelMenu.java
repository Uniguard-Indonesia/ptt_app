/*
 * Copyright (C) 2015 Andrew Comminos <andrew@comminos.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.uniguard.ptt_app.channel;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.uniguard.humla.IHumlaService;
import com.uniguard.humla.IHumlaSession;
import com.uniguard.humla.model.IChannel;
import com.uniguard.humla.model.Server;
import com.uniguard.humla.model.WhisperTargetChannel;
import com.uniguard.humla.net.Permissions;
import com.uniguard.humla.util.VoiceTargetMode;
import com.uniguard.ptt_app.R;
import com.uniguard.ptt_app.channel.comment.ChannelDescriptionFragment;
import com.uniguard.ptt_app.db.MumlaDatabase;

/**
 * Created by andrew on 22/11/15.
 */
public class ChannelMenu implements PermissionsPopupMenu.IOnMenuPrepareListener, PopupMenu.OnMenuItemClickListener {
    private static final String TAG = ChannelMenu.class.getName();

    private final Context mContext;
    private final IChannel mChannel;
    private final IHumlaService mService;
    private final MumlaDatabase mDatabase;
    private final FragmentManager mFragmentManager;

    public ChannelMenu(Context context, IChannel channel, IHumlaService service,
            MumlaDatabase database, FragmentManager fragmentManager) {
        mContext = context;
        mChannel = channel;
        mService = service;
        mDatabase = database;
        mFragmentManager = fragmentManager;
    }

    @Override
    public void onMenuPrepare(Menu menu, int permissions) {
        // TODO This breaks uMurmur ACL. Put in a fix based on server version perhaps?
        // menu.getMenu().findItem(R.id.menu_channel_add)
        // .setVisible((permissions & (Permissions.MakeChannel |
        // Permissions.MakeTempChannel)) > 0);
        menu.findItem(R.id.context_channel_edit).setVisible((permissions & Permissions.Write) > 0);
        menu.findItem(R.id.context_channel_remove).setVisible((permissions & Permissions.Write) > 0);
        menu.findItem(R.id.context_channel_view_description)
                .setVisible(mChannel.getDescription() != null ||
                        mChannel.getDescriptionHash() != null);
        Server server = mService.getTargetServer();
        if (server != null) {
            menu.findItem(R.id.context_channel_pin)
                    .setChecked(mDatabase.isChannelPinned(server.getId(), mChannel.getId()));
        }
        if (mService.isConnected()) {
            IChannel ourChan = null;
            try {
                ourChan = mService.HumlaSession().getSessionChannel();
            } catch (IllegalStateException e) {
                Log.d(TAG, "exception in onMenuPrepare: " + e);
            }
            if (ourChan != null) {
                menu.findItem(R.id.context_channel_link).setChecked(mChannel.getLinks().contains(ourChan));
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (!mService.isConnected())
            return false;

        int itemId = item.getItemId();
        if (itemId == R.id.context_channel_join) {
            mService.HumlaSession().joinChannel(mChannel.getId());
        } else if (itemId == R.id.context_channel_add || itemId == R.id.context_channel_edit) {
            Bundle args = new Bundle();
            if (itemId == R.id.context_channel_add) {
                args.putInt("parent", mChannel.getId());
                args.putBoolean("adding", true);
            } else {
                args.putInt("channel", mChannel.getId());
                args.putBoolean("adding", false);
            }
            ChannelEditFragment addFragment = new ChannelEditFragment();
            addFragment.setArguments(args);
            addFragment.show(mFragmentManager, "ChannelAdd");
        } else if (itemId == R.id.context_channel_remove) {
            AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
            adb.setTitle(R.string.confirm);
            adb.setMessage(R.string.confirm_delete_channel);
            adb.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mService.isConnected()) {
                        mService.HumlaSession().removeChannel(mChannel.getId());
                    }
                }
            });
            adb.setNegativeButton(android.R.string.cancel, null);
            adb.show();
        } else if (itemId == R.id.context_channel_view_description) {
            Bundle commentArgs = new Bundle();
            commentArgs.putInt("channel", mChannel.getId());
            commentArgs.putString("comment", mChannel.getDescription());
            commentArgs.putBoolean("editing", false);
            DialogFragment commentFragment = (DialogFragment) Fragment.instantiate(mContext,
                    ChannelDescriptionFragment.class.getName(), commentArgs);
            commentFragment.show(mFragmentManager, ChannelDescriptionFragment.class.getName());
        } else if (itemId == R.id.context_channel_pin) {
            long serverId = mService.getTargetServer().getId();
            boolean pinned = mDatabase.isChannelPinned(serverId, mChannel.getId());
            if (!pinned)
                mDatabase.addPinnedChannel(serverId, mChannel.getId());
            else
                mDatabase.removePinnedChannel(serverId, mChannel.getId());
        } else if (itemId == R.id.context_channel_link) {
            IChannel channel = mService.HumlaSession().getSessionChannel();
            if (!item.isChecked()) {
                mService.HumlaSession().linkChannels(channel, mChannel);
            } else {
                mService.HumlaSession().unlinkChannels(channel, mChannel);
            }
        } else if (itemId == R.id.context_channel_unlink_all) {
            mService.HumlaSession().unlinkAllChannels(mChannel);
        } else if (itemId == R.id.context_channel_shout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.shout_configure);
            LinearLayout layout = new LinearLayout(mContext);
            layout.setOrientation(LinearLayout.VERTICAL);

            final CheckBox subchannelBox = new CheckBox(mContext);
            subchannelBox.setText(R.string.shout_include_subchannels);
            layout.addView(subchannelBox);

            final CheckBox linkedBox = new CheckBox(mContext);
            linkedBox.setText(R.string.shout_include_linked);
            layout.addView(linkedBox);

            builder.setView(layout);
            builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!mService.isConnected())
                        return;

                    IHumlaSession session = mService.HumlaSession();

                    // Unregister any existing voice target.
                    if (session.getVoiceTargetMode() == VoiceTargetMode.WHISPER) {
                        session.unregisterWhisperTarget(session.getVoiceTargetId());
                    }

                    WhisperTargetChannel channelTarget = new WhisperTargetChannel(mChannel,
                            linkedBox.isChecked(), subchannelBox.isChecked(), null);
                    byte id = session.registerWhisperTarget(channelTarget);
                    if (id > 0) {
                        session.setVoiceTargetId(id);
                    } else {
                        Toast.makeText(mContext, R.string.shout_failed, Toast.LENGTH_LONG).show();
                    }
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        } else {
            return false;
        }
        return true;
    }

    public void showPopup(View anchor) {
        PermissionsPopupMenu popupMenu = new PermissionsPopupMenu(mContext, anchor,
                R.menu.context_channel, this, this, mChannel, mService);
        popupMenu.show();
    }
}
