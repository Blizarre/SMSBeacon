package com.example.smsbeacon;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.Button;
import android.preference.PreferenceManager;

public class Config extends Activity {

	ToggleButton tgB;
	Button prefsButton = null;
	Button demoButton = null;
	TextView textView = null;
	
	public static class settings{
		public String pwd;
		public String phoneNumber;
	}
	private final String PREF_PWD = "password";
	private final String PREF_PHONE = "phone_number";
	
	public static final String PREFS_NAME = "MyPrefsFile";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config);
		tgB = (ToggleButton)findViewById(R.id.toggleButton1);
		tgB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) 
			{
				//do smthg
				Toast.makeText(Config.this,
    					"should do something",
    					Toast.LENGTH_SHORT).show();
			}});
		/*bouton d'accès aux préférences*/
		prefsButton = (Button)findViewById(R.id.prefs_access_btn);
		/*bouton d'affichage des préférences*/
		demoButton = (Button)findViewById(R.id.demo);
		/*listenner sur les deux boutons*/
		View.OnClickListener listenner = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.prefs_access_btn:
					/*lancement de PreferenceActivity*/
					Intent intent = new Intent(Config.this,
							PreferencesActivity.class);
						      startActivity(intent);
						      break;
				case R.id.demo:
					displaySharedPreferences();
					break;
				}
				
			}
		};
		prefsButton.setOnClickListener(listenner);
		demoButton.setOnClickListener(listenner);
		textView = (TextView) findViewById(R.id.delete_me);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.config, menu);
		return true;
	}
	
	private void displaySharedPreferences() {
		   SharedPreferences prefs = PreferenceManager
		    .getDefaultSharedPreferences(Config.this);
		   /*creation de la string contenant les paramètres à afficher*/
		   String lostPwd = prefs.getString("Lost pwd", "Default lost pwd");
		   String RbdPassw = prefs.getString("Robbed pwd", "Robbed pwd");
		   String settingsPwd = prefs.getString("Settings pwd", "Settings pwd");
		   boolean flashLight = prefs.getBoolean("Flash Light", false);
		   String ringTone = prefs.getString("List preference", "Default ringtone");
		 
		   StringBuilder builder = new StringBuilder();
		   builder.append("lost pasword: " + lostPwd + "\n");
		   builder.append("Robbed password: " + RbdPassw + "\n");
		   builder.append("Settings password: " + settingsPwd + "\n");
		   builder.append("Keep me logged in: " + String.valueOf(flashLight) + "\n");
		   builder.append("List preference: " + ringTone);
		 
		   textView.setText(builder.toString());
		}
}
