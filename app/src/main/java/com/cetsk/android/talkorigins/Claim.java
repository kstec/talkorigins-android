package com.cetsk.android.talkorigins;

import java.io.Serializable;
import java.util.ArrayList;

public class Claim implements Serializable {

	private static final long serialVersionUID = 1L;
	private int Id = 0;
	private String key = null;
	private String name = null;
	private String url = null;
	private int parentId = 0;
	private static String BASE_URL = "http://www.talkorigins.org/indexcc/";
	private ArrayList<Claim> children = null;

	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return this.key;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return this.url;
	}

	public void setChildren(ArrayList<Claim> children) {
		this.children = children;
	}

	public void addChild(Claim claim) {
		this.children.add(claim);
	}

	public ArrayList<Claim> getChildren() {
		return this.children;
	}

	@Override
	public String toString() {
		return "Claim [Id=" + Id + ", key=" + key + ", name=" + name + ", url=" + url + ", parentId=" + parentId + "]";
	}

	public void setId(int id) {
		Id = id;
	}

	public int getId() {
		return Id;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public int getParentId() {
		return parentId;
	}

	public String getFullUrl() {
		return BASE_URL + this.url;
	}

}
