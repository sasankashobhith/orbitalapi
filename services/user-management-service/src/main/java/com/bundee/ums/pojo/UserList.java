package com.bundee.ums.pojo;

import com.bundee.msfw.defs.*;

import java.util.*;

public class UserList extends BaseResponse {
    List<User> userdetail;
    List<Channel> channels;
    List<DriverProfile> driverProfiles;
    UserResponse userResponse;
    List<UserResponse> userResponses;
    List<PushNotificationsResponse> pushnotifications;
    List<OwnerEmployeeMappingResponse> owneremployeemapping;
    List<MasterRoles> masterroles;
    List<UserRoleMap> userrolemap;
    List<MobileConfResponse> mobileconfigration;
    List<ApiVersion> apiVersions;
    List<HostProfile> hostProfiles;
    private String errorCode;
    private String errorMessage;
    private int count;
    List<ErrorLog> errorLogs;

    public List<ErrorLog> getErrorLogs() {
        return errorLogs;
    }

    public void setErrorLogs(List<ErrorLog> errorLogs) {
        this.errorLogs = errorLogs;
    }

    public UserList() {
        userdetail = new ArrayList<User>();
        userResponses = new ArrayList<UserResponse>();
        pushnotifications = new ArrayList<PushNotificationsResponse>();
        owneremployeemapping = new ArrayList<OwnerEmployeeMappingResponse>();
        userrolemap = new ArrayList<UserRoleMap>();
        masterroles = new ArrayList<MasterRoles>();
        mobileconfigration = new ArrayList<MobileConfResponse>();
        apiVersions = new ArrayList<ApiVersion>();
        hostProfiles = new ArrayList<HostProfile>();
        channels = new ArrayList<Channel>();
        driverProfiles=new ArrayList<DriverProfile>();
        errorLogs=new ArrayList<ErrorLog>();
    }

    public List<HostProfile> getHostProfiles() {
        return hostProfiles;
    }

    public void setHostProfiles(List<HostProfile> hostProfiles) {
        this.hostProfiles = hostProfiles;
    }

    public List<DriverProfile> getDriverProfiles() {
        return driverProfiles;
    }

    public void setDriverProfiles(List<DriverProfile> driverProfiles) {
        this.driverProfiles = driverProfiles;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public List<ApiVersion> getApiVersions() {
        return apiVersions;
    }

    public void setApiVersions(List<ApiVersion> apiVersions) {
        this.apiVersions = apiVersions;
    }

    public List<MobileConfResponse> getMobileconfigration() {
        return mobileconfigration;
    }

    public void setMobileconfigration(List<MobileConfResponse> mobileconfigration) {
        this.mobileconfigration = mobileconfigration;
    }

    public List<MasterRoles> getMasterroles() {
        return masterroles;
    }

    public void setMasterroles(List<MasterRoles> masterroles) {
        this.masterroles = masterroles;
    }

    public List<UserRoleMap> getUserrolemap() {
        return userrolemap;
    }

    public void setUserrolemap(List<UserRoleMap> userrolemap) {
        this.userrolemap = userrolemap;
    }

    public List<OwnerEmployeeMappingResponse> getOwneremployeemapping() {
        return owneremployeemapping;
    }

    public void setOwneremployeemapping(List<OwnerEmployeeMappingResponse> owneremployeemapping) {
        this.owneremployeemapping = owneremployeemapping;
    }

    public List<PushNotificationsResponse> getPushnotifications() {
        return pushnotifications;
    }

    public void setPushnotifications(List<PushNotificationsResponse> pushnotifications) {
        this.pushnotifications = pushnotifications;
    }

    public List<UserResponse> getUserResponses() {
        return userResponses;
    }

    public void setUserResponses(List<UserResponse> userResponses) {
        this.userResponses = userResponses;
    }

    public UserResponse getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(UserResponse userResponse) {
        this.userResponse = userResponse;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<User> getUserdetail() {
        return userdetail;
    }

    public void setUserdetail(List<User> userdetail) {
        this.userdetail = userdetail;
    }
}
