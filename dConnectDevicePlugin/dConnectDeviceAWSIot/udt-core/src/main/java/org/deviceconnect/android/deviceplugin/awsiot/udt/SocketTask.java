package org.deviceconnect.android.deviceplugin.awsiot.udt;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

class SocketTask {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "ABC";

    private static final byte[] HEADER = {
            0x01, 0x02, 0x03, 0x04
    };

    private static final int BUFFER_SIZE = 512;

    private boolean mCloseFlag = false;

    private Socket mSocket;

    private final byte[] mRecvBuffer = new byte[4];
    private final byte[] mSendBuffer = new byte[4];

    private OnSocketTaskListener mListener;

    public SocketTask(final Socket socket) {
        if (socket == null) {
            throw new NullPointerException("socket is null.");
        }
        mSocket = socket;
    }

    public void setOnSocketTaskListener(final OnSocketTaskListener listener) {
        mListener = listener;
    }

    public void close() throws IOException {
        mCloseFlag = true;
        mSocket.close();
    }

    public void sendData(final byte[] data) throws IOException {
        intToByte(data.length, mSendBuffer, 0);
        mSocket.getOutputStream().write(HEADER);
        mSocket.getOutputStream().write(mSendBuffer);
        int offset = 0;
        int length;
        while (offset < data.length) {
            length = data.length - offset;
            if (length > BUFFER_SIZE) {
                length = BUFFER_SIZE;
            }
            mSocket.getOutputStream().write(data, offset, length);
            offset += length;
        }
    }

    public boolean execute() {
        final byte[] data = new byte[BUFFER_SIZE];
        final String address = getRemoteAddress();
        final int port = getRemotePort();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            if (mListener != null) {
                mListener.onConnected(address, port);
            }

            int len;
            final InputStream is = mSocket.getInputStream();
            while (!mCloseFlag) {
                // TODO データの読み込みがずれた時にリカバリー処理
                readHeader(is);
                int size = readSize(is);

                out.reset();
                while (size > 0) {
                    if (size < BUFFER_SIZE) {
                        len = is.read(data, 0, size);
                    } else {
                        len = is.read(data, 0, BUFFER_SIZE);
                    }
                    if (len < 0) {
                        continue;
                    }

                    out.write(data, 0, len);
                    size -= len;
                }

                // TODO データサイズが大きくなると困るので、一度に送るのではなく細かく分けた方が良いかな。
                if (mListener != null) {
                    mListener.onReceivedData(out.toByteArray(), address, port);
                }
            }
            return true;
        } catch (Exception e) {
            if (DEBUG) {
                Log.w(TAG, "SocketTask#execute", e);
            }
            return false;
        } finally {
            if (mListener != null) {
                mListener.onDisconnected(address, port);
            }
        }
    }

    private boolean readHeader(final InputStream is) throws IOException {
        is.read(mRecvBuffer, 0 ,4);
        return mRecvBuffer[0] == HEADER[0] && mRecvBuffer[1] == HEADER[1] &&
                mRecvBuffer[2] == HEADER[2] && mRecvBuffer[3] == HEADER[3];
    }

    private int readSize(final InputStream is) throws IOException {
        is.read(mRecvBuffer, 0, 4);
        return byteToInt(mRecvBuffer, 0);
    }

    private void intToByte(final int value, final byte[] buffer, final int offset) {
        buffer[offset] = (byte) (value & 0xff);
        buffer[offset + 1] = (byte) ((value >> 8) & 0xff);
        buffer[offset + 2] = (byte) ((value >> 16) & 0xff);
        buffer[offset + 3] = (byte) ((value >> 24) & 0xff);
    }

    private int byteToInt(final byte[] buffer, final int offset) {
        return (buffer[offset] & 0xff) | ((buffer[offset + 1] & 0xff) << 8)
                | ((buffer[offset + 2] & 0xff) << 16) | ((buffer[offset + 3] & 0xff) << 24);
    }

    private String getRemoteAddress() {
        return ((InetSocketAddress) mSocket.getRemoteSocketAddress()).getAddress().getHostAddress();
    }

    private int getRemotePort() {
        return ((InetSocketAddress) mSocket.getRemoteSocketAddress()).getPort();
    }

    public interface OnSocketTaskListener {
        void onConnected(String address, int port);
        void onReceivedData(byte[] data, String address, int port);
        void onDisconnected(String address, int port);
    }
}
