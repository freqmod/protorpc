package com.likbilen.protorpc.stream.session;

/* Copyright (C) 2008 Frederik M.J. Vestre

*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at

*   http://www.apache.org/licenses/LICENSE-2.0
*/
/**
 * Session manager, implemented by TwoWayStream to make a server remember session information.
 * The session id is the same when the service is called from the same connection
 */
public interface SessionManager {
	/**
	 * Set session id
	 * @param id - session id
	 */
	public void setSessionId(Object id);
	/**
	 * Recieve session id set by setSessionId
	 * @return - session id
	 */
	public Object getSessionId();
}
