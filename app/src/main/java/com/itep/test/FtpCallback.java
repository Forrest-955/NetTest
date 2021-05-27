package com.itep.test;

public interface FtpCallback {
    void ftpSuccess(String msg, String speed);

    void ftpFailed(String msg);
}
