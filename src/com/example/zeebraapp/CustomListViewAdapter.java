package com.example.zeebraapp;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class CustomListViewAdapter extends ArrayAdapter<Recording> {

	private Context context;
	private ArrayList<Recording> recordings;

	public ArrayList<Recording> getRecordings() {
		return recordings;
	}

	public CustomListViewAdapter(Context context, int resourceId,
			List<Recording> items) {
		super(context, resourceId, items);
		this.context = context;
		this.recordings = new ArrayList<Recording>();
		this.recordings.addAll(items);

	}

	/* private view holder class */
	private class ViewHolder {
		CheckBox checkBox;
		TextView txtAt;
		TextView txtDate;
		TextView txtDuration;
		TextView txtSize;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		Recording rowItem = getItem(position);

		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_adapter, null);
			holder = new ViewHolder();
			holder.txtDate = (TextView) convertView.findViewById(R.id.date);
			holder.txtAt = (TextView) convertView.findViewById(R.id.at);
			holder.txtDuration = (TextView) convertView
					.findViewById(R.id.duration);
			holder.txtSize = (TextView) convertView.findViewById(R.id.size);

			holder.checkBox = (CheckBox) convertView.findViewById(R.id.chk);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
			holder.checkBox.setOnCheckedChangeListener(null);
			holder.checkBox.setChecked(recordings.get(position).isChecked());
		}

		holder.checkBox.setTag(rowItem);
		String month = rowItem.getDate().substring(0,
				rowItem.getDate().indexOf("/"));
		int monthInt = Integer.parseInt(month);

		String day = rowItem.getDate().substring(
				rowItem.getDate().indexOf("/") + 1,
				rowItem.getDate().indexOf("/",
						rowItem.getDate().indexOf("/") + 1));
		if (day.charAt(0) == '0') {
			day = day.substring(1);
		}
		String year = rowItem.getDate().substring(
				rowItem.getDate().length() - 4);

		holder.txtDate.setText(getMonth(monthInt) + " " + day + ", " + year);
		holder.txtAt.setText("at " + rowItem.getAt());
		int minutes = (int) (rowItem.getDuration() / 60);
		int seconds = rowItem.getDuration() % 60;

		holder.txtDuration.setText((minutes > 9 ? minutes : ("0" + minutes))
				+ ":" + (seconds > 9 ? seconds : "0" + seconds));
		holder.txtSize.setText(Integer.toString(rowItem.getSize()) + "kB");

		holder.checkBox.setOnCheckedChangeListener((RecorderActivity) context);

		String fileName = rowItem.getPath().substring(
				rowItem.getPath().lastIndexOf('/') + 1);
		if (fileName.length() > 10) {
			if (!fileName.substring(0, 10).equals(new String("recording-"))) {
				holder.txtAt.setText(getMonth(monthInt) + " " + day + ", "
						+ year + ", at " + rowItem.getAt());
				holder.txtDate.setText(fileName);
			}
		} else {
			holder.txtAt.setText(getMonth(monthInt) + " " + day + ", " + year
					+ " at " + rowItem.getAt());
			holder.txtDate.setText(fileName);
		}
		return convertView;
	}

	public String getMonth(int month) {
		return new DateFormatSymbols().getMonths()[month - 1];
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int arg0) {
		return true;
	}

}
