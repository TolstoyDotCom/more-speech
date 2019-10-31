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

import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class GUIComponentPasswordField extends GUIComponent {
	private final JPasswordField field;

	public GUIComponentPasswordField( final ElementDescriptor desc, final String value, final JPanel panel, final int row ) {
		super( desc );

		field = new JPasswordField( value, getDesc().width );

		add( field, panel, row );
	}

	@Override
	public String getValue() {
		return String.valueOf( field.getPassword() );
	}

	@Override
	public boolean hasLabel() {
		return true;
	}
}

