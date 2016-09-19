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

package eu.trentorise.smartcampus.permissionprovider.oauth;

import java.util.List;

import eu.trentorise.smartcampus.permissionprovider.model.Resource;

/**
 * Resource storage interface
 * 
 * @author raman
 *
 */
public interface ResourceStorage extends ResourceServices {

	/**
	 * Store the specified {@link Resource}
	 * @param resource
	 * @return stored resource object
	 */
	public Resource storeResource(Resource resource);
	/**
	 * Store list of {@link Resource} list 
	 * @param resources
	 */
	public void storeResources(List<Resource> resources);
}
