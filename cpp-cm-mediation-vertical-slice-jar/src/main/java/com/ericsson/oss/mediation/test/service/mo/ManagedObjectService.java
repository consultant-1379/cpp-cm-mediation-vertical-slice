
package com.ericsson.oss.mediation.test.service.mo;

import static com.ericsson.oss.mediation.test.constants.Namespace.CPP_MED;
import static com.ericsson.oss.mediation.test.constants.Namespace.OSS_NE_DEF;
import static com.ericsson.oss.mediation.test.constants.Namespace.OSS_TOP;
import static com.ericsson.oss.mediation.test.constants.Version.V1_0_0;
import static com.ericsson.oss.mediation.test.constants.Version.V2_0_0;
import static com.ericsson.oss.mediation.test.constants.Version.V3_0_0;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.mediation.test.util.fdn.Fdn;

@Stateless
public class ManagedObjectService {

    private static final String OSS_PREFIX = "ossPrefix";
    private static final String IP_ADDRESS = "192.168.100.2";

    @Inject
    private Logger log;

    @EJB
    private CliService cliUtil;

    public void createSubNetwork(final Fdn fdn) {
        cliUtil.createManagedObject(fdn, getSubNetworkAttributes(fdn), OSS_TOP, V3_0_0);
        log.info("Created SubNetwork [{}]", fdn);
    }

    public void createMeContext(final Fdn fdn) {
        cliUtil.createManagedObject(fdn, getMeContextAttributes(fdn), OSS_TOP, V3_0_0, true);
        log.info("Created MeContext [{}]", fdn);
    }

    public void createNetworkElement(final Fdn fdn, final String ossPrefix) {
        cliUtil.createManagedObject(fdn, getNetworkElementAttributes(fdn, ossPrefix), OSS_NE_DEF, V2_0_0);
        // workaround for dead-lock caused by infinispan https://jira-nam.lmera.ericsson.se/browse/TORF-124828
        cliUtil.findMo(fdn);
        log.info("Created NetworkElement [{}] with ossPrefix '{}'", fdn, ossPrefix);
    }

    public void createCppConnectivityInfo(final Fdn fdn) {
        createCppConnectivityInfo(fdn, IP_ADDRESS);
    }

    public void createCppConnectivityInfo(final Fdn fdn, final String ipAddress) {
        cliUtil.createManagedObject(fdn, getCppConnectivityInformationAttributes(fdn, ipAddress), CPP_MED, V1_0_0);
        log.info("Created CppConnectivityInfo [{}]", fdn);
    }

    private Map<String, Object> getBasicAttributes(final Fdn fdn) {
        return getBasicAttributes(fdn, fdn.getName());
    }

    private Map<String, Object> getBasicAttributes(final Fdn fdn, final String name) {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(fdn.getType() + "Id", name);
        return attributes;
    }

    private Map<String, Object> getSubNetworkAttributes(final Fdn fdn) {
        return getBasicAttributes(fdn);
    }

    private Map<String, Object> getMeContextAttributes(final Fdn fdn) {
        final Map<String, Object> attributes = getBasicAttributes(fdn);
        attributes.put("neType", "ERBS");
        attributes.put("platformType", "CPP");
        return attributes;
    }

    private Map<String, Object> getNetworkElementAttributes(final Fdn fdn, final String ossPrefix) {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("networkElementId", fdn.getName());
        attributes.put("ossModelIdentity", "17A-H.1.160");
        attributes.put("neType", "ERBS");
        attributes.put("platformType", "CPP");
        attributes.put(OSS_PREFIX, ossPrefix);
        return attributes;
    }

    private Map<String, Object> getCppConnectivityInformationAttributes(final Fdn fdn, final String ipAddress) {
        final Map<String, Object> attributes = getBasicAttributes(fdn);
        attributes.put("ipAddress", ipAddress);
        attributes.put("port", 2345);
        return attributes;
    }
}
