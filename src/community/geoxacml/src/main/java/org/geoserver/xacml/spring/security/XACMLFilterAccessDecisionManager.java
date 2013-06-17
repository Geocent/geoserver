/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.xacml.spring.security;

import java.util.Collection;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;

/**
 * Spring Security AccessDecsionsManger implementation for Services
 * 
 * @author Christian Mueller
 * 
 */
public class XACMLFilterAccessDecisionManager extends AbstractAccessDecisionManager {

    public void decide(Authentication auth, Object arg1, Collection<ConfigAttribute> arg2)
            throws AccessDeniedException, InsufficientAuthenticationException {

        AccessDecisionVoter voter = (AccessDecisionVoter) this.getDecisionVoters().get(0);
        int decision = voter.vote(auth, arg1, arg2);
        if (decision != AccessDecisionVoter.ACCESS_GRANTED) {
            throw new AccessDeniedException("Access Denied: " + arg1.toString());
        }
    }

}
