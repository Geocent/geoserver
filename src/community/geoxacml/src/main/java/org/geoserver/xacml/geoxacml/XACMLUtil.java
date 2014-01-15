/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.xacml.geoxacml;

import org.geoserver.xacml.role.XACMLRole;
import org.herasaf.xacml.core.WritingException;
import org.herasaf.xacml.core.context.RequestMarshaller;
import org.herasaf.xacml.core.context.ResponseMarshaller;
import org.herasaf.xacml.core.context.impl.DecisionType;
import org.herasaf.xacml.core.context.impl.RequestType;
import org.herasaf.xacml.core.context.impl.ResponseType;
import org.herasaf.xacml.core.context.impl.ResultType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Some utility methods
 * 
 * @author Mueller Christian
 * 
 */
public class XACMLUtil {

    static public String asXMLString(RequestType ctx) {
        OutputStream out = new ByteArrayOutputStream();
        try {
            RequestMarshaller.marshal(ctx, out);
        } catch (WritingException e) {
            throw new RuntimeException(e);
        }
        return out.toString();
    }

    static public String asXMLString(ResponseType ctx) {
        OutputStream out = new ByteArrayOutputStream();
        try {
            ResponseMarshaller.marshal(ctx, out);
        } catch (WritingException e) {
            throw new RuntimeException(e);
        }
        return out.toString();
    }

    /**
     * One Permit is enough, but all responses must be checked if there was a processing error
     * 
     * @param responses
     *            from role requests
     * @return XACML decision
     */
    public static DecisionType getDecisionFromRoleResponses(List<ResponseType> responses) {
        boolean hasPermit = false;

        for (ResponseType responseCtx : responses) {
            DecisionType decision = getDecisionFromResponseContext(responseCtx);
            if (decision == DecisionType.INDETERMINATE){
                return decision;
            } else if (decision == DecisionType.PERMIT){
                hasPermit = true;
            }
        }
        return hasPermit ? DecisionType.PERMIT : DecisionType.DENY;

    }

    public static DecisionType getDecisionFromResponseContext(ResponseType responseCtx) {
        List<ResultType> results = responseCtx.getResults();
        Set<String> resources = new HashSet<String>();

        boolean hasPermit = false, hasDeny = false;
        for (ResultType result : results) {
            DecisionType decision = result.getDecision();
            resources.add(result.getResourceId());
            if (decision == DecisionType.INDETERMINATE){
                return DecisionType.INDETERMINATE;
            } else if (decision == DecisionType.DENY){
                hasDeny = true;
            }
            if (decision == DecisionType.PERMIT){
                hasPermit = true;
            }
        }
        if (hasDeny && hasPermit) {
            logDecision(DecisionType.INDETERMINATE, resources);
            return DecisionType.INDETERMINATE;
        }
        if (!hasDeny && !hasPermit) {
            logDecision(DecisionType.NOT_APPLICABLE, resources);
            return DecisionType.NOT_APPLICABLE;
        }
        if (hasDeny) {
            logDecision(DecisionType.DENY, resources);
            return DecisionType.DENY;
        }

        return DecisionType.PERMIT;
    }

    private static void logDecision(DecisionType decision, Set<String> resources) {
        StringBuffer buff = new StringBuffer("User: ");
        buff.append(authenticationAsString());
        buff.append(" resource: ");
        for (String resource : resources) {
            buff.append(resource).append(",");
        }
        if (resources.size() > 1)
            buff.setLength(buff.length() - 1);
        buff.append(" ");
        buff.append(decision);
        getXACMLLogger().info(buff.toString());
    }

    private static String authenticationAsString() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            return "anonymous";
        String userName = auth.getCredentials() instanceof UserDetails ? ((UserDetails) auth
                .getPrincipal()).getUsername() : auth.getCredentials().toString();
        StringBuffer buff = new StringBuffer(userName);
        buff.append(" [ ");
        for (GrantedAuthority ga : auth.getAuthorities()) {
            buff.append(ga.getAuthority()).append(",");
        }
        if (auth.getAuthorities().size() > 0)
            buff.setLength(buff.length() - 1);
        buff.append(" ] ");
        return buff.toString();
    }

    public static Logger getXACMLLogger() {
        return Logger.getLogger("XACML");
    }

    public static XACMLRole toXACMLRoleFrom(GrantedAuthority grantedAuthority) {
        XACMLRole xacmlRole;
        if (grantedAuthority instanceof XACMLRole) {
            xacmlRole = (XACMLRole) grantedAuthority;
        } else {
            XACMLRole role = new XACMLRole(grantedAuthority.getAuthority());
            //TODO: Allow list of disabled roles?
            role.setEnabled(true);
            xacmlRole = role;
        }
        return xacmlRole;
    }
}
