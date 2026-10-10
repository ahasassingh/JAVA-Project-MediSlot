package com.medislot.model;

import java.sql.Time;

public class DoctorAvailability {
    private int availabilityId;
    private int doctorId;
    private DayOfWeek dayOfWeek;
    private Time startTime;
    private Time endTime;
    private int slotDurationMinutes;
    private boolean active;

    public enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }

    public DoctorAvailability() {}

    public DoctorAvailability(int availabilityId, int doctorId, DayOfWeek dayOfWeek,
                              Time startTime, Time endTime, int slotDurationMinutes, boolean active) {
        this.availabilityId = availabilityId;
        this.doctorId = doctorId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDurationMinutes = slotDurationMinutes;
        this.active = active;
    }

    public int getAvailabilityId() { return availabilityId; }
    public void setAvailabilityId(int availabilityId) { this.availabilityId = availabilityId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Time getStartTime() { return startTime; }
    public void setStartTime(Time startTime) { this.startTime = startTime; }

    public Time getEndTime() { return endTime; }
    public void setEndTime(Time endTime) { this.endTime = endTime; }

    public int getSlotDurationMinutes() { return slotDurationMinutes; }
    public void setSlotDurationMinutes(int slotDurationMinutes) { this.slotDurationMinutes = slotDurationMinutes; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
