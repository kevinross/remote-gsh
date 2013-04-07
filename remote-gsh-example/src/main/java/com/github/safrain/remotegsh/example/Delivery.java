package com.github.safrain.remotegsh.example;

/**
 * @author safrain
 */
public class Delivery {
	public void deliver(Pet pet) {
		throw new HereIsABugException();
	}
}
