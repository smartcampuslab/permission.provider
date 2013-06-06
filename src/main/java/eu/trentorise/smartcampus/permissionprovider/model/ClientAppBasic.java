/**
 *    Copyright 2012-2013 Trento RISE
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package eu.trentorise.smartcampus.permissionprovider.model;

import java.util.Set;

/**
 * @author raman
 *
 */
public class ClientAppBasic {

	private String clientId;
	private String clientSecret;
	private String name;
	private String redirectUris;
	private Set<String> grantedTypes;
	
	private boolean nativeAppsAccess;
	private boolean browserAccess;
	private boolean serverSideAccess;
	
	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}
	/**
	 * @param clientId the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	/**
	 * @return the clientSecret
	 */
	public String getClientSecret() {
		return clientSecret;
	}
	/**
	 * @param clientSecret the clientSecret to set
	 */
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the redirectUris
	 */
	public String getRedirectUris() {
		return redirectUris;
	}
	/**
	 * @param redirectUris the redirectUris to set
	 */
	public void setRedirectUris(String redirectUris) {
		this.redirectUris = redirectUris;
	}
	/**
	 * @return the grantedTypes
	 */
	public Set<String> getGrantedTypes() {
		return grantedTypes;
	}
	/**
	 * @param set the grantedTypes to set
	 */
	public void setGrantedTypes(Set<String> set) {
		this.grantedTypes = set;
	}
	/**
	 * @return the nativeAppsAccess
	 */
	public boolean isNativeAppsAccess() {
		return nativeAppsAccess;
	}
	/**
	 * @param nativeAppsAccess the nativeAppsAccess to set
	 */
	public void setNativeAppsAccess(boolean nativeAppsAccess) {
		this.nativeAppsAccess = nativeAppsAccess;
	}
	/**
	 * @return the browserAccess
	 */
	public boolean isBrowserAccess() {
		return browserAccess;
	}
	/**
	 * @param browserAccess the browserAccess to set
	 */
	public void setBrowserAccess(boolean browserAccess) {
		this.browserAccess = browserAccess;
	}
	/**
	 * @return the serverSideAccess
	 */
	public boolean isServerSideAccess() {
		return serverSideAccess;
	}
	/**
	 * @param serverSideAccess the serverSideAccess to set
	 */
	public void setServerSideAccess(boolean serverSideAccess) {
		this.serverSideAccess = serverSideAccess;
	}
	
	
}
