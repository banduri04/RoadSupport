package com.help.dto;

import jakarta.persistence.Column;

import java.time.LocalDateTime;

public class FullEmergencyPostData {
    private int emergencyPostId;
    private String authorProfileName, profileImagePath;
    private int userId, civicTrustScore;
    private String imagePath1, imagePath2, imagePath3, imagePath4, imagePath5;
    private String audioFilePath;
    private String emergencyPostTitle, emergencyPostDescription;
    private LocalDateTime emergencyPostUploadDateTime;
    private Double latitude;
    private Double longitude;
    private String street;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private short emergencyPostStatus;// 0 -> Unresolved, 1 -> resolved

    public FullEmergencyPostData(int emergencyPostId, String authorProfileName, String profileImagePath, int userId,
                                 int civicTrustScore, String imagePath1, String imagePath2, String imagePath3, String imagePath4,
                                 String imagePath5, String audioFilePath, String emergencyPostTitle, String emergencyPostDescription, LocalDateTime emergencyPostUploadDateTime,
                                 Double latitude, Double longitude, String street, String city, String state, String country, String zipCode, short emergencyPostStatus) {
        this.emergencyPostId = emergencyPostId;
        this.authorProfileName = authorProfileName;
        this.profileImagePath = profileImagePath;
        this.userId = userId;
        this.civicTrustScore = civicTrustScore;
        this.imagePath1 = imagePath1;
        this.imagePath2 = imagePath2;
        this.imagePath3 = imagePath3;
        this.imagePath4 = imagePath4;
        this.imagePath5 = imagePath5;
        this.audioFilePath = audioFilePath;
        this.emergencyPostTitle = emergencyPostTitle;
        this.emergencyPostDescription = emergencyPostDescription;
        this.emergencyPostUploadDateTime = emergencyPostUploadDateTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
        this.zipCode = zipCode;
        this.emergencyPostStatus = emergencyPostStatus;
    }

    public String getAuthorProfileName() {
        return authorProfileName;
    }

    public void setAuthorProfileName(String authorProfileName) {
        this.authorProfileName = authorProfileName;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCivicTrustScore() {
        return civicTrustScore;
    }

    public void setCivicTrustScore(int civicTrustScore) {
        this.civicTrustScore = civicTrustScore;
    }

    public int getEmergencyPostId() {
        return emergencyPostId;
    }

    public void setEmergencyPostId(int emergencyPostId) {
        this.emergencyPostId = emergencyPostId;
    }

    public String getImagePath1() {
        return imagePath1;
    }

    public void setImagePath1(String imagePath1) {
        this.imagePath1 = imagePath1;
    }

    public String getImagePath2() {
        return imagePath2;
    }

    public void setImagePath2(String imagePath2) {
        this.imagePath2 = imagePath2;
    }

    public String getImagePath3() {
        return imagePath3;
    }

    public void setImagePath3(String imagePath3) {
        this.imagePath3 = imagePath3;
    }

    public String getImagePath4() {
        return imagePath4;
    }

    public void setImagePath4(String imagePath4) {
        this.imagePath4 = imagePath4;
    }

    public String getImagePath5() {
        return imagePath5;
    }

    public void setImagePath5(String imagePath5) {
        this.imagePath5 = imagePath5;
    }

    public String getAudioFilePath() {
        return audioFilePath;
    }

    public void setAudioFilePath(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }

    public String getEmergencyPostTitle() {
        return emergencyPostTitle;
    }

    public void setEmergencyPostTitle(String emergencyPostTitle) {
        this.emergencyPostTitle = emergencyPostTitle;
    }

    public String getEmergencyPostDescription() {
        return emergencyPostDescription;
    }

    public void setEmergencyPostDescription(String emergencyPostDescription) {
        this.emergencyPostDescription = emergencyPostDescription;
    }

    public LocalDateTime getEmergencyPostUploadDateTime() {
        return emergencyPostUploadDateTime;
    }

    public void setEmergencyPostUploadDateTime(LocalDateTime emergencyPostUploadDateTime) {
        this.emergencyPostUploadDateTime = emergencyPostUploadDateTime;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public short getEmergencyPostStatus() {
        return emergencyPostStatus;
    }

    public void setEmergencyPostStatus(short emergencyPostStatus) {
        this.emergencyPostStatus = emergencyPostStatus;
    }
}
