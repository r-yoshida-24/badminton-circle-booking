package com.badminton.exception;

public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(Long eventId) {
        super("イベントが見つかりません: " + eventId);
    }
}
