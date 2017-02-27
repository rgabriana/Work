package com.enlightedportal.types;

public enum UserAuditActionType {
     Login,
     Customer_Create,
     Customer_Update,
     License_Generation;
     
     public String getName() {
         return this.toString().replaceAll("__", "/").replaceAll("_", " ");
     }
}
