package com.badminton.form;

import com.badminton.entity.Event;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public class EventForm {

    @NotNull(message = "開催日を入力してください。")
    @FutureOrPresent(message = "開催日は今日以降を指定してください。")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate eventDate;

    @NotNull(message = "開始時間を入力してください。")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime startTime;

    @NotNull(message = "終了時間を入力してください。")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime endTime;

    @NotBlank(message = "場所を入力してください。")
    @Size(max = 200, message = "場所は200文字以内で入力してください。")
    private String place;

    @NotNull(message = "定員を入力してください。")
    @Min(value = 1, message = "定員は1以上で入力してください。")
    @Max(value = 200, message = "定員は200以下で入力してください。")
    private Integer capacity;

    @Size(max = 1000, message = "備考は1000文字以内で入力してください。")
    private String description;

    @AssertTrue(message = "開始時間は終了時間より前にしてください。")
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || startTime.isBefore(endTime);
    }

    public static EventForm from(Event event) {
        EventForm form = new EventForm();
        form.setEventDate(event.getEventDate());
        form.setStartTime(event.getStartTime());
        form.setEndTime(event.getEndTime());
        form.setPlace(event.getPlace());
        form.setCapacity(event.getCapacity());
        form.setDescription(event.getDescription());
        return form;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
