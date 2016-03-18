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

import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Additional app info descriptor
 * @author raman
 *
 */
public class ClientAppInfo {
	
	public static final int APPROVED = 1;
	public static final int REJECTED = 2;
	public static final int REQUESTED = 0;
	public static final Integer UNKNOWN = -1;


	private static ObjectMapper mapper = new ObjectMapper();
	
	private String name;

	private boolean nativeAppsAccess;
	private String nativeAppSignatures;

	private Map<String, Boolean> resourceApprovals;
	
	private Map<String, Integer> identityProviders;
	
	public static ClientAppInfo convert(Map<String,Object> map) {
		return mapper.convertValue(map, ClientAppInfo.class);
	}

	public String toJson() throws Exception {
		return mapper.writeValueAsString(this);
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
	 * @return
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
	 * @return the resourceApprovals
	 */
	public Map<String, Boolean> getResourceApprovals() {
		return resourceApprovals;
	}

	/**
	 * @param resourceApprovals the resourceApprovals to set
	 */
	public void setResourceApprovals(Map<String, Boolean> resourceApprovals) {
		this.resourceApprovals = resourceApprovals;
	}

	/**
	 * @return the identityProviders
	 */
	public Map<String, Integer> getIdentityProviders() {
		return identityProviders;
	}

	/**
	 * @param identityProviders the identityProviders to set
	 */
	public void setIdentityProviders(Map<String, Integer> identityProviders) {
		this.identityProviders = identityProviders;
	}

	/**
	 * @return the nativeAppSignatures
	 */
	public String getNativeAppSignatures() {
		return nativeAppSignatures;
	}

	/**
	 * @param nativeAppSignatures the nativeAppSignatures to set
	 */
	public void setNativeAppSignatures(String nativeAppSignatures) {
		this.nativeAppSignatures = nativeAppSignatures;
	}
}
