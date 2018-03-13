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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.Document;
import javax.swing.text.html.*;
import javax.swing.event.EventListenerList;
import com.tolstoy.basic.gui.ElementDescriptor;
import com.tolstoy.basic.api.utils.*;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.basic.api.statusmessage.*;

public class MainGUI implements ActionListener, IStatusMessageReceiver {
	private EventListenerList listenerList = new EventListenerList();
	private IPreferences prefs;
	private IResourceBundleWithFormatting bundle;
	private java.util.List<ElementDescriptor> descriptors;
	private JFrame frame;
	private JButton btnPreferences, btnAction;
	private JEditorPane editorPaneStatusMessage;

	@Override
	public void addMessage( StatusMessage message ) {
		String color, html;

		if ( message.getSeverity().equals( StatusMessageSeverity.ERROR ) ) {
			color = "red";
		}
		else if ( message.getSeverity().equals( StatusMessageSeverity.WARN ) ) {
			color = "blue";
		}
		else {
			color = "black";
		}

		html = "<span style=\"color:" + color + "\">" + message.getMessage() + "</span><br/>";

		try {
			HTMLDocument document = (HTMLDocument) editorPaneStatusMessage.getDocument();
			HTMLEditorKit editorKit = (HTMLEditorKit) editorPaneStatusMessage.getEditorKit();
			editorKit.insertHTML( document, document.getLength(), html, 0, 0, null );
			//document.insertString( document.getLength(), html, null );

			editorPaneStatusMessage.setCaretPosition( document.getLength() );
		}
		catch ( Exception e ) {
		}
	}

	@Override
	public void clearMessages() {
		editorPaneStatusMessage.setText( "" );
	}

	public MainGUI( IResourceBundleWithFormatting bundle, IPreferences prefs, java.util.List<ElementDescriptor> descriptors ) {
		this.bundle = bundle;
		this.prefs = prefs;
		this.descriptors = descriptors;
	}

	public void showGUI() {
		frame = new JFrame( bundle.getString( "main_title" ) );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setSize( 500, 400 );

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout( new FlowLayout() );

		btnPreferences = new JButton( bundle.getString( "main_prefs_button" ) );
		btnPreferences.addActionListener( this );
		buttonPanel.add( btnPreferences );

		btnAction = new JButton( bundle.getString( "main_run_button" ) );
		btnAction.addActionListener( this );
		buttonPanel.add( btnAction );

		frame.getContentPane().add( buttonPanel, BorderLayout.PAGE_START );

		editorPaneStatusMessage = new JEditorPane();
		editorPaneStatusMessage.setEditorKit( new HTMLEditorKit() );
		editorPaneStatusMessage.setContentType( "text/html" );
		editorPaneStatusMessage.setEditable( false );

		JScrollPane scrollPane = new JScrollPane( editorPaneStatusMessage );
		scrollPane.setPreferredSize( new Dimension( 480, 150 ) );
		scrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
		scrollPane.setBorder( new BevelBorder( BevelBorder.LOWERED ) );

		frame.getContentPane().add( scrollPane, BorderLayout.CENTER );

		frame.addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing( WindowEvent windowEvent ) {
				fireWindowClosingEvent( new WindowClosingEvent( this, windowEvent.getWindow() ) );
			}
		});

		frame.setVisible( true );
	}

	public void enableRunFunction( boolean run ) {
		btnAction.setEnabled( run );
	}

	@Override
	public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getSource() == btnPreferences ) {
			PreferencesDialog preferencesDialog = new PreferencesDialog( frame, bundle, descriptors, prefs.getValues() );
			preferencesDialog.setVisible( true );
			java.util.Map<String,String> userdata = preferencesDialog.getUserdata();
			if ( userdata != null ) {
				firePreferencesEvent( new PreferencesEvent( this, userdata ) );
			}
		}
		else if ( actionEvent.getSource() == btnAction ) {
			fireRunEvent( new RunEvent( this ) );
		}
	}

	public void addRunEventListener( RunEventListener l ) {
		listenerList.add( RunEventListener.class, l );
	}

	public void removeRunEventListener( RunEventListener l ) {
		listenerList.remove( RunEventListener.class, l );
	}

	public void addPreferencesEventListener( PreferencesEventListener l ) {
		listenerList.add( PreferencesEventListener.class, l );
	}

	public void removePreferencesEventListener( PreferencesEventListener l ) {
		listenerList.remove( PreferencesEventListener.class, l );
	}

	public void addWindowClosingEventListener( WindowClosingEventListener l ) {
		listenerList.add( WindowClosingEventListener.class, l );
	}

	public void removeWindowClosingEventListener( WindowClosingEventListener l ) {
		listenerList.remove( WindowClosingEventListener.class, l );
	}

	protected void fireRunEvent( RunEvent runEvent ) {
		Object[] listeners = listenerList.getListenerList();

		for ( int i = listeners.length - 2; i >= 0; i -= 2 ) {
			if ( listeners[ i ] == RunEventListener.class ) {
				( (RunEventListener) listeners[ i + 1 ] ).runEventFired( runEvent );
			}
		}
	}

	protected void firePreferencesEvent( PreferencesEvent preferencesEvent ) {
		Object[] listeners = listenerList.getListenerList();

		for ( int i = listeners.length - 2; i >= 0; i -= 2 ) {
			if ( listeners[ i ] == PreferencesEventListener.class ) {
				( (PreferencesEventListener) listeners[ i + 1 ] ).preferencesEventFired( preferencesEvent );
			}
		}
	}

	protected void fireWindowClosingEvent( WindowClosingEvent windowClosingEvent ) {
		Object[] listeners = listenerList.getListenerList();

		for ( int i = listeners.length - 2; i >= 0; i -= 2 ) {
			if ( listeners[ i ] == WindowClosingEventListener.class ) {
				( (WindowClosingEventListener) listeners[ i + 1 ] ).windowClosingEventFired( windowClosingEvent );
			}
		}
	}
}
