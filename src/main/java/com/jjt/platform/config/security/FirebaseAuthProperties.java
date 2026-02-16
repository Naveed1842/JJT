package com.jjt.platform.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "firebase.auth")
public class FirebaseAuthProperties {

    private String serviceAccountBase64;
    private String projectId;

    public String getServiceAccountBase64() {
        return serviceAccountBase64;
    }

    public void setServiceAccountBase64(String serviceAccountBase64) {
        this.serviceAccountBase64 = serviceAccountBase64;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}

