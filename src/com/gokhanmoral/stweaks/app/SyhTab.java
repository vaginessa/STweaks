package com.gokhanmoral.stweaks.app;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

class SyhTab {
	public String name;
	public final List<SyhPane> panes;
	final Context mContext;
    	public SyhTab(Context context)
	{
		name = "";
		panes = new ArrayList<>();
		mContext = context;
	}
	
	public View getCustomView(ViewGroup parent)
	{
		return null;
	}
}
