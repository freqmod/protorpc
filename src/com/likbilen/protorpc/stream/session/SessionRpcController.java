package com.likbilen.protorpc.stream.session;

/* Copyright (C) 2008 Frederik M.J. Vestre

*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at

*   http://www.apache.org/licenses/LICENSE-2.0
*/

import com.google.protobuf.RpcController;

public interface SessionRpcController extends RpcController,SessionManager {
}
