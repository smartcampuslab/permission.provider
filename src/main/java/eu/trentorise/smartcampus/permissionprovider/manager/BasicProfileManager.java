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

package eu.trentorise.smartcampus.permissionprovider.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import eu.trentorise.smartcampus.permissionprovider.model.User;
import eu.trentorise.smartcampus.permissionprovider.repository.UserRepository;
import eu.trentorise.smartcampus.profile.model.AccountProfile;
import eu.trentorise.smartcampus.profile.model.BasicProfile;

/**
 * @author raman
 *
 */
@Component
@Transactional
public class BasicProfileManager {

	@Autowired
	private UserRepository userRepository;
	/**
	 * @param userId
	 * @return
	 */
	public BasicProfile getBasicProfileById(String userId) {
		try {
			return BasicProfileConverter.toBasicProfile(userRepository.findOne(Long.parseLong(userId)));
		} catch (Exception e) {
			throw new IllegalStateException("User with id "+userId +" does not exist");
		}
	}
	/**
	 * returns all users eu.trentorise.smartcampus.profileservice.model in the
	 * system
	 * 
	 * @return the list of all minimal profiles of users
	 * @throws CommunityManagerException
	 */
	public List<BasicProfile> getUsers() {
		try {
			return BasicProfileConverter.toBasicProfile(userRepository.findAll());
		} catch (Exception e) {
			throw new IllegalStateException("Problem reading users: "+e.getMessage());
		}
	}
	/**
	 * returns all minimal eu.trentorise.smartcampus.profileservice.model of
	 * users who match part of name
	 * 
	 * @param name
	 *            the string to match with name of user
	 * @return the list of minimal
	 *         eu.trentorise.smartcampus.profileservice.model of users which
	 *         name contains parameter or an empty list
	 * @throws CommunityManagerException
	 */
	public List<BasicProfile> getUsers(String fullNameFilter) {
		try {
			return BasicProfileConverter.toBasicProfile(userRepository.findByFullNameLike("%"+fullNameFilter+"%"));
		} catch (Exception e) {
			throw new IllegalStateException("Problem reading users: "+e.getMessage());
		}
	}
	/**
	 * @param userIds
	 * @return
	 */
	public List<BasicProfile> getUsers(List<String> userIds) {
		if (userIds != null) {
			List<BasicProfile> list = new ArrayList<BasicProfile>();
			for (String userId : userIds) {
				try {
					list.add(getBasicProfileById(userId));
				} catch (Exception e) {
					throw new IllegalStateException("Problem reading users: "+e.getMessage());
				}
			}
		}
		return Collections.emptyList();
	}
	/**
	 * @param socialId
	 * @return
	 */
	public BasicProfile getBasicProfileBySocialId(String socialId) {
		try {
			return BasicProfileConverter.toBasicProfile(userRepository.findBySocialId(socialId));
		} catch (Exception e) {
			throw new IllegalStateException("Problem reading users: "+e.getMessage());
		}
	}
	/**
	 * @param userId
	 * @return
	 */
	public AccountProfile getAccountProfileById(String userId) {
		User user = userRepository.findOne(Long.parseLong(userId));
		return AccountProfileConverter.toAccountProfile(user);
	}
	/**
	 * @param userIds
	 * @return
	 */
	public List<AccountProfile> getAccountProfilesById(List<String> userIds) {
		List<AccountProfile> list = new ArrayList<AccountProfile>();
		if (userIds != null) {
			for (String userId : userIds) {
				list.add(getAccountProfileById(userId));
			}
		}
		return list;
	}

}
