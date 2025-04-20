package com.softwareengineering.finsage.model;

public class ThirdParty {
    private String id;
    private String serviceName; // Only "QQ" or "WeChat"
    private String serviceUsername;
    private String servicePassword;
    private String userId;

    // Constructors, getters and setters
    public ThirdParty() {}

    public ThirdParty(String id, String serviceName, String serviceUsername, String servicePassword, String userId) {
        this.id = id;
        this.serviceName = serviceName;
        this.serviceUsername = serviceUsername;
        this.servicePassword = servicePassword;
        this.userId = userId;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getServiceUsername() { return serviceUsername; }
    public void setServiceUsername(String serviceUsername) { this.serviceUsername = serviceUsername; }
    public String getServicePassword() { return servicePassword; }
    public void setServicePassword(String servicePassword) { this.servicePassword = servicePassword; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
