/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.xacml.geoxacml;


import com.vividsolutions.jts.geom.Geometry;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserDetailsImpl implements UserDetails {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    Geometry geometryRestriction;

    int persNr;

    String username, password;

    Collection<GrantedAuthority> authorities = null;

    public UserDetailsImpl(String name, String pw, Collection<GrantedAuthority> authorities) {
        username = name;
        password = pw;
        this.authorities = authorities;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public boolean isAccountNonExpired() {
        return false;
    }

    public boolean isAccountNonLocked() {
        return false;
    }

    public boolean isCredentialsNonExpired() {
        return false;
    }

    public boolean isEnabled() {
        return false;
    }

    public Geometry getGeometryRestriction() {
        return geometryRestriction;
    }

    public void setGeometryRestriction(Geometry geometryRestriction) {
        this.geometryRestriction = geometryRestriction;
    }

    public int getPersNr() {
        return persNr;
    }

    public void setPersNr(int persNr) {
        this.persNr = persNr;
    }

}
