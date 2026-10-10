package com.medislot.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Doctor {
    private int doctorId;
    private int userId;
    private int specialtyId;
    private String specialtyName;
    private String firstName;
    private String lastName;
    private String qualification;
    private String phone;
    private String email;
    private BigDecimal consultationFee;
    private String roomNumber;
    private Timestamp createdAt;

    public Doctor() {}

    public Doctor(int doctorId, int userId, int specialtyId, String specialtyName,
                  String firstName, String lastName, String qualification,
                  String phone, String email, BigDecimal consultationFee,
                  String roomNumber, Timestamp createdAt) {
        this.doctorId = doctorId;
        this.userId = userId;
        this.specialtyId = specialtyId;
        this.specialtyName = specialtyName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.qualification = qualification;
        this.phone = phone;
        this.email = email;
        this.consultationFee = consultationFee;
        this.roomNumber = roomNumber;
        this.createdAt = createdAt;
    }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getSpecialtyId() { return specialtyId; }
    public void setSpecialtyId(int specialtyId) { this.specialtyId = specialtyId; }

    public String getSpecialtyName() { return specialtyName; }
    public void setSpecialtyName(String specialtyName) { this.specialtyName = specialtyName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() { return "Dr. " + firstName + " " + lastName; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return getFullName() + " (" + (specialtyName != null ? specialtyName : "Specialist") + ")";
    }
}
