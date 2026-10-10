package com.medislot.model;

import java.sql.Timestamp;

public class AppointmentHistory {
    private int historyId;
    private int appointmentId;
    private String action;
    private String previousStatus;
    private String newStatus;
    private Integer changedBy;
    private String remarks;
    private Timestamp changedAt;

    public AppointmentHistory() {}

    public AppointmentHistory(int historyId, int appointmentId, String action,
                              String previousStatus, String newStatus, Integer changedBy,
                              String remarks, Timestamp changedAt) {
        this.historyId = historyId;
        this.appointmentId = appointmentId;
        this.action = action;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.remarks = remarks;
        this.changedAt = changedAt;
    }

    public int getHistoryId() { return historyId; }
    public void setHistoryId(int historyId) { this.historyId = historyId; }

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public Integer getChangedBy() { return changedBy; }
    public void setChangedBy(Integer changedBy) { this.changedBy = changedBy; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Timestamp getChangedAt() { return changedAt; }
    public void setChangedAt(Timestamp changedAt) { this.changedAt = changedAt; }
}
