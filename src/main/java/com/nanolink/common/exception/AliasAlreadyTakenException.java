package com.nanolink.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AliasAlreadyTakenException extends RuntimeException {
    public AliasAlreadyTakenException() {
        super("Alias already taken");
    }
}
