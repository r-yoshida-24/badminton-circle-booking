package com.badminton.exception;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException() {
        super("サークル会員として登録されていません。");
    }
}
