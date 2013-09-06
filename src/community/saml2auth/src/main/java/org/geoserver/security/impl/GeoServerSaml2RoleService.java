/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.security.GeoServerRoleService;
import org.geoserver.security.GeoServerRoleStore;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.event.RoleLoadedEvent;
import org.geoserver.security.event.RoleLoadedListener;
import org.geoserver.security.filter.GeoServerJ2eeAuthenticationFilter;
import org.geotools.util.logging.Logging;
import org.springframework.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * Implementation for {@link GeoServerRoleService} obtaining
 * roles from <b>role-name</b> elements contained in WEB-INF/web.xml
 * 
 * This implementation could be used in combination with {@link GeoServerJ2eeAuthenticationFilter} objects.
 * 
 * @author Christian
 *
 */
public class GeoServerSaml2RoleService extends AbstractGeoServerSecurityService 
    implements GeoServerRoleService {
    
    
    protected static Logger LOGGER = Logging.getLogger("org.geoserver.security");
    
    protected String adminRoleName, groupAdminRoleName;
    protected SortedSet<GeoServerRole> emptySet;
    protected SortedSet<String> emptyStringSet;
    protected Map<String,String> parentMappings;
    protected HashMap<String, GeoServerRole> roleMap;
    protected SortedSet<GeoServerRole> roleSet;
    
    protected Set<RoleLoadedListener> listeners = 
        Collections.synchronizedSet(new HashSet<RoleLoadedListener>());

    protected GeoServerSaml2RoleService() throws IOException{
        emptySet=Collections.unmodifiableSortedSet(new TreeSet<GeoServerRole>());
        emptyStringSet=Collections.unmodifiableSortedSet(new TreeSet<String>());      
        parentMappings=new HashMap<String,String>();
        load();
    }

    
    @Override
    public void initializeFromConfig(SecurityNamedServiceConfig config) throws IOException {        
        super.initializeFromConfig(config);
        adminRoleName = ((SecurityRoleServiceConfig)config).getAdminRoleName();
        groupAdminRoleName = ((SecurityRoleServiceConfig)config).getGroupAdminRoleName();
        load();
    }

    
    @Override
    public GeoServerRole getAdminRole() {
        if (StringUtils.hasLength(adminRoleName)==false)
            return null;
        try {
            return getRoleByName(adminRoleName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GeoServerRole getGroupAdminRole() {
        if (StringUtils.hasLength(groupAdminRoleName)==false)
            return null;
        try {
            return getRoleByName(groupAdminRoleName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    
    @Override
    public GeoServerRoleStore createStore() throws IOException {        
        return null;
    }

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#registerRoleLoadedListener(org.geoserver.security.event.RoleLoadedListener)
     */
    public void registerRoleLoadedListener (RoleLoadedListener listener) {
        listeners.add(listener);
        
    }
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#unregisterRoleLoadedListener(org.geoserver.security.event.RoleLoadedListener)
     */
    public void unregisterRoleLoadedListener (RoleLoadedListener listener) {
        listeners.remove(listener);
    }

                
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#getRoles()
     */
    public SortedSet<GeoServerRole> getRoles()   throws IOException{
        if (roleSet!=null)
            return roleSet;        
        return emptySet;
    
    }
            

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#load()
     */
    public synchronized void load() throws IOException{
        
        //TODO: query for list of roles
        fireRoleLoadedEvent();
    }

     
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#getRolesForUser(java.lang.String)
     */
    public  SortedSet<GeoServerRole> getRolesForUser(String username)  throws IOException{
        return emptySet;
    }

    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#getRolesForGroup(java.lang.String)
     */
    public  SortedSet<GeoServerRole> getRolesForGroup(String groupname)  throws IOException{
        return emptySet;
    }

    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#createRoleObject(java.lang.String)
     */
    public GeoServerRole createRoleObject(String role)   throws IOException{
        return new GeoServerRole(role);
    }
    

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#getParentRole(org.geoserver.security.impl.GeoserverRole)
     */
    public GeoServerRole getParentRole(GeoServerRole role)   throws IOException{
        //TODO
        return null;        
    }
    
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#getRoleByName(java.lang.String)
     */
    public GeoServerRole getRoleByName(String role) throws  IOException {
            if (roleMap!=null)
                return roleMap.get(role);
            return null;
    }
    
    /**
     * Fire {@link RoleLoadedEvent} for all listeners
     */
    protected void fireRoleLoadedEvent() {
        RoleLoadedEvent event = new RoleLoadedEvent(this);
        for (RoleLoadedListener listener : listeners) {
            listener.rolesChanged(event);
        }
    }    

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#getGroupNamesForRole(org.geoserver.security.impl.GeoserverRole)
     */
    public SortedSet<String> getGroupNamesForRole(GeoServerRole role) throws IOException {
        //TODO
        return emptyStringSet;
    }


    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#getUserNamesForRole(org.geoserver.security.impl.GeoserverRole)
     */
    public SortedSet<String> getUserNamesForRole(GeoServerRole role) throws IOException{
        //TODO
        return emptyStringSet;
    }
    
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#getParentMappings()
     */
    public  Map<String,String> getParentMappings() throws IOException {
        return parentMappings;
    }

    /** (non-Javadoc)
     * @see org.geoserver.security.GeoServerRoleService#personalizeRoleParams(java.lang.String, java.util.Properties, java.lang.String, java.util.Properties)
     * 
     * Do nothing, SAML2 roles have no role params 
     */
    public  Properties personalizeRoleParams (String roleName,Properties roleParams, 
            String userName,Properties userProps) throws IOException {
        return null;
    }

    /**
     * The root configuration for the role service.
     */
    public File getConfigRoot() throws IOException {
        return new File(getSecurityManager().getRoleRoot(), getName());
    }
    
    public int getRoleCount() throws IOException {
        if (roleSet != null)
            return roleSet.size();
        return 0;
    }
}
