package com.badminton.form;

import com.badminton.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public class AttendanceForm {

    @NotNull(message = "出欠を選択してください。")
    private AttendanceStatus status;

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }
}
