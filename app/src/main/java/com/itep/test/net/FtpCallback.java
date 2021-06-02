package com.itep.test.net;

public interface FtpCallback {
    void ftpSuccess(String msg, String speed);

    void ftpFailed(String msg);
}
