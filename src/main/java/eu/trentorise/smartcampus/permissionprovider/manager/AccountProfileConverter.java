/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
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
 ******************************************************************************/

package eu.trentorise.smartcampus.permissionprovider.manager;

import java.util.Set;

import eu.trentorise.smartcampus.permissionprovider.model.Attribute;
import eu.trentorise.smartcampus.permissionprovider.model.User;
import eu.trentorise.smartcampus.profile.model.AccountProfile;

/**
 * @author raman
 *
 */
public class AccountProfileConverter {

	/**
	 * @param user
	 * @return
	 */
	public static AccountProfile toAccountProfile(User user) {
		if (user == null) {
			return null;
		}
		AccountProfile minProfile = new AccountProfile();
		Set<Attribute> attrs =  user.getAttributeEntities();
		if (attrs != null) {
			for (Attribute a : attrs) {
				String account = a.getAuthority().getName();
				minProfile.addAttribute(account, a.getKey(), a.getValue());
			}
		}
		return minProfile;
	}

}
