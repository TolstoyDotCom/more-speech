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
package com.tolstoy.basic.gui;

public class ElementDescriptor {
	public String type, key, label, help;
	public int width;

	public ElementDescriptor( String type, String key, String label, String help, int width ) {
		this.type = type;
		this.key = key;
		this.label = label;
		this.help = help;
		this.width = width;
	}
}
