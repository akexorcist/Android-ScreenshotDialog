package app.akexorcist.screenshotdialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class Main extends Activity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		ImageView imgDialog = (ImageView)findViewById(R.id.imgDialog);
		imgDialog.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final Dialog dialog = new Dialog(Main.this);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.customdialog);
				dialog.setCancelable(true);
				
				Button btnSave = (Button)dialog.findViewById(R.id.btnSave);
				btnSave.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						saveScreen(dialog);
					}
				});
				
				dialog.show();
			}
		});
	}
	
	public void saveScreen(Dialog dialog) {
		// Get bitmap from root layout
		View view = findViewById(android.R.id.content).getRootView();
		view.setDrawingCacheEnabled(true);
		Bitmap bm1 = Bitmap.createBitmap(view.getDrawingCache());
		view.setDrawingCacheEnabled(false);
		
		// Get bitmap from dialog layout
		View dialogView = dialog.getWindow().getDecorView();
		dialogView.setDrawingCacheEnabled(true);
		Bitmap bm2 = Bitmap.createBitmap(dialogView.getDrawingCache());
		dialogView.setDrawingCacheEnabled(false);
		
		// Merge bitmap
		Bitmap bitmap = Bitmap.createBitmap(bm1.getWidth()
				, bm1.getHeight(), bm1.getConfig());
		Canvas c = new Canvas(bitmap);
		c.drawBitmap(bm1, new Matrix(), null);
		c.translate((bm1.getWidth() / 2) - (bm2.getWidth() / 2)
				, (bm1.getHeight() / 2) - (bm2.getHeight() / 2));
		c.drawBitmap(bm2, new Matrix(), null);
		
		// Clear bitmap
		bm1.recycle();
		bm2.recycle();
		
		try {
			// Save bitmap to storage
			Date d = new Date();
			String filename  = (String)DateFormat.format("hhmmss-MMddyyyy"
					, d.getTime());
			File dir = new File(Environment.getExternalStorageDirectory()
					, "/Pictures/" + filename + ".jpg");
			FileOutputStream out = new FileOutputStream(dir);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			out.write(bos.toByteArray());

			// Clear bitmap
			bitmap.recycle();
			
			// Update image to media system
			Intent intent = 
					new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			intent.setData(Uri.fromFile(dir));
			sendBroadcast(intent);
			
			Toast.makeText(getApplicationContext(), "Saved!"
					, Toast.LENGTH_SHORT).show();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}