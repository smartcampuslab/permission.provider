<%--

       Copyright 2012-2013 Trento RISE

       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at

           http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.

--%>
<%@page contentType="text/html" pageEncoding="UTF8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF8">
<meta name="viewport"
	content="width=device-width, initial-scale=1, maximum-scale=1">
<link href="../css/style.css" rel="stylesheet" type="text/css">
<title>Smart Campus Authentication</title>
</head>
<body>
	<img class="logo" src="../img/ls_logo.png" alt="SmartCampus" />
	<div class="clear"></div>
	<div class="authorities">
		<p>Please choose the provider for your login</p>
		<ul>
            <li>
		      <a href="<%=request.getContextPath() %>/eauth/fbk?target=${target}">FBK</a>
            </li>
            <li>
              <a href="<%=request.getContextPath() %>/eauth/unitn?target=${target}">UNITN</a>
            </li>
            <li>
              <a href="<%=request.getContextPath() %>/eauth/google?target=${target}">GOOGLE</a>
            </li>
		</ul>
	</div>
</body>
</html>