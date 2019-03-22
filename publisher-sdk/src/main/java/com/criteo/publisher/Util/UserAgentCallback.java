package com.criteo.publisher.Util;


/**
 * Callback to listen that we got useragent from Webview
 */
public interface UserAgentCallback {

    /**
     * Callback to notify handler that useragent is received
     *
     * @param userAgent
     */
    void done(String userAgent);

}
