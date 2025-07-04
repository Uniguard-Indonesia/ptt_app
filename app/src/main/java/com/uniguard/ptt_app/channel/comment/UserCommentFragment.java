/*
 * Copyright (C) 2014 Andrew Comminos
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

package com.uniguard.ptt_app.channel.comment;

import com.uniguard.humla.IHumlaService;
import com.uniguard.humla.model.IUser;
import com.uniguard.humla.util.HumlaObserver;

/**
 * Created by andrew on 03/03/14.
 */
public class UserCommentFragment extends AbstractCommentFragment {

    @Override
    public void requestComment(final IHumlaService service) {
        if (!service.isConnected())
            return;
        service.registerObserver(new HumlaObserver() {
            @Override
            public void onUserStateUpdated(IUser user) {
                if (user.getSession() == getSession() &&
                        user.getComment() != null) {
                    loadComment(user.getComment());
                    service.unregisterObserver(this);
                }
            }
        });
        service.HumlaSession().requestComment(getSession());
    }

    @Override
    public void editComment(IHumlaService service, String comment) {
        if (!service.isConnected())
            return;
        service.HumlaSession().setUserComment(getSession(), comment);
    }

    public int getSession() {
        return getArguments().getInt("session");
    }
}
