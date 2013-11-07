package com.mss.camdevbus.photo.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.mss.axa.photo.app.R;
import com.mss.devbus.photo.util.Util;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;

public class PhotoShutter extends Activity implements SensorEventListener {

	private static final String TAG = "PhotoShutter";

	private SensorManager mSensorManager;
	private Sensor mOrientationSensor;

	private Camera mCamera;
	private CameraPreview mPreview;
	private ImageButton btnFlash;
	private ImageButton btnSnapShot;
	private ImageButton btnGallery;
	private FrameLayout preview;
	public static File Directory;
	private TextView txtPicCount;
	private static String siniestro;
	private int count;
	String pathD;
	int cuenta = 0;
	List<String> itemList = new ArrayList<String>();

	private int numberOfCameras;

	// The first rear facing camera
	int defaultCameraId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.axa_photoshutter_layout);
		try {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

			Bundle extr = getIntent().getExtras();
			if (extr != null && extr.containsKey("siniestro")) {
				siniestro = extr.getString("siniestro");
			} else if (savedInstanceState != null) {
				if (savedInstanceState.containsKey("siniestro")) {
					siniestro = savedInstanceState.getString("siniestro");
					savedInstanceState.remove("siniestro");
				}
			}

			// path = Util.createTmpDir("AxaAM", siniestro + "/pics/");

			// Create a RelativeLayout container that will hold a SurfaceView,
			// and set it as the content of our activity.
			mPreview = new CameraPreview(this);
			preview = (FrameLayout) findViewById(R.id.c_preview);
			preview.addView(mPreview);

			btnFlash = (ImageButton) findViewById(R.id.btnFlash);
			btnFlash.setOnClickListener(flashClick);

			txtPicCount = (TextView) findViewById(R.id.txtPicCount);
			txtPicCount.setText(Integer.toString(cuenta));
			btnGallery = (ImageButton) findViewById(R.id.btnGallery);
			btnGallery.setOnClickListener(GalleryShow);

			btnSnapShot = (ImageButton) findViewById(R.id.btnSnapshot);
			btnSnapShot.setOnClickListener(snapShotClick);

			// Find the total number of cameras available
			numberOfCameras = Camera.getNumberOfCameras();

			// Find the ID of the default camera
			CameraInfo cameraInfo = new CameraInfo();
			for (int i = 0; i < numberOfCameras; i++) {
				Camera.getCameraInfo(i, cameraInfo);
				if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
					defaultCameraId = i;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "ERROR PhotoShutter:\n" + e.getMessage());
		}
	}

	public void contarFoto() {
		File[] list = Directory.listFiles();

		for (File f : list) {
			String name = f.getName();
			if (name.endsWith(".jpg") || name.endsWith(".JPG"))
				cuenta++;
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		try {
			outState.putString("siniestro", siniestro);
			mCamera.release();
		} catch (Exception e) {

		}
	}
	
	

	@Override
	public void onBackPressed() {
		getIntent().removeExtra("UPLOAD_PICS");
		super.onBackPressed();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_NORMAL);
		// Open the default i.e. the first rear facing camera.
		mCamera = Camera.open();

		mPreview.setCamera(mCamera);

	}

	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
		// Because the Camera object is a shared resource, it's very
		// important to release it when the activity is paused.
		if (mCamera != null) {
			mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;

		}
	}

	// Llamada a galeria
	private OnClickListener GalleryShow = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Gallery();
		}

	};

	private void Gallery() {
		Bundle ext = getIntent().getExtras();
		String action = "";
		if (ext != null && ext.containsKey("action_caller")) {
			action = ext.getString("action_caller");
		}
		Intent i = new Intent(this, GalleryA.class);
		i.putExtra("siniestro", siniestro);
		i.putExtra("action_caller", action);
		startActivity(i);
		mCamera.stopPreview();
		finish();
		// Toast.makeText(getApplicationContext(), "ERROR",
		// Toast.LENGTH_LONG).show();
	}

	private OnClickListener snapShotClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mPreview.mCamera.takePicture(null, null, jpegCallback);

			addPicCount();

		}
	};

	/** Handles data for jpeg picture */
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {

			File pictureFile = getOutputMediaFile();
			if (pictureFile == null) {
				return;
			}
			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.flush();
				fos.close();

			} catch (FileNotFoundException e) {
				// e.printStackTrace();
			} catch (IOException e) {
				// e.printStackTrace();
			}
			Toast.makeText(PhotoShutter.this, "Image Saved: " + Directory, Toast.LENGTH_SHORT).show();
			mCamera.stopPreview();
			mCamera.startPreview();
		}
	};

	private static File getOutputMediaFile() {

		Directory = Util.createTmpDir("Axa", siniestro + "/Gallery/");
		if (!Directory.exists()) {
			if (!Directory.mkdirs()) {
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}
		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		File mediaFile = new File(Directory.getPath() + File.separator + "IMG_"
				+ timeStamp + ".jpg");

		return mediaFile;
	}

	private OnClickListener flashClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			enableFlash();
		}
	};

	private void addPicCount() {
		try {
			if (txtPicCount.getVisibility() != View.VISIBLE) {
				txtPicCount.setVisibility(View.VISIBLE);
			}
			if (count < 9) {
				txtPicCount.setPadding(13, 5, 0, 0);
			} else {
				txtPicCount.setPadding(8, 5, 0, 0);
			}
			txtPicCount.setText(Util.integerToString(++count));
		} catch (Exception e) {
			Log.e(TAG, "ERROR PhotoShutter - addPicCount:\n" + e.getMessage());
		}
	}

	private void enableFlash() {
		try {
			Parameters p = mCamera.getParameters();
			if (p.getFlashMode().equalsIgnoreCase(Parameters.FLASH_MODE_TORCH)) {
				p.setFlashMode(Parameters.FLASH_MODE_OFF);
			} else {
				p.setFlashMode(Parameters.FLASH_MODE_TORCH);
			}
			mCamera.setParameters(p);
		} catch (Exception e) {
			Log.e(TAG, "ERROR PhotoShutter - enableFlash:\n" + e.getMessage());
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event != null) {

			float pitch = event.values[2];
			if (pitch <= 45 && pitch >= -45) {
				// vertical
			} else if ((pitch < -45) || (pitch > 45)) {
				// side up
			}
		}
	}

}
