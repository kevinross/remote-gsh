package com.github.safrain.remotegsh.example;

import javax.annotation.Resource;
import java.util.Set;

public class PetStore {
	@Resource
	private Delivery delivery;

	private Set<Pet> pets;

	private boolean open;

	public Pet getById(int id) {
		for (Pet pet : pets) {
			if (pet.getId() == id) {
				return pet;
			}
		}
		return null;
	}

	public void deliverPet(Pet pet) {
		delivery.deliver(pet);
	}

	public void open() {
		open = true;
	}

	public void close() {
		open = false;
	}

	public Set<Pet> getPets() {
		return pets;
	}

	public void setPets(Set<Pet> pets) {
		this.pets = pets;
	}

	public boolean isOpen() {
		return open;
	}

}
