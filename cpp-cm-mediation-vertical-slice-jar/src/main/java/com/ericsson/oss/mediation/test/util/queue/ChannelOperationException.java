package com.ericsson.oss.mediation.test.util.queue;

/**
 * Created by emaynes on 02/04/2016.
 */
public class ChannelOperationException extends RuntimeException {

    private static final long serialVersionUID = 8696558079051720341L;

    public ChannelOperationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
