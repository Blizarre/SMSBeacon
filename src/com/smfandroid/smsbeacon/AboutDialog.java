package com.smfandroid.smsbeacon;

import com.example.smsbeacon.R;

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
/*	    builder.setPositiveButton(R.string.edit_button_remove_all, this);
	    builder.setNegativeButton(R.string.edit_button_cancel, this); */
	    return builder.create();
	}
}
