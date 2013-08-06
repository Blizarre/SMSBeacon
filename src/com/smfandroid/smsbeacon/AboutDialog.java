package com.smfandroid.smsbeacon;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Html;

public class AboutDialog extends DialogFragment {

    @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	
	    Builder builder = new Builder(getActivity());
	    builder.setMessage(Html.fromHtml(getString(R.string.msg_about)));
	    builder.setNeutralButton(R.string.button_continue, null);

	    return builder.create();
	}
}
