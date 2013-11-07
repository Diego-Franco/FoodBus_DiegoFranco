package com.mss.camdevbus.photo.app;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.mss.axa.photo.app.R;
import com.mss.devbus.photo.util.Util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;

public class GalleryA extends Activity {

	private Intent intent;
	private ArrayList<String> itemList = new ArrayList<String>();
	private ArrayList<String> selectedItems = new ArrayList<String>();
	private ImageAdapter myImageAdapter;
	private String siniestro;
	private String action;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Bundle extr = getIntent().getExtras();
		if (extr != null) {
			if (extr.containsKey("siniestro")) {
				siniestro = extr.getString("siniestro");
			}
			if (extr.containsKey("action_caller")) {
				action = extr.getString("action_caller");
			}

		} else if (savedInstanceState != null) {
			if (savedInstanceState.containsKey("siniestro")) {
				siniestro = savedInstanceState.getString("siniestro");
				savedInstanceState.remove("siniestro");
			}
		}

		intent = getIntent();
		final GridView gridview = (GridView) findViewById(R.id.gridview);
		gridview.setClickable(true);
		myImageAdapter = new ImageAdapter();
		gridview.setAdapter(myImageAdapter);

		// Path Directory

		File targetDirector = Util.createTmpDir("Axa", siniestro + "/Gallery/");

		File[] files = targetDirector.listFiles();
		for (File file : files) {

			myImageAdapter.add(file.getAbsolutePath());

		}

	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		try {
			outState.putString("siniestro", siniestro);
		} catch (Exception e) {

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Enviar fotografias");
		return true;

	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		try {
			Intent itt = new Intent("com.mss.axa.AM");
			itt.setClassName("com.mss.axa.app", action);
			itt.putExtra("UPLOAD_PICS", true);
			itt.putExtra("pictures", selectedItems);
			startActivity(itt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public class ImageAdapter extends BaseAdapter {

		void add(String path) {
			itemList.add(path);
		}

		@Override
		public int getCount() {
			return itemList.size();
		}

		@Override
		public Object getItem(int position) {
			return 0;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = null;
			try {
				String path = itemList.get(position);
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.galleryitem, null);
				ImageView imgView = (ImageView) v.findViewById(R.id.thumbImage);
				CheckBox ch = (CheckBox) v.findViewById(R.id.itemCheckBox);
				ch.setTag(path);
				ch.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Object o = v.getTag();
						if (o != null && o instanceof String) {
							String pth = (String) o;
							if (pth != null) {
								if (!selectedItems.contains(pth)) {
									selectedItems.add(pth);
								} else {
									selectedItems.remove(pth);
								}
							}
						}
					}
				});
				Bitmap bm = decodeSampledBitmapFromUri(path, 180, 150);
				imgView.setImageBitmap(bm);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return v;
		}

		public Bitmap decodeSampledBitmapFromUri(String path, int reqWidth, int reqHeight) {

			Bitmap bm = null;
			// First decode with inJustDecodeBounds=true to check dimensions
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, options);

			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			bm = BitmapFactory.decodeFile(path, options);

			return bm;
		}

		public int calculateInSampleSize(

		BitmapFactory.Options options, int reqWidth, int reqHeight) {
			// Raw height and width of image
			final int height = options.outHeight;
			final int width = options.outWidth;
			int inSampleSize = 1;

			if (height > reqHeight || width > reqWidth) {
				if (width > height) {
					inSampleSize = Math.round((float) height
							/ (float) reqHeight);
				} else {
					inSampleSize = Math.round((float) width / (float) reqWidth);
				}
			}

			return inSampleSize;
		}

	}

}
