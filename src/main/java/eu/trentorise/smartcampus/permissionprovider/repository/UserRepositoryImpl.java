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

package eu.trentorise.smartcampus.permissionprovider.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import eu.trentorise.smartcampus.permissionprovider.model.Attribute;
import eu.trentorise.smartcampus.permissionprovider.model.User;

/**
 * 
 * @author raman
 *
 */
public class UserRepositoryImpl implements UserRepositoryCustom {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AttributeRepository attributeRepository;

	@Override
	public List<User> getUsersByAttributes(List<Attribute> list) {
		Map<Long,User> userMap = new HashMap<Long, User>();
		for (Attribute a : list) {
			List<User> attrUsers = userRepository.findByAttribute(a.getAuthority().getName(), a.getKey(), a.getValue());
			if (attrUsers != null) {
				for (User u : attrUsers) {
					userMap.put(u.getId(), u);
				}
			}
		}
		return new ArrayList<User>(userMap.values());
	}

}
