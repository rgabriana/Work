package com.ems.mvc.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ems.model.Company;
import com.ems.service.CompanyManager;

@PrepareForTest
@RunWith(PowerMockRunner.class)
public class ApplicationEntryPointControllerTest {

    @Test
    public void testEntryPointForCompanyExisting() {
//        ApplicationEntryPointController appEntryPointController = new ApplicationEntryPointController();
//        CompanyManager companyManager = mock(CompanyManager.class);
//        appEntryPointController.setCompanyManager(companyManager);
//
//        List<Company> companyList = new ArrayList<Company>();
//        Company company = new Company();
//        company.setCompletionStatus(3);
//        companyList.add(company);
//
//        when(companyManager.getAllCompanies()).thenReturn(companyList);
//
//        String view = appEntryPointController.entryPoint();
//
//        assertEquals("redirect:facilities/home.ems", view);
    }

    @Test
    public void testEntryPointForCompanyNotExisting() {

//        ApplicationEntryPointController appEntryPointController = new ApplicationEntryPointController();
//        CompanyManager companyManager = mock(CompanyManager.class);
//        appEntryPointController.setCompanyManager(companyManager);
//
//        List<Company> companyList = new ArrayList<Company>();
//
//        when(companyManager.getAllCompanies()).thenReturn(companyList);
//
//        String view = (String) appEntryPointController.entryPoint();
//
//        assertEquals("redirect:companySetup.ems", view);

    }

    @Test
    public void testEntryPointForCompanyPartiallyExisting() {

//        ApplicationEntryPointController appEntryPointController = new ApplicationEntryPointController();
//        CompanyManager companyManager = mock(CompanyManager.class);
//        appEntryPointController.setCompanyManager(companyManager);
//
//        List<Company> companyList = new ArrayList<Company>();
//        Company company = new Company();
//        company.setName("Enlighted Inc");
//        company.setCompletionStatus(2);
//        companyList.add(company);
//
//        when(companyManager.getAllCompanies()).thenReturn(companyList);
//
//        String view = appEntryPointController.entryPoint();
//        
//        assertEquals("redirect:companySetup.ems", view);
    }

}
