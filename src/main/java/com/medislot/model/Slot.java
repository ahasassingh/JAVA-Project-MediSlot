package com.medislot.model;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class Slot {
    private int slotId;
    private int doctorId;
    private Date slotDate;
    private Time startTime;
    private Time endTime;
    private int maxCapacity;
    private Status status;
    private Timestamp createdAt;

    // Optional display helper
    private String doctorName;

    public enum Status {
        AVAILABLE, BOOKED, CANCELLED
    }

    public Slot() {}

    public Slot(int slotId, int doctorId, Date slotDate, Time startTime, Time endTime,
                int maxCapacity, Status status, Timestamp createdAt) {
        this.slotId = slotId;
        this.doctorId = doctorId;
        this.slotDate = slotDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxCapacity = maxCapacity;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getSlotId() { return slotId; }
    public void setSlotId(int slotId) { this.slotId = slotId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public Date getSlotDate() { return slotDate; }
    public void setSlotDate(Date slotDate) { this.slotDate = slotDate; }

    public Time getStartTime() { return startTime; }
    public void setStartTime(Time startTime) { this.startTime = startTime; }

    public Time getEndTime() { return endTime; }
    public void setEndTime(Time endTime) { this.endTime = endTime; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    @Override
    public String toString() {
        return slotDate + " [" + startTime + " - " + endTime + "] (" + status + ")";
    }
}
