package com.syos.application.dto;

public class CustomerRegistrationRequest {
    private final String fullName;
    private final String email;
    private final String phone;
    private final String password;

    public CustomerRegistrationRequest
            (String fullName, String email, String phone, String password) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPassword() { return password; }
}