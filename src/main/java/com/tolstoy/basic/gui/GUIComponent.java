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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public abstract class GUIComponent {
	private final ElementDescriptor desc;
	private final JLabel label;

	public abstract String getValue();
	public abstract boolean hasLabel();

	public GUIComponent( final ElementDescriptor desc ) {
		this.desc = desc;

		if ( desc.label != null && desc.label.length() > 0 ) {
			this.label = new JLabel( desc.label );
		}
		else {
			this.label = null;
		}
	}

	public void add( final JComponent component, final JPanel panel, final int row ) {
		if ( desc.help != null && desc.help.length() > 0 ) {
			component.setToolTipText( desc.help );
		}

		final GridBagConstraints constraints = new GridBagConstraints();

		if ( hasLabel() ) {
			constraints.gridx = 0;
			constraints.gridy = row;
			constraints.gridwidth = 1;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.insets = new Insets( 7, 15, 5, 5 );
			panel.add( label, constraints );

			constraints.gridx = 1;
			constraints.gridy = row;
			constraints.gridwidth = 2;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.insets = new Insets( 7, 5, 5, 15 );
			panel.add( component, constraints );
		}
		else {
			constraints.gridx = 0;
			constraints.gridy = row;
			constraints.gridwidth = 3;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.insets = new Insets( 7, 15, 5, 15 );
			panel.add( component, constraints );
		}
	}

	public JLabel getLabel() {
		return label;
	}

	public ElementDescriptor getDesc() {
		return desc;
	}

	public void storeValue( final Map<String,String> map ) {
		map.put( desc.key, getValue() );
	}
}

