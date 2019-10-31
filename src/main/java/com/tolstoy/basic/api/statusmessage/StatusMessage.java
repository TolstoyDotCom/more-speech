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
package com.tolstoy.basic.api.statusmessage;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class StatusMessage {
	private final StatusMessageSeverity severity;
	private final String message;

	public StatusMessage( final String message, final StatusMessageSeverity severity ) {
		this.message = message;
		this.severity = severity;
	}

	public String getMessage() {
		return message;
	}

	public StatusMessageSeverity getSeverity() {
		return severity;
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.append( "message", message )
		.append( "severity", severity )
		.toString();
	}
}
