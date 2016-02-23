package com.nullpointexecutioners.buzzfilms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

/**
 * Splash screen for our app gives the appearance of speeeeeeeed
 */
public class SplashActivity extends Activity {

    private SessionManager mSession;

    /**
     * Creates this activity
     * @param savedInstanceState no idea what this is
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        /*Make the status bar and nav bar translucent*/
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        this.mSession = SessionManager.getInstance(getApplicationContext());

        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(5); //wait for X number of milliseconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent intent;
                    if (mSession.checkLogin()) {
                        intent = new Intent(SplashActivity.this, MainActivity.class);
                    } else {
                        intent = new Intent(SplashActivity.this, WelcomeActivity.class);
                    }
                    //Closing all the Activities
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    //Add new Flag to start new Activity
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        };
        timerThread.start();
    }

    /**
     * Handles the activity once it is paused (i.e. in the background)
     */
    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
