package com.help.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.help.dto.OtpForVerification;
import com.help.jwt.dto.AuthRequest;
import com.help.jwt.dto.AuthResponse;
import com.help.jwt.dto.RegisterWrapper;
import com.help.jwt.service.AuthService;
import com.help.service.AdminService;
import com.help.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthService authService;
    private final UserService userService;
    private final AdminService adminService;

    @Autowired
    public AuthenticationController(AuthService authService, UserService userService, AdminService adminService) {
        this.authService = authService;
        this.userService = userService;
        this.adminService = adminService;
    }

    @PostMapping("/user/email/otp")
    public boolean getUserEmailOTP(@RequestBody String email){
        return userService.sendRegistrationEmailOTP(email);
    }

    @PostMapping("/user/otp/verify")
    public int verifyUserEmailOTP(@RequestBody OtpForVerification otpForVerification){// -1 for Expired OTP, -2 for Invalid OTP, 0 for Valid OTP
        return userService.verifyRegistrationOTP(otpForVerification);
    }

    @PostMapping("/user/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterWrapper wrapper) {
        String response=authService.register(wrapper.getRegisterRequest(),wrapper.getUser());
        if(response.equals("Validated.")) return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/user/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody AuthRequest request) {
        AuthResponse authResponse=authService.authenticate(request);
        if(authResponse==null)return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }

    @PostMapping("/admin/email/otp")
    public boolean getAdminEmailOTP(@RequestBody String email){
        return adminService.sendRegistrationEmailOTP(email);
    }

    @PostMapping("/admin/otp/verify")
    public int verifyAdminEmailOTP(@RequestBody OtpForVerification otpForVerification){// -1 for Expired OTP, -2 for Invalid OTP, 0 for Valid OTP
        return adminService.verifyRegistrationOTP(otpForVerification);
    }

    @PostMapping(value = "/admin/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerAdmin(@RequestPart("wrapper") String wrapperString,
                                           @RequestPart("profileImage") MultipartFile profileImage) throws JsonProcessingException {
        RegisterWrapper wrapper=new ObjectMapper().readValue(wrapperString, RegisterWrapper.class);
        String response=authService.register(wrapper.getRegisterRequest(), wrapper.getAdmin(), profileImage);
        if(response.equals("Validated.")) return ResponseEntity.status(HttpStatus.CREATED).body("Admin registered successfully!");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> loginAdmin(@RequestBody AuthRequest request) {
        AuthResponse authResponse=authService.authenticate(request);
        if(authResponse!=null){
            if(!authService.canAdminLogin(request))return ResponseEntity.status(HttpStatus.ACCEPTED).body("Admin status is inactive. Please try again later.");
            return ResponseEntity.status(HttpStatus.OK).body(authResponse);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }
}
