package org.deviceconnect.android.libsrt;

class NdkHelper {

    static {
        System.loadLibrary("srt");
        System.loadLibrary("srt-native-interface");
    }

    private NdkHelper() {
    }

    public static native void startup();
    public static native long cleanup();

    public static native long createSrtSocket(String address, int port);
    public static native void closeSrtSocket(long nativePtr);

    public static native long accept(long nativePtr, String address, int port);
    public static native int sendMessage(long nativePtr, byte[] data, int length);
}
