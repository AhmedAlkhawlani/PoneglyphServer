package com.nova.poneglyph.exception;

public class AccountDisabledException extends Throwable {
    public AccountDisabledException(String accountIsDisabled) {
        super(accountIsDisabled);
    }
}
