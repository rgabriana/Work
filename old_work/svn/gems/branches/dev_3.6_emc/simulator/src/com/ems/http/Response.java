/**
 * 
 */
package com.ems.http;

/**
 * @author enlighted
 *
 */
public class Response {
    private int status;
    private String message;
    
    public Response() {
        status = 0;
    }
    
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}
