package me.ahmednur.twitterclone.util;

import javax.naming.AuthenticationException;

public class UsernameExistsException extends AuthenticationException {
    public UsernameExistsException(String msg) {
        super(msg);
    }
}
