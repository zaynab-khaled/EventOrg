package com.example.nfcmifareclassic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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

public class DataWriteActivity extends Activity {

	static String separator = System.getProperty("line.separator");
	public static String calString = "BEGIN:VCALENDAR" + separator
			+ "PRODID:-//AT Content Types//AT Event//EN" + separator
			+ "VERSION:2.0" + separator + "METHOD:PUBLISH" + separator
			+ "BEGIN:VEVENT" + separator + "DTSTAMP:20140320T070804Z"
			+ separator + "CREATED:20111007T042948Z" + separator
			+ "UID:ATEvent-17dfbd0146a309fd6f48b50a79c9cdcf" + separator
			+ "LAST-MODIFIED:20111007T043003Z" + separator
			+ "SUMMARY:Yusra Test" + separator + "DTSTART:20111101T000000Z"
			+ separator + "DTEND:20111108T000000Z" + separator
			+ "LOCATION:San Francisco\\, CA" + separator
			+ "URL:http://ploneconf.org" + separator + "CLASS:PUBLIC"
			+ separator + "END:VEVENT" + separator + "BEGIN:VEVENT" + separator
			+ "DTSTAMP:20140320T070804Z" + separator
			+ "CREATED:20111007T042948Z" + separator
			+ "UID:ATEvent-17dfbd0136a309fd6f48b50a79c9cdcf" + separator
			+ "LAST-MODIFIED:20111007T043003Z" + separator
			+ "SUMMARY:Amina Test" + separator + "DTSTART:20131101T000000Z"
			+ separator + "DTEND:20131108T000000Z" + separator
			+ "LOCATION:San Francisco\\, CA" + separator
			+ "URL:http://ploneconf.org" + separator + "CLASS:PUBLIC"
			+ separator + "END:VEVENT" + separator + "END:VCALENDAR";

	private SharedPreferences nfcPreferences;
	private TextView mTextView;
	private NfcAdapter mNfcAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mIntentFilters;
	private String[][] mNFCTechLists;
	private static boolean write = false;

	private static final String FILENAME = "mCalendar.ics";
	private static final String TAG = ReadActivity.class.getName();
	public static final String MIME_TEXT_CALENDAR = "text/x-vcalendar";
	private static final String HEXES = "0123456789ABCDEF";
	private static final String preferenceName = "NFC_PREFERENCES";

	private static final byte[] KEYA = { (byte) 0xd3, (byte) 0xf7, (byte) 0xd3,
			(byte) 0xf7, (byte) 0xd3, (byte) 0xf7 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_write);

		nfcPreferences = getSharedPreferences(preferenceName,
				Context.MODE_PRIVATE);

		mTextView = (TextView) findViewById(R.id.tv);
		mTextView.setText("Tap tag to write...");
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
		if (!write) {
			processIntent(intent);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mNfcAdapter.disableForegroundDispatch(this);
	}

	private void processIntent(Intent intent) {

		if ((int) Math.ceil(calString.getBytes().length
				/ (double) MifareClassic.BLOCK_SIZE) > 216) {
			System.out.println("Message is too big to be written to tag!");
			return;
		}
		byte[][] msg = new byte[216][MifareClassic.BLOCK_SIZE];
		int start = 0;
		int calBlocks = (int) Math.ceil(calString.getBytes().length
				/ (double) MifareClassic.BLOCK_SIZE);

		for (int i = 0; i < calBlocks; i++) {
			if (start + MifareClassic.BLOCK_SIZE > calString.getBytes().length) {
				System.arraycopy(calString.getBytes(), start, msg[i], 0,
						calString.getBytes().length - start);
			} else {
				System.arraycopy(calString.getBytes(), start, msg[i], 0,
						MifareClassic.BLOCK_SIZE);
			}
			start += MifareClassic.BLOCK_SIZE;
		}

		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

		boolean auth = false;
		MifareClassic mfc = MifareClassic.get(tagFromIntent);

		try {
			String metaInfo = "";
			int msgCount = 0;
			mfc.connect();

			for (int j = 0; j < mfc.getSectorCount(); j++) {

				if (msgCount >= nfcPreferences
						.getInt("prevCalendarBlocks", 216) && msgCount > calBlocks) {
					System.out
							.println("Stopped writing because prev calendar size was: "
									+ nfcPreferences.getInt(
											"prevCalendarBlocks", 216));
					break;
				}

				// Authenticate a sector with key.
				auth = mfc.authenticateSectorWithKeyA(j, KEYA);
				int bCount;
				int bIndex;
				if (auth) {
					System.out.println("Sector " + j
							+ " authenticated successfully");
					bCount = mfc.getBlockCountInSector(j);
					bIndex = mfc.sectorToBlock(j);

					for (int i = 0; i < bCount; i++) {

						// Write to data blocks with key A
						if ((bIndex + 1) % bCount != 0) {
							try {
								mfc.writeBlock(bIndex, msg[msgCount]);
								msgCount++;
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
			System.out.println("Write count = " + msgCount);

			Editor editor = nfcPreferences.edit();
			editor.putInt("prevCalendarBlocks", calBlocks);
			editor.commit();

			System.out.println("sharedPrefernces: "
					+ nfcPreferences.getInt("prevCalendarBlocks", 216));
			mfc.close();
			write = true;
			new AlertDialog.Builder(this)
					.setMessage("Tag successfully written!")
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
