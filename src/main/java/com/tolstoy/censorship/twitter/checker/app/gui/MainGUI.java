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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.EventListenerList;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import com.tolstoy.basic.api.statusmessage.IStatusMessageReceiver;
import com.tolstoy.basic.api.statusmessage.StatusMessage;
import com.tolstoy.basic.api.statusmessage.StatusMessageSeverity;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.gui.ElementDescriptor;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;

public class MainGUI implements ActionListener, IStatusMessageReceiver {
	public static final String ACTION_REPLIES = "replies";
	public static final String ACTION_TIMELINE = "timeline";
	public static final String ACTION_REWRITE_LAST_REPORT = "rewrite";

	private final EventListenerList listenerList = new EventListenerList();
	private final IPreferences prefs;
	private final IResourceBundleWithFormatting bundle;
	private final java.util.List<ElementDescriptor> descriptors;
	private JFrame frame;
	private JButton btnPreferences, btnRewriteLastReport, btnRunItinerary, btnCheckTimeline;
	private JEditorPane editorPaneStatusMessage;

	@Override
	public void addMessage( final StatusMessage message ) {
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
			final HTMLDocument document = (HTMLDocument) editorPaneStatusMessage.getDocument();
			final HTMLEditorKit editorKit = (HTMLEditorKit) editorPaneStatusMessage.getEditorKit();
			editorKit.insertHTML( document, document.getLength(), html, 0, 0, null );
			//document.insertString( document.getLength(), html, null );

			editorPaneStatusMessage.setCaretPosition( document.getLength() );
		}
		catch ( final Exception e ) {
		}
	}

	@Override
	public void clearMessages() {
		editorPaneStatusMessage.setText( "" );
	}

	public MainGUI( final IResourceBundleWithFormatting bundle, final IPreferences prefs, final java.util.List<ElementDescriptor> descriptors ) {
		this.bundle = bundle;
		this.prefs = prefs;
		this.descriptors = descriptors;
	}

	public void showGUI() {
		frame = new JFrame( bundle.getString( "main_title" ) );
		frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout( new FlowLayout() );

		btnPreferences = new JButton( bundle.getString( "main_prefs_button" ) );
		btnPreferences.addActionListener( this );
		buttonPanel.add( btnPreferences );

		btnRewriteLastReport = new JButton( bundle.getString( "main_rewrite_button" ) );
		btnRewriteLastReport.addActionListener( this );
		buttonPanel.add( btnRewriteLastReport );

		btnRunItinerary = new JButton( bundle.getString( "main_check_itinerary_button" ) );
		btnRunItinerary.setToolTipText( bundle.getString( "main_check_itinerary_button_help" ) );
		btnRunItinerary.addActionListener( this );
		buttonPanel.add( btnRunItinerary );

		btnCheckTimeline = new JButton( bundle.getString( "main_check_timeline_button" ) );
		btnCheckTimeline.setToolTipText( bundle.getString( "main_check_timeline_button_help" ) );
		btnCheckTimeline.addActionListener( this );
		buttonPanel.add( btnCheckTimeline );

		frame.getContentPane().add( buttonPanel, BorderLayout.PAGE_START );

		editorPaneStatusMessage = new JEditorPane();
		editorPaneStatusMessage.setEditorKit( new HTMLEditorKit() );
		editorPaneStatusMessage.setContentType( "text/html" );
		editorPaneStatusMessage.setEditable( false );

		final JScrollPane scrollPane = new JScrollPane( editorPaneStatusMessage );
		scrollPane.setPreferredSize( new Dimension( 480, 350 ) );
		scrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		scrollPane.setBorder( new BevelBorder( BevelBorder.LOWERED ) );

		frame.getContentPane().add( scrollPane, BorderLayout.CENTER );

		frame.addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing( final WindowEvent windowEvent ) {
				fireWindowClosingEvent( new WindowClosingEvent( this, windowEvent.getWindow() ) );
			}
		});

		frame.pack();
		frame.setVisible( true );
	}

	public void enableRunFunction( final boolean state ) {
		btnCheckTimeline.setEnabled( state );
		btnRewriteLastReport.setEnabled( state );
		btnRunItinerary.setEnabled( state );
	}

	public void enablePreferencesFunction( final boolean state ) {
		btnPreferences.setEnabled( state );
	}

	@Override
	public void actionPerformed( final ActionEvent actionEvent ) {
		if ( actionEvent.getSource() == btnPreferences ) {
			final PreferencesDialog preferencesDialog = new PreferencesDialog( frame, bundle, descriptors, prefs.getValues() );
			preferencesDialog.setVisible( true );
			final java.util.Map<String,String> userdata = preferencesDialog.getUserdata();
			if ( userdata != null ) {
				firePreferencesEvent( new PreferencesEvent( this, userdata ) );
			}
		}
		else if ( actionEvent.getSource() == btnCheckTimeline ) {
			fireRunEvent( new RunEvent( this, ACTION_TIMELINE ) );
		}
		else if ( actionEvent.getSource() == btnRewriteLastReport ) {
			fireRunEvent( new RunEvent( this, ACTION_REWRITE_LAST_REPORT ) );
		}
		else if ( actionEvent.getSource() == btnRunItinerary ) {
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory( new File( System.getProperty( "user.home" ) ) );
			if ( fileChooser.showOpenDialog( frame ) == JFileChooser.APPROVE_OPTION ) {
				fireRunItineraryEvent( new RunItineraryEvent( this, fileChooser.getSelectedFile() ) );
			}
		}
	}

	public void addRunEventListener( final RunEventListener l ) {
		listenerList.add( RunEventListener.class, l );
	}

	public void removeRunEventListener( final RunEventListener l ) {
		listenerList.remove( RunEventListener.class, l );
	}

	public void addPreferencesEventListener( final PreferencesEventListener l ) {
		listenerList.add( PreferencesEventListener.class, l );
	}

	public void removePreferencesEventListener( final PreferencesEventListener l ) {
		listenerList.remove( PreferencesEventListener.class, l );
	}

	public void addRunItineraryEventListener( final RunItineraryEventListener l ) {
		listenerList.add( RunItineraryEventListener.class, l );
	}

	public void removeItineraryRunEventListener( final RunItineraryEventListener l ) {
		listenerList.remove( RunItineraryEventListener.class, l );
	}

	public void addWindowClosingEventListener( final WindowClosingEventListener l ) {
		listenerList.add( WindowClosingEventListener.class, l );
	}

	public void removeWindowClosingEventListener( final WindowClosingEventListener l ) {
		listenerList.remove( WindowClosingEventListener.class, l );
	}

	protected void fireRunEvent( final RunEvent runEvent ) {
		final Object[] listeners = listenerList.getListenerList();

		for ( int i = listeners.length - 2; i >= 0; i -= 2 ) {
			if ( listeners[ i ] == RunEventListener.class ) {
				( (RunEventListener) listeners[ i + 1 ] ).runEventFired( runEvent );
			}
		}
	}

	protected void firePreferencesEvent( final PreferencesEvent preferencesEvent ) {
		final Object[] listeners = listenerList.getListenerList();

		for ( int i = listeners.length - 2; i >= 0; i -= 2 ) {
			if ( listeners[ i ] == PreferencesEventListener.class ) {
				( (PreferencesEventListener) listeners[ i + 1 ] ).preferencesEventFired( preferencesEvent );
			}
		}
	}

	protected void fireRunItineraryEvent( final RunItineraryEvent runItineraryEvent ) {
		final Object[] listeners = listenerList.getListenerList();

		for ( int i = listeners.length - 2; i >= 0; i -= 2 ) {
			if ( listeners[ i ] == RunItineraryEventListener.class ) {
				( (RunItineraryEventListener) listeners[ i + 1 ] ).runItineraryEventFired( runItineraryEvent );
			}
		}
	}

	protected void fireWindowClosingEvent( final WindowClosingEvent windowClosingEvent ) {
		final Object[] listeners = listenerList.getListenerList();

		for ( int i = listeners.length - 2; i >= 0; i -= 2 ) {
			if ( listeners[ i ] == WindowClosingEventListener.class ) {
				( (WindowClosingEventListener) listeners[ i + 1 ] ).windowClosingEventFired( windowClosingEvent );
			}
		}
	}
}
