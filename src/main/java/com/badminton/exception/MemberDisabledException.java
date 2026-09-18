package com.badminton.exception;

public class MemberDisabledException extends RuntimeException {

    public MemberDisabledException() {
        super("現在この会員は利用できません。");
    }
}
