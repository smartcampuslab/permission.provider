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

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.provider.token.JdbcTokenStore;

/**
 * Token store with DB tables creation on startup.
 * @see {@link JdbcTokenStore}
 * @author raman
 *
 */
public class AutoJdbcTokenStore extends JdbcTokenStore {

	private JdbcTemplate jdbcTemplate;
	
	private static final String DEFAULT_CREATE_RT_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS oauth_refresh_token ( token_id VARCHAR(64) NOT NULL PRIMARY KEY, token BLOB NOT NULL, authentication BLOB NOT NULL);";
	private static final String DEFAULT_CREATE_AT_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS oauth_access_token (token_id VARCHAR(256),  token BLOB, authentication_id VARCHAR(256), user_name VARCHAR(256), client_id VARCHAR(256), authentication BLOB, refresh_token VARCHAR(256));";
	
	private String createRefreshTokenStatement = DEFAULT_CREATE_RT_TABLE_STATEMENT;
	private String createAccessTokenStatement = DEFAULT_CREATE_AT_TABLE_STATEMENT;
	
	/**
	 * @param dataSource
	 */
	public AutoJdbcTokenStore(DataSource dataSource) {
		super(dataSource);
		initSchema(dataSource);
	}

	protected void initSchema(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.execute(createAccessTokenStatement);
		jdbcTemplate.execute(createRefreshTokenStatement);
	}

	/**
	 * @param dataSource
	 * @param createRefreshTokenStatement
	 * @param createAccessTokenStatement
	 */
	public AutoJdbcTokenStore(DataSource dataSource, String createRefreshTokenStatement, String createAccessTokenStatement) {
		super(dataSource);
		this.createRefreshTokenStatement = createRefreshTokenStatement;
		this.createAccessTokenStatement = createAccessTokenStatement;
		initSchema(dataSource);
	}
	
}
