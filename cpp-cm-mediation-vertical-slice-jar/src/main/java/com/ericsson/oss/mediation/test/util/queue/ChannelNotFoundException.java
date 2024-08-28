package com.ericsson.oss.mediation.test.util.queue;

/**
 * Created by emaynes on 02/04/2016.
 */
public class ChannelNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 8874934184939187966L;

    public ChannelNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
