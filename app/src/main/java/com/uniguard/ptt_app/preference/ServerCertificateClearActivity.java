/*
 * Copyright (C) 2016 Andrew Comminos <andrew@comminos.com>
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

package com.uniguard.ptt_app.preference;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.uniguard.ptt_app.R;
import com.uniguard.ptt_app.util.MumlaTrustStore;

/**
 * Created by andrew on 13/01/16.
 */
public class ServerCertificateClearActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MumlaTrustStore.clearTrustStore(this);
        Toast.makeText(this, R.string.trust_cleared, Toast.LENGTH_LONG).show();
        finish();
    }
}
