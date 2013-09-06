package org.geoserver.xacml.security;

import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.filter.GeoServerCompositeFilter;
import org.geoserver.xacml.spring.security.XACMLFilterAccessDecisionManager;
import org.geoserver.xacml.spring.security.XACMLFilterDecisionVoter;
import org.geoserver.xacml.spring.security.XACMLFilterSecurityInterceptor;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XACMLSecurityInterceptorFilter extends GeoServerCompositeFilter {

    @Override
    public void initializeFromConfig(SecurityNamedServiceConfig config) throws IOException {
        super.initializeFromConfig(config);

        XACMLSecurityInterceptorFilterConfig siConfig =
                (XACMLSecurityInterceptorFilterConfig) config;

        XACMLFilterSecurityInterceptor filter = new XACMLFilterSecurityInterceptor();

        filter.setAuthenticationManager(getSecurityManager());


        List<AccessDecisionVoter> voters = new ArrayList<AccessDecisionVoter>();
        voters.add(new XACMLFilterDecisionVoter());
        //voters.add(new AuthenticatedVoter());

        XACMLFilterAccessDecisionManager accessDecisionManager = new XACMLFilterAccessDecisionManager(voters);
        accessDecisionManager.setAllowIfAllAbstainDecisions(siConfig.isAllowIfAllAbstainDecisions());

        filter.setAccessDecisionManager(accessDecisionManager);

        filter.setSecurityMetadataSource((FilterInvocationSecurityMetadataSource)filter.obtainSecurityMetadataSource());

        try {
            filter.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        getNestedFilters().add(filter);
    }

}
