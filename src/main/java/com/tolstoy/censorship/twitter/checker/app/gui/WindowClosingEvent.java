/*
 * Copyright 2018 Chris Kelly
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.tolstoy.censorship.twitter.checker.app.gui;

import java.awt.Window;
import java.util.EventObject;

public class WindowClosingEvent extends EventObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8039206645007048173L;
	private final Window window;

	public WindowClosingEvent( final Object source, final Window window ) {
		super( source );
		this.window = window;
	}

	public Window getWindow() {
		return window;
	}
}
