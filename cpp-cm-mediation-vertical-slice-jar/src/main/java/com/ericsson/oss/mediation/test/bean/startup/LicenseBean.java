
package com.ericsson.oss.mediation.test.bean.startup;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.lcm.api.LicenseControlMonitorService;
import com.ericsson.oss.services.lcm.api.LicenseException;

@Startup
@Singleton
public class LicenseBean {

    @EServiceRef
    private LicenseControlMonitorService lcmServiceBean;

    private static final String ERBS_LICENSE_STRING = "FAT1023070";
    private static final String ERBS_LICENSE_KEY =
            "14 FAT1023070 Ni LONG NORMAL NETWORK EXCL 1000000 INFINITE_KEYS 9 OCT 2018 10 APR 2019 NO_SHR SLM_CODE 1 "
                    + "NON_COMMUTER NO_GRACE NO_OVERDRAFT DEMO NON_REDUNDANT Ni NO_HLD 20 Radio_Network_Base_Package_numberOf_5MHzSC "
                    + "XM,0jWXjIA3gHmO7LtjzVPuzun34zuzJ0a,8H0PMFR37wtrEWy1V1o9NLVkt1xHDVSAvPY4N39Z:lcJOwNQPjhfsthgg7hYTBksbouMCYYBIV:didFc294jFx3SzcDdraO3W";

    @Inject
    private Logger logger;

    @PostConstruct
    public void createLicense() {
        try {
            logger.info("Creating ERBS License with String {}", ERBS_LICENSE_STRING);
            lcmServiceBean.addLicense(ERBS_LICENSE_KEY);
        } catch (final LicenseException e) {
            logger.error("Not able to add License", e);
        }
    }

    @PreDestroy
    public void deleteLicense() {
        try {
            logger.info("Deleting ERBS License with Key {}", ERBS_LICENSE_KEY);
            lcmServiceBean.deleteLicense(ERBS_LICENSE_KEY);
        } catch (final LicenseException e) {
            logger.error("Not able to delete License", e);
        }
    }
}
