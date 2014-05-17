package ajman.university.grad.project.eventshare.common.helpers;

import java.util.Calendar;

import ajman.university.grad.project.eventshare.common.models.Event;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.widget.Toast;

public class Utils {
	public static String formatToUnixDate(Event event) {
		String date = "UNKNOWN";
		// TODO: Do something and return
//		date += event.getToYear();
//		date += (event.getToMonth() < 10 ? "0" + (event.getToMonth() + 1) : event.getToMonth() + 1);
//		date += (event.getToDay() < 10 ? "0" + event.getToDay() : event.getToDay());
//		date += (event.getToDayHour() < 10 ? "0" + event.getToDayHour() : event.getToDayHour());
//		date += (event.getToMinute() < 10 ? "0" + event.getToMinute() : event.getToMinute());
//		date += "00";
		return date;
	}
	
	public static int getTimeDifferenceFromNow(int year, int month, int day, int hour, int minutes) {
		Calendar eventCal = Calendar.getInstance();
		eventCal.set(Calendar.YEAR, year);
		eventCal.set(Calendar.MONTH, month);
		eventCal.set(Calendar.DAY_OF_MONTH, day);
		eventCal.set(Calendar.HOUR_OF_DAY, hour);
		eventCal.set(Calendar.MINUTE, minutes);
		
		Calendar now = Calendar.getInstance();
		
		return (int)((eventCal.getTimeInMillis() - now.getTimeInMillis()) / 60000);
	}
}
