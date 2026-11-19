package com.novaos.mobile;

import android.os.Build;

public final class DeviceInfo {
    private DeviceInfo() {}

    public static String summary() {
        return "Manufacturer: " + Build.MANUFACTURER
                + "\nModel: " + Build.MODEL
                + "\nDevice: " + Build.DEVICE
                + "\nAndroid: " + Build.VERSION.RELEASE
                + "\nAPI level: " + Build.VERSION.SDK_INT
                + "\nHardware: " + Build.HARDWARE;
    }
}
