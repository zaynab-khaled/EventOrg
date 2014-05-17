package ajman.university.grad.project.eventshare.common.services;

import ajman.university.grad.project.eventshare.common.contracts.IErrorService;
import ajman.university.grad.project.eventshare.common.helpers.ApplicationContextProvider;
import android.content.Context;
import android.util.Log;

public class ErrorService implements IErrorService {
	
	private static String LOG_TAG = "Error Service";

	@Override
	public void log(String message) {
		Log.d(LOG_TAG, message);
//		new AlertDialog.Builder(getApplicationContext())
//		.setTitle("Sorry...an error occurred!")
//		.setMessage(message)
//		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int id) {
//				
//	           }
//		})
//		.show();
	}

	@Override
	public void log(Exception e) {
		Log.d(LOG_TAG, e.getMessage());
//		new AlertDialog.Builder(getApplicationContext())
//		.setTitle("Sorry...an exception occurred!")
//		.setMessage(e.getMessage())
//		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int id) {
//				
//	           }
//
//		})
//		.show();
	}
	
	private Context getApplicationContext() {
		return ApplicationContextProvider.getContext();		
	}
}
