/*
 * Remote Groovy Shell    A servlet web application management tool
 * Copyright (c)          2013 Safrain <z.safrain@gmail.com>
 *                        All Rights Reserved
 *
 * This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://www.wtfpl.net/ for more details.
 */

package com.github.safrain.remotegsh.server;

import javax.script.ScriptEngine;

/**
 * A shell session
 *
 * @author safrain
 */
public class ShellSession {
	/**
	 * Sid held by the client
	 */
	private String id;
	/**
	 * The associate script context with this session
	 */
	private ScriptEngine engine;
	/**
	 * Last access time
	 */
	private long lastAccessTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ScriptEngine getEngine() {
		return engine;
	}

	public void setEngine(ScriptEngine engine) {
		this.engine = engine;
	}

	public long getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(long lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

}
