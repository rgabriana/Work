package com.ems.vo;

public class LdapSettings {

    String authenticationType = "DATABASE";
    String url;
    String ldapAuthenticationType;
    String securityPrincipal;
    boolean allowNonVFMUsers = false;

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLdapAuthenticationType() {
        return ldapAuthenticationType;
    }

    public void setLdapAuthenticationType(String ldapAuthenticationType) {
        this.ldapAuthenticationType = ldapAuthenticationType;
    }

    public String getSecurityPrincipal() {
        return securityPrincipal;
    }

    public void setSecurityPrincipal(String securityPrincipal) {
        this.securityPrincipal = securityPrincipal;
    }

    public boolean isAllowNonVFMUsers() {
        return allowNonVFMUsers;
    }

    public void setAllowNonVFMUsers(boolean allowNonVFMUsers) {
        this.allowNonVFMUsers = allowNonVFMUsers;
    }

}
