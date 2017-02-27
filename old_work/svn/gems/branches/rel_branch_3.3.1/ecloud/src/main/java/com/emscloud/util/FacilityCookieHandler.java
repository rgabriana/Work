package com.emscloud.util;

import com.emscloud.types.FacilityType;
import com.emscloud.util.tree.TreeNode;

public class FacilityCookieHandler {

    public static final String selectedFacilityCookie = "uem_facilites_jstree_select";
    public static final String selectedProfileCookie = "uem_profiles_jstree_select";
    public static final String facilityCookieNameSeparator = "_";
    public static final String prefixForJSTree = "%23";

    private FacilityType faciltiyType;
    private Long facilityId;

    public FacilityCookieHandler(String cookie) {

        if (cookie == null || "".equals(cookie)) {
            throw new IllegalArgumentException("Cookie is not valid");
        }
        int seperator = cookie.indexOf(FacilityCookieHandler.facilityCookieNameSeparator);
        if (seperator < 0) {
            throw new IllegalArgumentException("Separator not Valid");
        }
        faciltiyType = FacilityType.valueOf(cookie.substring(1, seperator).toUpperCase());
        facilityId = new Long(cookie.substring(seperator + 1));
    }

    public FacilityType getFaciltiyType() {
        return faciltiyType;
    }

    public Long getFacilityId() {
        return facilityId;
    }

    public static String getDefaultNodeIdToSelect(TreeNode<FacilityType> facilityTreeHierarchy) {
        TreeNode<FacilityType> selectedNode = facilityTreeHierarchy.getLogicalSelection();
        return prefixForJSTree + selectedNode.getNodeType().toString().toLowerCase() + "_" + selectedNode.getNodeId();
    }

}