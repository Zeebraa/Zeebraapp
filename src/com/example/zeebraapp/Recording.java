package com.example.zeebraapp;

public class Recording {

	private String date;
	private String at;
	private int size;
	private int duration;
	private String path;
	private boolean isChecked;

	public Recording(String date, String at, int size, int duration, String path) {
		super();
		this.date = date;
		this.at = at;
		this.size = size;
		this.duration = duration;
		this.setPath(path);
		setChecked(false);
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getAt() {
		return at;
	}

	public void setAt(String at) {
		this.at = at;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

}
