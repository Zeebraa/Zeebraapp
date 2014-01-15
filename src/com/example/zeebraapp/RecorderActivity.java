package com.example.zeebraapp;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ListFragment;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

public class RecorderActivity extends FragmentActivity implements
		ActionBar.TabListener,
		android.widget.CompoundButton.OnCheckedChangeListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	private SectionsPagerAdapter sectionsPagerAdapter;
	private CustomListViewAdapter listAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager viewPager;

	private ActionMode actionMode = null;
	private ShareActionProvider shareActionProvider;
	private MyListFragment listFragment = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recorder);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		sectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(sectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		viewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < sectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(sectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.recorder, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		viewPager.setCurrentItem(tab.getPosition());

	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.

			Fragment fragment = null;

			switch (position) {
			case 0:
				fragment = new MyRecorderFragment();
				break;
			case 1:
				fragment = new MyListFragment();
				listFragment = (MyListFragment) fragment;
				break;

			}
			return fragment;

		}

		@Override
		public int getCount() {
			// Show 2 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getResources().getText(R.string.tab_recorder);
			case 1:
				return getResources().getText(R.string.tab_player);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	@SuppressLint("SimpleDateFormat")
	public static class MyRecorderFragment extends Fragment {
		
		private static final String LOG_TAG = "RecorderActivity";
		private static String mFileName = null;
		private MediaRecorder mRecorder = null;
		private RecordButton mRecordButton = null;
		private Button stopButton = null;
		private Handler waveHandler = null;
		private Boolean recording = false;
		private VisualizerView visualizer = null;
		private TextView timeTextView = null;
		private Timer t = null;
		private int minute = 0, seconds = 0, hour = 0;
		private Drawable recordImg;
		private ImageView imgView;
		boolean pausedRecording = false;

		

		private void onRecord(boolean start) {
			if (start) {
				startRecording();
			} else {
				stopRecording();
			}
		}

		private void startRecording() {
			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

			int index = mFileName.lastIndexOf('/');
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
			String currentDateandTime = sdf.format(new Date());
			mFileName = mFileName.substring(0, index + 1) + "recording-"
					+ currentDateandTime + ".3gp";

			mRecorder.setOutputFile(mFileName);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

			try {
				mRecorder.prepare();
			} catch (IOException e) {
				Log.e(LOG_TAG, "prepare() failed");
			}

			mRecorder.start();
			recording = true;
			if (mRecordButton.getText() == "Record") {
				visualizer.clear();
				resetTimer();
			}
			waveHandler.post(updateVisualizer);
		}

		private void stopRecording() {
			if (mRecorder != null) {
				if (pausedRecording) {
					
					/*
					 * TODO:Add Pause and Resume options
					 */
					Toast.makeText(getActivity(), "Merge Files",
							Toast.LENGTH_LONG).show();
					pausedRecording = false;

				}

				mRecorder.stop();
				mRecorder.release();
				mRecorder = null;

			}

			recording = false;
		}

		class RecordButton extends Button {
			boolean mStartRecording = true;

			OnClickListener clicker = new OnClickListener() {
				public void onClick(View v) {
					

					if (mStartRecording) {
						onRecord(mStartRecording);
						setText("Pause");
						Drawable pauseOff = getResources().getDrawable(
								R.drawable.pause_off);
						setCompoundDrawablesWithIntrinsicBounds(pauseOff, null,
								null, null);
						imgView.setImageResource(R.drawable.zeebraa_web);

						startTimer();
					} else {
						pausedRecording = true;
						onRecord(mStartRecording);
						setText("Resume");
						setCompoundDrawablesWithIntrinsicBounds(recordImg, null,
								null, null);
						imgView.setImageResource(R.drawable.zeebraa_web_not);

						stopTimer();
					}
					mStartRecording = !mStartRecording;
				}
			};

			public RecordButton(Context ctx) {
				super(ctx);
				setText("Record");
				setOnClickListener(clicker);
			}
		}

		@SuppressWarnings("deprecation")
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			
			File folder = new File(Environment.getExternalStorageDirectory()
					+ "/" + getResources().getText(R.string.folder_name));
			if (!folder.exists()) {
				folder.mkdir();
			}
			

			mFileName = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/" + getResources().getText(R.string.folder_name);
			mFileName += "/first.3gp";
			

			waveHandler = new Handler();

			View rootView = inflater.inflate(R.layout.recorder_fragment,
					container, false);
			timeTextView = (TextView) rootView.findViewById(R.id.time);
			timeTextView.setText("00:00:00");

			
			LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.ll);
			mRecordButton = new RecordButton(getActivity());
			mRecordButton.setBackgroundResource(R.drawable.recorder_button);
			mRecordButton.setTextColor(getResources().getColor(
					R.color.white_text_color));

			recordImg = getResources().getDrawable(R.drawable.record);
			Drawable stopOff = getResources().getDrawable(R.drawable.stop_off);

			mRecordButton.setCompoundDrawablesWithIntrinsicBounds(recordImg, null,
					null, null);

			stopButton = new Button(getActivity());
			stopButton.setText(getResources().getText(R.string.stop_button));
			stopButton.setTextColor(getResources().getColor(
					R.color.white_text_color));

			stopButton.setBackgroundResource(R.drawable.stop_button);
			stopButton.setCompoundDrawablesWithIntrinsicBounds(stopOff, null,
					null, null);

			stopButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					pausedRecording = false;
					onRecord(false);
					mRecordButton.mStartRecording = true;

					mRecordButton.setText(getResources().getText(R.string.record_button));
					mRecordButton.setCompoundDrawablesWithIntrinsicBounds(
							recordImg, null, null, null);
					imgView.setImageResource(R.drawable.zeebraa_web_not);

					stopTimer();
					// resetTimer();
				}
			});

			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT, 1);

			layoutParams.setMargins(0, 0, 10, 0);

			ll.addView(mRecordButton, layoutParams);
			ll.addView(stopButton, new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT, 1));

			visualizer = (VisualizerView) rootView
					.findViewById(R.id.visualizerView);

			imgView = (ImageView) rootView.findViewById(R.id.zeebraaLogo);
			imgView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mRecordButton.performClick();

				}
			});

			return rootView;
		}

		private void startTimer() {
			t = new Timer("timer", true);
			t.schedule(new TimerTask() {

				@Override
				public void run() {
					timeTextView.post(updateTimer);

				}
			}, 0, 1000);

		}

		private void stopTimer() {
			if (t != null) {
				t.cancel();
			}

		}

		private void resetTimer() {
			seconds = 0;
			minute = 0;
			hour = 0;
		}

		Runnable updateTimer = new Runnable() {

			public void run() {
				timeTextView.setText("" + (hour > 9 ? hour : ("0" + hour))
						+ ":" + (minute > 9 ? minute : ("0" + minute)) + ":"
						+ (seconds > 9 ? seconds : "0" + seconds));
				seconds++;
				if (seconds == 60) {
					seconds = 0;
					minute++;
				}
				if (minute == 60) {
					minute = 0;
					hour++;
				}

			}
		};

		Runnable updateVisualizer = new Runnable() {
			@Override
			public void run() {
				if (recording) // if we are already recording
				{
					// get the current amplitude
					int x = mRecorder.getMaxAmplitude();
					visualizer.addAmplitude(x); // update the VisualizeView +1
												// to always have a stripe
					visualizer.invalidate();
					waveHandler.postDelayed(this, 50);

				} // end if
			} // end method run
		}; // end Runnable

	}

	@SuppressLint("ValidFragment")
	public class MyListFragment extends ListFragment {

		private MediaPlayer mediaPlayer = null;
		private boolean playing = false;
		private Recording currentRecord;
		private SeekBar seekBar;
		private ImageButton playButton;
		private String lastItem;
		private int selectedItem = 0;
		private ListView listView;
		private boolean firstPlay = true;
		private boolean secondPlay = false;

		private Handler seekBarHandler = new Handler();
		private Runnable seekBarRunnable = new Runnable() {

			@Override
			public void run() {
				if (mediaPlayer != null) {
					int mCurrentPosition = mediaPlayer.getCurrentPosition();
					seekBar.setProgress(mCurrentPosition);
				}
				seekBarHandler.postDelayed(this, 50);
			}
		};

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
		
			View view = inflater.inflate(R.layout.list_fragment, null);

			return view;

		}

		@Override
		public void setUserVisibleHint(boolean isVisibleToUser) {
			super.setUserVisibleHint(isVisibleToUser);

			// Make sure that we are currently visible
			if (this.isVisible()) {
				// If we are becoming invisible, then...

				refreshList();
				seekBar = (SeekBar) findViewById(R.id.playSeekBar);
				playButton = (ImageButton) findViewById(R.id.playButton);
				listView = (ListView) findViewById(android.R.id.list);
				listView.setItemChecked(0, true);

				playButton.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						currentRecord = (Recording) getListAdapter().getItem(
								selectedItem);
						String item = currentRecord.getPath();
						if (firstPlay) {
							listView.getChildAt(listView.getCheckedItemPosition())
									.setBackgroundColor(
											getResources().getColor(
													R.color.pressed_color));
							secondPlay = true;
							lastItem = item;
						}

						if (mediaPlayer != null) {
							if (mediaPlayer.isPlaying()) {
								pausePlaying();
							} else {
								startPlaying(item);

							}
						} else {
							startPlaying(item);
						}
					}
				});

				seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						if (mediaPlayer != null && fromUser) {
							mediaPlayer.seekTo(progress);
						}
					}
				});

			}

		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

		}

		private void startPlaying(String fileName) {

			if (mediaPlayer != null) {
				mediaPlayer.start();
				playButton.setImageResource(android.R.drawable.ic_media_pause);

			} else {

				mediaPlayer = new MediaPlayer();
				try {
					mediaPlayer.setDataSource(fileName);
					mediaPlayer.prepare();
					mediaPlayer.start();
					playing = true;
					seekBar.setMax(mediaPlayer.getDuration());

					seekBarHandler.post(seekBarRunnable);
					playButton
							.setImageResource(android.R.drawable.ic_media_pause);

					mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

						@Override
						public void onCompletion(MediaPlayer mp) {
							playButton
									.setImageResource(android.R.drawable.ic_media_play);
							stopPlaying();

						}
					});
				} catch (IOException e) {
					Log.e("RecorderActivity", "prepare() failed");
				}
			}
		}

		private void pausePlaying() {
			mediaPlayer.pause();
			playButton.setImageResource(android.R.drawable.ic_media_play);

		}

		private void stopPlaying() {
			if (mediaPlayer != null) {
				mediaPlayer.release();
			}
			mediaPlayer = null;
			playing = false;
		}

		@SuppressLint("ResourceAsColor")
		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			if (secondPlay) {
				listView.getChildAt(0).setBackgroundColor(Color.TRANSPARENT);
				secondPlay = false;
			}
			firstPlay = false;
			v.setSelected(true);

			selectedItem = position;

			currentRecord = (Recording) getListAdapter().getItem(position);
			String item = currentRecord.getPath();
			if (mediaPlayer != null) {
				playing = mediaPlayer.isPlaying();
			}

			if (lastItem != null) {
				if (item.equals(lastItem)) {
					if (playing) {
						pausePlaying();
					} else {
						startPlaying(item);
					}
				} else {

					stopPlaying();
					startPlaying(item);

				}
			} else {
				startPlaying(item);
			}
			lastItem = item;

		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		Recording recording = (Recording) buttonView.getTag();

		recording.setChecked(isChecked);

		int counter = 0;

		for (int i = 0; i < listAdapter.getRecordings().size(); i++) {

			if (listAdapter.getRecordings().get(i).isChecked())
				counter++;

		}

		if (counter > 0) {
			actionMode = RecorderActivity.this
					.startActionMode(new ActionBarCallBack());

			if (counter > 1) {
				actionMode.setTitle(Integer.toString(counter));
				MenuItem item = actionMode.getMenu().findItem(R.id.item_edit);
				item.setVisible(false);
				this.invalidateOptionsMenu();
			}

		} else {
			actionMode.finish();
		}

		MenuItem item = actionMode.getMenu().findItem(R.id.item_share);

		shareActionProvider = (ShareActionProvider) item.getActionProvider();
		shareActionProvider.setShareIntent(getDefaultIntent());

	}

	private Intent getDefaultIntent() {
		Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_SUBJECT, "My track on Zeebraapp");

		ArrayList<Uri> files = new ArrayList<Uri>();
		for (int i = 0; i < listAdapter.getRecordings().size(); i++) {

			if (listAdapter.getRecordings().get(i).isChecked()) {
				Uri uri = Uri.fromFile(new File(listAdapter.getRecordings().get(i)
						.getPath()));
				files.add(uri);
				intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
				// intent.putExtra(Intent.EXTRA_STREAM, uri);
				// break;
			}

		}
		return intent;
	}

	public void refreshList() {
		File dir = new File(Environment.getExternalStorageDirectory()
				+ "/" + getResources().getText(R.string.folder_name));
		File[] filelist = dir.listFiles();
		List<Recording> Recordings = new Vector<Recording>();

		MediaMetadataRetriever retriever = new MediaMetadataRetriever();

		for (int i = 0; i < filelist.length; i++) {
			retriever.setDataSource(filelist[i].getAbsolutePath());
			String time = retriever
					.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			long timeInmillisec = Long.parseLong(time);
			long length = timeInmillisec / 1000;
			long hours = length / 3600;
			long minutes = (length - hours * 3600) / 60;
			int seconds = (int) (length - (hours * 3600 + minutes * 60));

			int duration = seconds;
			long lastTime = filelist[i].lastModified();
			String fullDate = DateFormat.format("MM/dd/yyyy;HH:mm",
					new Date(lastTime)).toString();
			String date = fullDate.substring(0, fullDate.indexOf(";"));
			int size = (int) (filelist[i].length() / 1024);
			String at = fullDate.substring(fullDate.indexOf(";") + 1);
			String path = filelist[i].getAbsolutePath();

			Recordings.add(new Recording(date, at, size, duration, path));
		}

		listAdapter = new CustomListViewAdapter(this, R.layout.list_adapter,
				Recordings);

		listFragment.setListAdapter(listAdapter);

		if (actionMode != null)
			actionMode.finish();

	}

	class ActionBarCallBack implements ActionMode.Callback {

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			switch (item.getItemId()) {
			case R.id.item_delete:
				for (int i = 0; i < listAdapter.getRecordings().size(); i++) {

					if (listAdapter.getRecordings().get(i).isChecked()) {
						File toDelete = new File(listAdapter.getRecordings().get(i)
								.getPath());
						toDelete.delete();

					}
				}
				refreshList();
				listAdapter.notifyDataSetChanged();

				mode.finish(); // Action picked, so close the CAB
				return true;
			case R.id.item_edit:
				for (int i = 0; i < listAdapter.getRecordings().size(); i++) {

					if (listAdapter.getRecordings().get(i).isChecked()) {

						showRenameDialog(listAdapter.getRecordings().get(i)
								.getPath());

					}
				}
				mode.finish(); // Action picked, so close the CAB
				return true;
			case R.id.item_upload:
				/* TODO: upload to zeebraa */
				showZeebraaToast();
				return true;

			default:
				return false;
			}

		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.getMenuInflater().inflate(R.menu.contextual_menu, menu);
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {

		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

			return false;
		}
	}

	private void showZeebraaToast() {
		Toast.makeText(this, "Files have been uploaded to Zeebraa",
				Toast.LENGTH_LONG).show();
	}

	private void showRenameDialog(final String path) {
		AlertDialog.Builder renameDialog = new AlertDialog.Builder(this);
		renameDialog.setTitle(getResources().getText(R.string.rename_dialog));
		final EditText newName = new EditText(this);
		String fileName = path.substring(path.lastIndexOf('/') + 1);
		final String fileDir = path.substring(0, path.lastIndexOf('/') + 1);
		newName.setText(fileName);
		newName.selectAll();

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

		renameDialog.setView(newName);

		renameDialog.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface arg0, int arg1) {

						File renameFrom = new File(path);
						File renameTo = new File(fileDir + newName.getText());

						renameFrom.renameTo(renameTo);
						refreshList();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.toggleSoftInput(
								InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

					}
				});

		renameDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface arg0, int arg1) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.toggleSoftInput(
								InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
					}
				});
		renameDialog.show();
	}
}
