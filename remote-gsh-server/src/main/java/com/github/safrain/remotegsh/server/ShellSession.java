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
