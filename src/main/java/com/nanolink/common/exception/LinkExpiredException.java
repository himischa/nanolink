package com.nanolink.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class LinkExpiredException extends RuntimeException {
    public LinkExpiredException() {
        super("Link has expired");
    }
}
