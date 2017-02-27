package com.ems.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.action.SpringContext;
import com.ems.model.ApplicationConfiguration;
import com.ems.model.Campus;
import com.ems.model.Company;
import com.ems.model.EnergyConsumption;
import com.ems.model.Fixture;

@Transactional(propagation = Propagation.REQUIRED)
public class PollingJob extends QuartzJobBean {

    protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
        CompanyManager companyManager = (CompanyManager) SpringContext.getBean("companyManager");
        FixtureManager fixtureManager = (FixtureManager) SpringContext.getBean("fixtureManager");
        EnergyConsumptionManager energyConsumptionManager = (EnergyConsumptionManager) SpringContext
                .getBean("energyConsumptionManager");
        Company company = companyManager.loadCompany();
        List<Campus> campusList = company.getCampuses();
        Date captureAt = new Date();
        ApplicationConfigurationManager applicationConfigurationManager = (ApplicationConfigurationManager) SpringContext
                .getBean("applicationConfigurationManager");
        ApplicationConfiguration applicationConfiguration = null;
        List<ApplicationConfiguration> applicationConfigurations = applicationConfigurationManager.loadAllConfig();
        if (applicationConfigurations != null && !applicationConfigurations.isEmpty()) {
            applicationConfiguration = applicationConfigurations.get(0);
        }
        Float price = null;
        if (applicationConfiguration != null) {
            price = company.getPrice();
        }
        if (campusList != null && !campusList.isEmpty()) {
            for (Campus campus : campusList) {
                List<Fixture> fixtures = fixtureManager.loadFixtureByCampusId(campus.getId());
                if (fixtures != null && !fixtures.isEmpty()) {
                    Iterator<Fixture> iterator = fixtures.iterator();
                    while (iterator.hasNext()) {
                        Fixture fixture = iterator.next();
                        if (fixture != null) {
                            addEnergyConsumption(fixture, captureAt, energyConsumptionManager, price);
                        }
                    }
                }
            }
        }
    }

    private void addEnergyConsumption(Fixture fixture, Date captureAt,
            EnergyConsumptionManager energyConsumptionManager, Float price) {
        EnergyConsumption energyConsumption = new EnergyConsumption();
        Random rand = new Random();
        energyConsumption.setAvgTemperature(new Double(rand.nextInt(100) + ""));
        energyConsumption.setBrightOffset(new Short(rand.nextInt(100) + ""));
        energyConsumption.setBrightPercentage(new Short(rand.nextInt(100) + ""));
        energyConsumption.setCaptureAt(captureAt);
        energyConsumption.setDimOffset(new Short(rand.nextInt(100) + ""));
        energyConsumption.setDimPercentage(new Short(rand.nextInt(100) + ""));
        energyConsumption.setFixture(fixture);
        energyConsumption.setLightAvgLevel(new Short(rand.nextInt(100) + ""));
        energyConsumption.setLightMaxLevel(new Short(rand.nextInt(100) + ""));
        energyConsumption.setLightMinLevel(new Short(rand.nextInt(100) + ""));
        energyConsumption.setLightOff(new Short(rand.nextInt(100) + ""));
        energyConsumption.setLightOn(new Short(rand.nextInt(100) + ""));
        energyConsumption.setMaxTemperature(new Short(rand.nextInt(100) + ""));
        energyConsumption.setMinTemperature(new Short(rand.nextInt(100) + ""));
        energyConsumption.setOccCount(new Short(rand.nextInt(100) + ""));
        energyConsumption.setOccIn(new Short(rand.nextInt(100) + ""));
        energyConsumption.setOccOut(new Short(rand.nextInt(100) + ""));
        energyConsumption.setPowerUsed(new BigDecimal(rand.nextInt(10000) + ""));
        energyConsumption.setPrice(price);
        if (price != null) {
            energyConsumption.setCost((energyConsumption.getPowerUsed().floatValue() * price) / 12000);
            energyConsumption.setBaseCost(energyConsumption.getCost() + rand.nextInt(100));
            energyConsumption.setSavedCost(energyConsumption.getBaseCost() - energyConsumption.getCost());
        }
        energyConsumption.setBasePowerUsed(new BigDecimal(energyConsumption.getPowerUsed().longValue()
                + rand.nextInt(100)));
        energyConsumption.setSavedPowerUsed(new BigDecimal(energyConsumption.getBasePowerUsed().longValue()
                - energyConsumption.getPowerUsed().longValue()));
        energyConsumption.setOccSaving(new BigDecimal(energyConsumption.getBasePowerUsed().longValue()
                - rand.nextInt(10)));
        energyConsumption.setTuneupSaving(new BigDecimal(energyConsumption.getBasePowerUsed().longValue()
                - rand.nextInt(10)));
        energyConsumption.setAmbientSaving(new BigDecimal(energyConsumption.getBasePowerUsed().longValue()
                - rand.nextInt(10)));
        energyConsumptionManager.save(energyConsumption);
    }
}
