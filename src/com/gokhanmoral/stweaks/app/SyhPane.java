package com.gokhanmoral.stweaks.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

class SyhPane {
	public String description;
	public String name;
	
	public final List<SyhControl> controls = new ArrayList<>();
	
	public void addPaneToUI(Activity activity, LinearLayout layout)
	{
		TextView paneNameView = (TextView)LayoutInflater.from(activity).inflate(R.layout.template_panelname, layout, false);
		paneNameView.setText(this.name.toUpperCase(Locale.US));
		layout.addView(paneNameView); 
        
		if ((this.description != null) && (!this.description.equals("")))
		{
	        TextView paneDescriptionView = (TextView)LayoutInflater.from(activity).inflate(R.layout.template_paneldesc, layout, false);
	        paneDescriptionView.setText(this.description);
	        layout.addView(paneDescriptionView);  
		}	
	}
}
