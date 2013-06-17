/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.xacml.geoxacml;

import java.util.Arrays;
import org.geoserver.security.DataAccessManager;
import org.geoserver.security.ResourceAccessManager;
import org.geoserver.security.impl.SecureCatalogImplTest;
import org.geoserver.xacml.role.XACMLRole;
import org.geoserver.xacml.security.XACMLResourceAccessManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class XACMLSecureCatalogImplTest extends SecureCatalogImplTest {

    @Override
    protected ResourceAccessManager buildManager(String propertyFile) throws Exception {
        return (ResourceAccessManager) buildLegacyAccessManager(propertyFile);
    }

    @Override
    protected DataAccessManager buildLegacyAccessManager(String propertyFile) throws Exception {

        if ("wideOpen.properties".equals(propertyFile)) {
            GeoXACMLConfig.setPolicyRepsoitoryBaseDir("src/test/resources/wideOpen/");
        }
        if ("publicRead.properties".equals(propertyFile)) {
            GeoXACMLConfig.setPolicyRepsoitoryBaseDir("src/test/resources/publicRead/");
        }
        if ("lockedDownMixed.properties".equals(propertyFile)) {
            GeoXACMLConfig.setPolicyRepsoitoryBaseDir("src/test/resources/lockedDownMixed/");
        }
        if ("lockedDownChallenge.properties".equals(propertyFile)) {
            GeoXACMLConfig.setPolicyRepsoitoryBaseDir("src/test/resources/lockedDownChallenge/");
        }
        if ("lockedDown.properties".equals(propertyFile)) {
            GeoXACMLConfig.setPolicyRepsoitoryBaseDir("src/test/resources/lockedDown/");
        }
        if ("complex.properties".equals(propertyFile)) {
            GeoXACMLConfig.setPolicyRepsoitoryBaseDir("src/test/resources/complex/");
        }
        if ("lockedLayerInLayerGroup.properties".equals(propertyFile)) {
            GeoXACMLConfig.setPolicyRepsoitoryBaseDir("src/test/resources/lockedLayerInLayerGroup/");
        }

        GeoXACMLConfig.reset();
        return new XACMLResourceAccessManager();

    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        rwUser = new TestingAuthenticationToken("rw", "supersecret",
                Arrays.asList(new GrantedAuthority[] {
                new XACMLRole("READER"), new XACMLRole("WRITER") }));

        roUser = new TestingAuthenticationToken("ro", "supersecret",
                Arrays.asList(new GrantedAuthority[] { new XACMLRole("READER") }));

        anonymous = new TestingAuthenticationToken("anonymous", "",
                Arrays.asList(new GrantedAuthority[] { new XACMLRole(XACMLConstants.AnonymousRole) }));

        milUser = new TestingAuthenticationToken("military", "supersecret",
                Arrays.asList(new GrantedAuthority[] { new XACMLRole("MILITARY") }));

        root = new TestingAuthenticationToken("admin", "geoserver",
                Arrays.asList(new GrantedAuthority[] { new XACMLRole(XACMLConstants.AdminRole) }));

    }

}
