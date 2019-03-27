/*
 *  Copyright (C) 2019  The Diol App Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.diol.incallui.audioroute;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.os.BuildCompat;
import android.telecom.CallAudioState;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import app.diol.R;
import app.diol.dialer.common.FragmentUtils;
import app.diol.dialer.common.LogUtil;
import app.diol.dialer.logging.DialerImpression;
import app.diol.dialer.logging.Logger;
import app.diol.dialer.theme.base.ThemeComponent;
import app.diol.incallui.call.CallList;
import app.diol.incallui.call.DialerCall;
import app.diol.incallui.call.TelecomAdapter;

/**
 * Shows picker for audio routes
 */
public class AudioRouteSelectorDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = "AudioRouteSelectorDialogFragment";
    private static final String ARG_AUDIO_STATE = "audio_state";

    public static AudioRouteSelectorDialogFragment newInstance(CallAudioState audioState) {
        AudioRouteSelectorDialogFragment fragment = new AudioRouteSelectorDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_AUDIO_STATE, audioState);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragmentUtils.checkParent(this, AudioRouteSelectorPresenter.class);
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        LogUtil.i("AudioRouteSelectorDialogFragment.onCreateDialog", null);
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        if (Settings.canDrawOverlays(getContext())) {
            dialog
                    .getWindow()
                    .setType(
                            BuildCompat.isAtLeastO()
                                    ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                                    : WindowManager.LayoutParams.TYPE_PHONE);
        }
        return dialog;
    }

    @Nullable
    @Override
    @SuppressLint("NewApi")
    public View onCreateView(
            LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.audioroute_selector, viewGroup, false);
        CallAudioState audioState = getArguments().getParcelable(ARG_AUDIO_STATE);

        if (BuildCompat.isAtLeastP()) {
            // Create items for all connected Bluetooth devices
            Collection<BluetoothDevice> bluetoothDeviceSet = audioState.getSupportedBluetoothDevices();
            for (BluetoothDevice device : bluetoothDeviceSet) {
                boolean selected =
                        (audioState.getRoute() == CallAudioState.ROUTE_BLUETOOTH)
                                && (bluetoothDeviceSet.size() == 1
                                || device.equals(audioState.getActiveBluetoothDevice()));
                TextView textView = createBluetoothItem(device, selected);
                ((LinearLayout) view).addView(textView, 0);
            }
        } else {
            // Only create Bluetooth audio route
            TextView textView =
                    (TextView) getLayoutInflater().inflate(R.layout.audioroute_item, null, false);
            textView.setText(getString(R.string.audioroute_bluetooth));
            initItem(
                    textView,
                    CallAudioState.ROUTE_BLUETOOTH,
                    audioState,
                    DialerImpression.Type.IN_CALL_SWITCH_AUDIO_ROUTE_BLUETOOTH);
            ((LinearLayout) view).addView(textView, 0);
        }

        initItem(
                (TextView) view.findViewById(R.id.audioroute_speaker),
                CallAudioState.ROUTE_SPEAKER,
                audioState,
                DialerImpression.Type.IN_CALL_SWITCH_AUDIO_ROUTE_SPEAKER);
        initItem(
                (TextView) view.findViewById(R.id.audioroute_headset),
                CallAudioState.ROUTE_WIRED_HEADSET,
                audioState,
                DialerImpression.Type.IN_CALL_SWITCH_AUDIO_ROUTE_WIRED_HEADSET);
        initItem(
                (TextView) view.findViewById(R.id.audioroute_earpiece),
                CallAudioState.ROUTE_EARPIECE,
                audioState,
                DialerImpression.Type.IN_CALL_SWITCH_AUDIO_ROUTE_EARPIECE);

        // TODO(a bug): set peak height correctly to fully expand it in landscape mode.
        return view;
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
        FragmentUtils.getParentUnsafe(
                AudioRouteSelectorDialogFragment.this, AudioRouteSelectorPresenter.class)
                .onAudioRouteSelectorDismiss();
    }

    private void initItem(
            TextView item,
            final int itemRoute,
            CallAudioState audioState,
            DialerImpression.Type impressionType) {
        int selectedColor = ThemeComponent.get(getContext()).theme().getColorPrimary();
        if ((audioState.getSupportedRouteMask() & itemRoute) == 0) {
            item.setVisibility(View.GONE);
        } else if (audioState.getRoute() == itemRoute) {
            item.setSelected(true);
            item.setTextColor(selectedColor);
            item.setCompoundDrawableTintList(ColorStateList.valueOf(selectedColor));
            item.setCompoundDrawableTintMode(Mode.SRC_ATOP);
        }
        item.setOnClickListener(
                (v) -> {
                    logCallAudioRouteImpression(impressionType);
                    FragmentUtils.getParentUnsafe(
                            AudioRouteSelectorDialogFragment.this, AudioRouteSelectorPresenter.class)
                            .onAudioRouteSelected(itemRoute);
                    dismiss();
                });
    }

    private TextView createBluetoothItem(BluetoothDevice bluetoothDevice, boolean selected) {
        int selectedColor = ThemeComponent.get(getContext()).theme().getColorPrimary();
        TextView textView =
                (TextView) getLayoutInflater().inflate(R.layout.audioroute_item, null, false);
        textView.setText(getAliasName(bluetoothDevice));
        if (selected) {
            textView.setSelected(true);
            textView.setTextColor(selectedColor);
            textView.setCompoundDrawableTintList(ColorStateList.valueOf(selectedColor));
            textView.setCompoundDrawableTintMode(Mode.SRC_ATOP);
        }
        textView.setOnClickListener(
                (v) -> {
                    logCallAudioRouteImpression(DialerImpression.Type.IN_CALL_SWITCH_AUDIO_ROUTE_BLUETOOTH);
                    // Set Bluetooth audio route
                    FragmentUtils.getParentUnsafe(
                            AudioRouteSelectorDialogFragment.this, AudioRouteSelectorPresenter.class)
                            .onAudioRouteSelected(CallAudioState.ROUTE_BLUETOOTH);
                    // Set active Bluetooth device
                    TelecomAdapter.getInstance().requestBluetoothAudio(bluetoothDevice);
                    dismiss();
                });

        return textView;
    }

    @SuppressLint("PrivateApi")
    private String getAliasName(BluetoothDevice bluetoothDevice) {
        try {
            Method getActiveDeviceMethod = bluetoothDevice.getClass().getDeclaredMethod("getAliasName");
            getActiveDeviceMethod.setAccessible(true);
            return (String) getActiveDeviceMethod.invoke(bluetoothDevice);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return bluetoothDevice.getName();
        }
    }

    private void logCallAudioRouteImpression(DialerImpression.Type impressionType) {
        DialerCall dialerCall = CallList.getInstance().getOutgoingCall();
        if (dialerCall == null) {
            dialerCall = CallList.getInstance().getActiveOrBackgroundCall();
        }

        if (dialerCall != null) {
            Logger.get(getContext())
                    .logCallImpression(
                            impressionType, dialerCall.getUniqueCallId(), dialerCall.getTimeAddedMs());
        } else {
            Logger.get(getContext()).logImpression(impressionType);
        }
    }

    /**
     * Called when an audio route is picked
     */
    public interface AudioRouteSelectorPresenter {
        void onAudioRouteSelected(int audioRoute);

        void onAudioRouteSelectorDismiss();
    }
}
