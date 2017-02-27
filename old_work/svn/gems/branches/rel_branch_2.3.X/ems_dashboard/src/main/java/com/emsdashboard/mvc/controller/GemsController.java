package com.emsdashboard.mvc.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emsdashboard.model.GemsServer;
import com.emsdashboard.service.GemsManager;

@Controller
@RequestMapping("/gems")
public class GemsController {
    
    @Resource
    GemsManager gemsManager;
    
    @RequestMapping("/list.ems")
    public String getListOfGems(Model model) {
        List<GemsServer> gemsList = null;
        gemsList = gemsManager.loadGEMSData();
        model.addAttribute("gemsList", gemsList);
        return "gems/list";
    }
    
    @RequestMapping("/create.ems")
    String createUser(Model model) {
        
        GemsServer gems = new GemsServer();
        gems.setPort((long) 443);
        model.addAttribute("gems", gems);
        return "gems/details";
    }
    
    @RequestMapping("/save.ems")
    String saveUser(Model model,GemsServer gems) {
        try
        {
            GemsServer gemsToSave = new GemsServer();
            if (gems.getId() == null || gems.getId() == 0) {
                gemsToSave.setName(gems.getName());
                gemsToSave.setGemsIpAddress(gems.getGemsIpAddress());
                gemsToSave.setPort(gems.getPort());
                gemsManager.saveGEMSData(gemsToSave);
            }else
            {
                gemsToSave = gemsManager.loadGEMSById(gems.getId());
                gemsToSave.setName(gems.getName());
                gemsToSave.setGemsIpAddress(gems.getGemsIpAddress());
                gemsToSave.setPort(gems.getPort());
                gemsManager.updateGEMSData(gemsToSave);
            }
            
        }catch(Exception e)
        {
            model.addAttribute("error",e.getMessage());
            return "redirect:/gems/list.ems";
        }
        return "redirect:/gems/list.ems";
    }

    @RequestMapping(value = "/{gemID}/edit.ems")
    public String editGems(Model model, @PathVariable("gemID") Long gemID) {
        GemsServer gems = gemsManager.loadGEMSById(gemID);
        model.addAttribute("gems", gems);
        return "gems/details";
    }
    
    @RequestMapping(value = "/{gemID}/deleteGems.ems")
    public String deleteGems(Model model, @PathVariable("gemID") Long gemID) {
        try
        {
            gemsManager.removeGEMSData(gemID);
        }catch(Exception e)
        {
            return "redirect:/gems/list.ems";
        }
        return "redirect:/gems/list.ems";
    }
    
    @RequestMapping(value = "/{gemID}/activate.ems")
    public String activateGems(Model model, @PathVariable("gemID") Long gemID) {
        gemsManager.activateGEMS(gemID);
        return "redirect:/gems/list.ems";
    }
    
    @RequestMapping(value = "/{gemID}/deactivate.ems")
    public String deactivateGems(Model model, @PathVariable("gemID") Long gemID) {
        gemsManager.deActivateGEMS(gemID);
        return "redirect:/gems/list.ems";
    }
}
