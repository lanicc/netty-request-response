package io.github.lanicc.rafty.protocol.command;

import io.github.lanicc.rafty.protocol.Protocol;

/**
 * Created on 2022/6/28.
 *
 * @author lan
 */
public class HelloRequest extends Protocol {

    private String message;

    public String getMessage() {
        return message;
    }

    public HelloRequest setMessage(String message) {
        this.message = message;
        return this;
    }
}
