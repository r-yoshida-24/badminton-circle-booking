package com.badminton.viewmodel;

import com.badminton.entity.AttendanceStatus;

public record EventParticipantView(String displayName, String lineUserId, AttendanceStatus status) {
}
