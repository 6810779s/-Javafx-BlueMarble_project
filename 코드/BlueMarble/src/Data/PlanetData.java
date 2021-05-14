package Data;

import java.util.ArrayList;

public class PlanetData {
	String name;
	String owner;
	String data;
	int price;
	ArrayList<Object> building;
	public int count;
	public PlanetData(String name, String owner, String data, int price, int count) {
		building = new ArrayList<Object>();
		this.name = name;
		this.owner = owner;
		this.data = data;
		this.price = price;		
		this.count = count;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public ArrayList<Object> getBuilding() {
		return building;
	}
	public void setBuilding(ArrayList<Object> building) {
		this.building = building;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}	
	
	
}
