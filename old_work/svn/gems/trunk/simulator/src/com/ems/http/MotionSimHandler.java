package com.ems.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ems.su.SensorUnit;
import com.ems.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class MotionSimHandler extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -6235238638620751366L;

    public MotionSimHandler() {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson obj = new Gson();
        List<JsonObject> oList = new ArrayList<JsonObject>();
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        Map<String, SensorUnit> oSUMap = Utils.getSensorUnits();
        if (oSUMap != null) {
            Iterator<String> keyset = oSUMap.keySet().iterator();
            while (keyset.hasNext()) {
                String sMac = keyset.next();
                SensorUnit oSU = oSUMap.get(sMac);
                if (oSU != null) {
                    JsonObject jobj = new JsonObject();
                    jobj.addProperty("mac", sMac);
                    jobj.addProperty("mbits", oSU.getMbits());
                    jobj.addProperty("lastupdated", oSU.getMbitSetTime().toLocaleString());
                    oList.add(jobj);

                }
            }
        }

        response.getWriter().println(obj.toJson(oList));
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        Gson obj = new Gson();
        Response oResponse = new Response();
        oResponse.setStatus(1);
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        String sMac = request.getParameter("mac");
        long mbits = Long.parseLong(request.getParameter("mbits"));
        Map<String, SensorUnit> oSUMap = Utils.getSensorUnits();
        if (oSUMap != null) {
            SensorUnit oSU = oSUMap.get(sMac);
            if (oSU != null) {
                oSU.setMbits(mbits);
                oResponse.setStatus(0);
            }
        }
        response.getWriter().println(obj.toJson(oResponse));
    }

}
