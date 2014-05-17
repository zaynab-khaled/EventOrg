package com.example.nfcmifareclassic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.os.Build;

public class AccessBitsWriteActivity extends Activity {

	private TextView mTextView;
	private NfcAdapter mNfcAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mIntentFilters;
	private String[][] mNFCTechLists;
	private static boolean erase = false;

	public static final String MIME_TEXT_CALENDAR = "text/x-vcalendar";
	private static final String HEXES = "0123456789ABCDEF";

	/*private static final byte[] KEYA = { 
			(byte) 0xd3, (byte) 0xf7, (byte) 0xd3,
			(byte) 0xf7, (byte) 0xd3, (byte) 0xf7 };*/

	private static final byte[] KEYA_NEURO = {
			(byte) 0xc1, (byte) 0xf9, (byte) 0xd3,
			(byte) 0x45, (byte) 0x23, (byte) 0x11
	};

	private static final byte[] KEYA_FRESHTAG = { 
		(byte) 0xff, (byte) 0xff, (byte) 0xff,
		(byte) 0xff, (byte) 0xff, (byte) 0xff };

	private static final byte[] KEYB = { 
			(byte) 0xff, (byte) 0xff, (byte) 0xff,
			(byte) 0xff, (byte) 0xff, (byte) 0xff };

	private static final byte[] ACCESSBITS = { 
			(byte) 0x7F, (byte) 0x07,
			(byte) 0x88, (byte) 0x00 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_write);
		mTextView = (TextView) findViewById(R.id.tv);
		mTextView.setText("Tap tag to change access bits...");
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		// create an intent with tag data and deliver to this activity
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		// set an intent filter for all MIME data
		IntentFilter ndefIntent = new IntentFilter(
				NfcAdapter.ACTION_TECH_DISCOVERED);
		try {
			ndefIntent.addDataType("*/*");
			mIntentFilters = new IntentFilter[] { ndefIntent };
		} catch (Exception e) {
			Log.e("TagDispatch", e.toString());
		}

		mNFCTechLists = new String[][] { new String[] { MifareClassic.class
				.getName() } };
	}

	@Override
	public void onResume() {
		super.onResume();
		mNfcAdapter.enableForegroundDispatch(this, mPendingIntent,
				mIntentFilters, mNFCTechLists);
	}

	@Override
	public void onNewIntent(Intent intent) {
		Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
		if (!erase) {
			processIntent(intent);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mNfcAdapter.disableForegroundDispatch(this);
	}

	private void processIntent(Intent intent) {

		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

		boolean auth = false;
		MifareClassic mfc = MifareClassic.get(tagFromIntent);

		try {
			String metaInfo = "";

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			
			/***********************************************************/
			//for fresh tag
			outputStream.write(KEYA_FRESHTAG);
			
			//change key A
			//outputStream.write(KEYA_NEURO);
			
			outputStream.write(ACCESSBITS);
			outputStream.write(KEYB);
			/***********************************************************/
			
			byte[] newTrailerData = outputStream.toByteArray();

			if (newTrailerData.length != MifareClassic.BLOCK_SIZE) {
				System.out
						.println("Error: please check your newTrailerData size!");
				return;
			}

			mfc.connect();

			for (int j = 0; j < mfc.getSectorCount(); j++) {
				
				/***********************************************************/
				// Authenticate sector with key B.
				//auth = mfc.authenticateSectorWithKeyB(j, KEYB);

				// for fresh tag
				auth = mfc.authenticateSectorWithKeyA(j, KEYA_FRESHTAG);
				/***********************************************************/
				
				int bCount;
				int bIndex;
				if (auth) {
					System.out.println("Sector " + j
							+ " authenticated successfully");
					bCount = mfc.getBlockCountInSector(j);
					bIndex = mfc.sectorToBlock(j);

					for (int i = 0; i < bCount; i++) {

						// Write to trailer blocks with key B
						if ((bIndex + 1) % bCount == 0) {
							try {
								mfc.writeBlock(bIndex, newTrailerData);
								System.out.println("Written to block: "
										+ bIndex);

							} catch (IOException e) {
								System.out.println("Could not write to block: "
										+ bIndex);
							}
						}

						try {
							byte[] data = mfc.readBlock(bIndex);
							metaInfo += "Block " + bIndex + " : "
									+ byteArrayToHexString(data) + "\n";
						} catch (Exception e) {
							System.out.println("Cound not read block nr: "
									+ bIndex);
						}

						bIndex++;
					}
				} else {
					System.out.println("Sector " + j
							+ " could not be authenticated");
				}
			}
			System.out.println(metaInfo);
			mfc.close();
			erase = true;
			new AlertDialog.Builder(this)
					.setMessage("Access bits successfully written!")
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										DialogInterface dialogInterface, int i) {
									finish();
								}
							}).show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String byteArrayToHexString(byte[] raw) {
		if (raw == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * raw.length);
		for (final byte b : raw) {
			hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(
					HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}
}
