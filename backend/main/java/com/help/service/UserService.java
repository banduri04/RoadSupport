package com.help.service;

import com.help.dto.*;
import com.help.email.EmailService;
import com.help.email.GetMailText;
import com.help.model.*;
import com.help.repository.UserAuthDataRepository;
import com.help.repository.UserReportLogRepository;
import com.help.repository.UserRepository;
import com.help.repository.UserSubscriptionLogRepository;
import com.help.validation.UserValidation;
import jakarta.mail.Multipart;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserAuthDataRepository userAuthDataRepository;
    private final UserValidation userValidation;
    private final EmailService emailService;
    private final GeoService geoService;
    private final UserReportLogRepository userReportLogRepository;
    private final UserSubscriptionLogRepository userSubscriptionLogRepository;
    private final PostService postService;
    private final CampaignService campaignService;
    private final EmergencyPostService emergencyPostService;
    private final ConcurrentHashMap<String, OtpDetails> otpStorage = new ConcurrentHashMap<String, OtpDetails>();

    @Autowired
    public UserService(UserRepository userRepository, UserAuthDataRepository userAuthDataRepository,
                       UserValidation userValidation, EmailService emailService, GeoService geoService,
                       UserReportLogRepository userReportLogRepository, UserSubscriptionLogRepository userSubscriptionLogRepository,
                       PostService postService, CampaignService campaignService, EmergencyPostService emergencyPostService){
        this.userRepository = userRepository;
        this.userAuthDataRepository = userAuthDataRepository;
        this.userValidation=userValidation;
        this.emailService=emailService;
        this.geoService=geoService;
        this.userReportLogRepository=userReportLogRepository;
        this.userSubscriptionLogRepository=userSubscriptionLogRepository;
        this.postService=postService;
        this.campaignService=campaignService;
        this.emergencyPostService=emergencyPostService;
    }

    public ServiceResponse<SubscriptionDetails> getUserSubscriptionDetails(){
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isEmpty())return new ServiceResponse<>("No user found.");
        Optional<UserSubscriptionLog> userSubscriptionLog = userSubscriptionLogRepository.findByUserId(user.get().getUserId());
        if(userSubscriptionLog.isEmpty())return new ServiceResponse<>("No active subscription found.");

        else if(userSubscriptionLog.get().getEndDate().isBefore(LocalDateTime.now())){
            SubscriptionDetails details=new SubscriptionDetails(userSubscriptionLog.get().getUserSubscriptionId(),
                                                                userSubscriptionLog.get().getUserId(),
                                                                userSubscriptionLog.get().getStartDate(),
                                                                userSubscriptionLog.get().getEndDate(),
                                                                userSubscriptionLog.get().getLog());

            return new ServiceResponse<>("Subscription expired.", details);
        }

        SubscriptionDetails details=new SubscriptionDetails(userSubscriptionLog.get().getUserSubscriptionId(),
                                                            userSubscriptionLog.get().getUserId(),
                                                            userSubscriptionLog.get().getStartDate(),
                                                            userSubscriptionLog.get().getEndDate(),
                                                            userSubscriptionLog.get().getLog());
        return new ServiceResponse<>(details);
    }

    public boolean sendRegistrationEmailOTP(String email){
        String updatedEmail=email.replace("\""," ").trim();
        if(!userValidation.isValidEmail(updatedEmail))return false;
        OtpDetails otpDetails=new OtpDetails();
        this.otpStorage.put(updatedEmail,otpDetails);
        return emailService.sendRegistrationEmailOTP(updatedEmail, GetMailText.userMailSubjectOTP,GetMailText.userMailTextOTP.replace("{}", otpDetails.getOtp()));
    }

    public int verifyRegistrationOTP(OtpForVerification otpForVerification){
        otpForVerification.setEmail(otpForVerification.getEmail().replace("\""," ").trim());
        if(!this.otpStorage.get(otpForVerification.getEmail()).getGeneratedAt().plusMinutes(1).isAfter(LocalDateTime.now())){this.otpStorage.remove(otpForVerification.getEmail());return -1;}
        if(this.otpStorage.get(otpForVerification.getEmail()).getOtp().compareTo(otpForVerification.getOtp()) != 0) return -2;
        this.otpStorage.remove(otpForVerification.getEmail());
        return 0;
    }

    public ServiceResponse<UserProfile> updateUser(String uname, User newUser, MultipartFile image) {
        String msg=userValidation.isValidUserDetails(newUser);
        if(!msg.equals("Validated.")) {return new ServiceResponse<>(msg,null);}
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        if(!uname.equals(username))return new ServiceResponse<>("Invalid username!");
        String root=Paths.get("").toAbsolutePath().toString();
        String profileImagePath=null;
        User user=userRepository.findByUsername(username).get();
        user.setUserFirstName(newUser.getUserFirstName());
        user.setUserLastName(newUser.getUserLastName());
        user.setUserPhoneNumber(newUser.getUserPhoneNumber());
        user.setCity(newUser.getCity());
        user.setState(newUser.getState());
        user.setStreet(newUser.getStreet());
        user.setZipCode(newUser.getZipCode());
        user.setCountry(newUser.getCountry());
        if(image!=null && !image.isEmpty()) {
            if(user.getProfileImagePath()!=null && !user.getProfileImagePath().isEmpty() && !deleteExistingProfileImage(user.getProfileImagePath()))
                return new ServiceResponse<>("Failed to update profile image.");
            profileImagePath = saveUserProfileImage(image, root);
            if(profileImagePath==null)return new ServiceResponse<>("Failed to update profile image.");
            user.setProfileImagePath(profileImagePath.replace(root+"\\allMedia",""));
        }
        userRepository.save(user);
        return new ServiceResponse<>("updated.", userRepository.findUserProfile(uname).get());
    }

    private boolean deleteExistingProfileImage(String imagePath){
        String root=Paths.get("").toAbsolutePath().toString();
        Path profilePath=Paths.get(root+"/allMedia"+imagePath);
        try{Files.delete(profilePath);return true;}catch (Exception e){e.fillInStackTrace();}
        return false;
    }

    private String saveUserProfileImage( MultipartFile image, String root){
        String path=null;
        try{
            if(image.isEmpty() || image.getSize() > (5 * 1024 * 1024))return null;
            Path postImagePath=Paths.get(root+"/allMedia/userProfileImage");
            if(!Files.exists(postImagePath))Files.createDirectories(postImagePath);
            Path uploadPath=postImagePath.resolve(UUID.randomUUID().toString()+"_"+System.currentTimeMillis()+"_"+image.getOriginalFilename());
            path=uploadPath.toString();
            image.transferTo(uploadPath.toFile());
            return path;
        }catch (Exception e){
            e.printStackTrace();
            try{Files.delete(Paths.get(path));}catch (Exception e1){System.out.println(e1.toString());}
        }
        return null;
    }

    @Transactional
    public ServiceResponse<Boolean> deleteUser(int userId) {
        if(!userValidation.isValidNumeric(Integer.toString(userId)))return new ServiceResponse<>("Invalid user id.", false);
        Optional <User> user=userRepository.findById(userId);
        if(user.isEmpty())return new ServiceResponse<>("User not found.", false);
        ServiceResponse<UserPost> postList = postService.getAllPostsOfUser();
        List<UserCampaign> campaignList = campaignService.getAllCampaignsOfUser();
        ServiceResponse<EmergencyPostData> emergencyPostList = emergencyPostService.getAllPostsOfUser();
        if(!postList.getObjects().isEmpty()) for(UserPost post:postList.getObjects()) postService.deletePost(post.getPostId());
        if(!campaignList.isEmpty()) for(UserCampaign campaign:campaignList) campaignService.deleteCampaign(campaign.getCampaignId());
        if(!emergencyPostList.getObjects().isEmpty()) for(EmergencyPostData emergencyPostData:emergencyPostList.getObjects())
            emergencyPostService.deleteEmergencyPost(emergencyPostData.getEmergencyPostId());
        deleteUserProfileImage(user.get().getProfileImagePath());
        userAuthDataRepository.deleteByUsername(userAuthDataRepository.findByUser_UserId(userId).get().getUsername());
        userSubscriptionLogRepository.deleteByUserId(user.get().getUserId());
        userRepository.deleteById(user.get().getUserId());
        return new ServiceResponse<>("User deleted.", true);
    }

    @Transactional
    public ServiceResponse<Boolean> deleteUser() {
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        Optional <User> user=userRepository.findByUsername(username);
        if(user.isEmpty())return new ServiceResponse<>("User not found.", false);
        ServiceResponse<UserPost> postList = postService.getAllPostsOfUser();
        List<UserCampaign> campaignList = campaignService.getAllCampaignsOfUser();
        ServiceResponse<EmergencyPostData> emergencyPostList = emergencyPostService.getAllPostsOfUser();
        if(!postList.getObjects().isEmpty()) for(UserPost post:postList.getObjects()) postService.deletePost(post.getPostId());
        if(!campaignList.isEmpty()) for(UserCampaign campaign:campaignList) campaignService.deleteCampaign(campaign.getCampaignId());
        if(!emergencyPostList.getObjects().isEmpty()) for(EmergencyPostData emergencyPostData:emergencyPostList.getObjects())
            emergencyPostService.deleteEmergencyPost(emergencyPostData.getEmergencyPostId());
        deleteUserProfileImage(user.get().getProfileImagePath());
        userAuthDataRepository.deleteByUsername(username);
        userSubscriptionLogRepository.deleteByUserId(user.get().getUserId());
        userRepository.deleteById(user.get().getUserId());
        return new ServiceResponse<>("User deleted.", true);
    }

    private boolean deleteUserProfileImage(String profileImagePath){
        String root=Paths.get("").toAbsolutePath().toString();
        if(profileImagePath==null || profileImagePath.isEmpty())return true;
        try{Files.delete(Paths.get(root+"/allMedia"+profileImagePath));}catch (Exception e){e.printStackTrace();return false;}
        return true;
    }

    public ServiceResponse<UserProfile> getUserById(int userId, String uname) {
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        if(!uname.trim().replace("\"","").equals(username))return new ServiceResponse<>("Invalid username!", null);
        return new ServiceResponse<>("Found user.", userRepository.findUserById(userId).get());
    }

    public ServiceResponse<Page<UserSearchData>> searchUser(String searchString, int page, int size) {
        if(!userValidation.isValidName(searchString))return new ServiceResponse<>("Invalid searching.");
        PageRequest pageRequest = PageRequest.of(page, size);
        searchString = searchString.trim();
        String firstName, lastName;
        Page<UserSearchData> response=null;
        if(searchString.contains(" ")){
            firstName=searchString.substring(0, searchString.lastIndexOf(" "));
            lastName=searchString.substring(searchString.lastIndexOf(" "));
            response = userRepository.searchUser(pageRequest, firstName, lastName);
        }else response = userRepository.searchUser(pageRequest, searchString);
        return new ServiceResponse<>(response.getTotalPages()==0?"No such user found.":"", response);
    }

    public ServiceResponse<UserProfile> getUserProfile(String uname) {
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        if(!uname.trim().replace("\"","").equals(username))return new ServiceResponse<>("Invalid username!", null);
        return new ServiceResponse<>("Found user.", userRepository.findUserProfile(username).get());
    }

    public ServiceResponse<Optional<UserProfile>> findOtherUsers(int userId) {
        if(!userValidation.isValidNumeric(Integer.toString(userId)))return new ServiceResponse<>("Invalid user ID.");
        Optional<UserProfile> response = userRepository.findUserProfileForSearchById(userId);
        return new ServiceResponse<>(response.isEmpty()?"No such user found.":"",response);
    }

    @Transactional
    public ServiceResponse<Optional<UserProfile>> reportUser(int userId) {
        if(!userValidation.isValidNumeric(Integer.toString(userId)))return new ServiceResponse<>("Invalid user Id.");
        Optional<User> reportedUser = userRepository.findById(userId);
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> reporterUser = userRepository.findByUsername(username);
        if(reportedUser.isEmpty() || reporterUser.isEmpty())return new ServiceResponse<>("User not found.");
        if(reporterUser.get().getUserId()==reportedUser.get().getUserId())return new ServiceResponse<>("Cannot report yourself.");
        Optional<UserReportLog> reportLog = userReportLogRepository.findByUserIds(reporterUser.get().getUserId(), reportedUser.get().getUserId());
        if(reportLog.isPresent()) {
            userReportLogRepository.deleteById(reportLog.get().getUserReportLogId());
            reportedUser.get().setUserReports(reportedUser.get().getUserReports()-1);
            userRepository.save(reportedUser.get());
            return new ServiceResponse<>("Report removed.");
        }else{
            UserReportLog log=new UserReportLog(reporterUser.get().getUserId(), reportedUser.get().getUserId(),(short) 1);
            reportedUser.get().setUserReports(reportedUser.get().getUserReports()+1);
            userReportLogRepository.save(log);
            userRepository.save(reportedUser.get());
        }
        return new ServiceResponse<>("User reported.", userRepository.findUserProfileForSearchById(userId));
    }
}
