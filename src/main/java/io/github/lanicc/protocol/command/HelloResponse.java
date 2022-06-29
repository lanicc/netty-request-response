package io.github.lanicc.protocol.command;

import io.github.lanicc.protocol.Protocol;

/**
 * Created on 2022/6/28.
 *
 * @author lan
 */
public class HelloResponse extends Protocol {

    private String response;

    public String getResponse() {
        return response;
    }

    public HelloResponse setResponse(String response) {
        this.response = response;
        return this;
    }
}
