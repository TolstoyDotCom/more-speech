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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.basic.gui.ElementDescriptor;
import com.tolstoy.basic.gui.GUIComponent;
import com.tolstoy.basic.gui.GUIComponentCheckBox;
import com.tolstoy.basic.gui.GUIComponentPasswordField;
import com.tolstoy.basic.gui.GUIComponentTextField;

class PreferencesDialog extends JDialog implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -403942946798848557L;
	private final java.util.List<GUIComponent> components;
	private Map<String,String> userdata;
	private final JButton btnSave, btnCancel;

	PreferencesDialog( final Frame parent, final IResourceBundleWithFormatting bundle, final java.util.List<ElementDescriptor> descriptors, final Map<String,String> values ) {
		super( parent, bundle.getString( "prefs_title" ), true );

		this.userdata = null;
		this.components = new ArrayList<GUIComponent>( descriptors.size() );

		final JPanel fieldsPanel = new JPanel( new GridBagLayout() );
		fieldsPanel.setBorder( new LineBorder( Color.GRAY ) );

		int row = 0;
		for ( final ElementDescriptor descriptor : descriptors ) {
			if ( "textfield".equals( descriptor.type ) ) {
				components.add( new GUIComponentTextField( descriptor, values.get( descriptor.key ), fieldsPanel, row ) );
			}
			else if ( "password".equals( descriptor.type ) ) {
				components.add( new GUIComponentPasswordField( descriptor, values.get( descriptor.key ), fieldsPanel, row ) );
			}
			else if ( "checkbox".equals( descriptor.type ) ) {
				components.add( new GUIComponentCheckBox( descriptor, Utils.isStringTrue( values.get( descriptor.key ) ), fieldsPanel, row ) );
			}
			row++;
		}

		final JPanel buttonsPanel = new JPanel( new FlowLayout() );

		btnSave = new JButton( bundle.getString( "prefs_save_button" ) );
		btnSave.addActionListener( this );
		buttonsPanel.add( btnSave );

		btnCancel = new JButton( bundle.getString( "prefs_cancel_button" ) );
		btnCancel.addActionListener( this );
		buttonsPanel.add( btnCancel );

		getContentPane().add( fieldsPanel, BorderLayout.CENTER );
		getContentPane().add( buttonsPanel, BorderLayout.PAGE_END );

		pack();
		setLocationRelativeTo( parent );
	}

	@Override
	public void actionPerformed( final ActionEvent actionEvent ) {
		if ( actionEvent.getSource() == btnSave ) {
			userdata = new HashMap<String,String>( 50 );
			for ( final GUIComponent component : components ) {
				component.storeValue( userdata );
			}
		}
		else {
			userdata = null;
		}

		dispose();
	}

	public Map<String,String> getUserdata() {
		return userdata;
	}
}
