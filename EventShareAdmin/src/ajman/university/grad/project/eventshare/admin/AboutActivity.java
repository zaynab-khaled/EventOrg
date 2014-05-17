package ajman.university.grad.project.eventshare.admin;


import ajman.university.grad.project.eventshare.admin.helpers.Constants;
import ajman.university.grad.project.eventshare.common.contracts.IRemoteNotificationService;

import ajman.university.grad.project.eventshare.common.services.ServicesFactory;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Menu;

public class AboutActivity extends Activity {
	private static final String LOG_TAG = "About Activity"; 
	
	private IRemoteNotificationService remoteNotificationService = ServicesFactory
			.getRemoteNotificationService();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setBackgroundDrawable(new ColorDrawable(0xff33b5e5));
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayShowTitleEnabled(true);
		
		Log.d(LOG_TAG, " inside about");
		remoteNotificationService.sendPushNotification(Constants.PARSE_APP_ID, Constants.PARSE_APP_REST_KEY, "Neurology", "The Neurology department schedule has been updated");
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
