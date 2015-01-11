package com.gokhanmoral.stweaks.app;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

//TODO: check for updates (almost ready)
//TODO: flash kernel/zip

public class MainActivity extends FragmentActivity implements ActionBar.TabListener, SyhValueChangedInterface, OnClickListener{

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private static ViewPager mViewPager;
    
    //==================== Syh UI Elements ================================
    
	private static final String LOG_TAG = MainActivity.class.getName();
    private static final ArrayList<SyhTab> syhTabList = new ArrayList<>();
    private final Boolean testingWithNoKernelSupport = false;
	private Boolean kernelSupportOk = false;
	private Boolean userInterfaceConfigSuccess = false;
	private Boolean valueChanged = false;
	private ProgressDialog dialog = null;
	private String valuesChanged = ""; 

	private boolean getUserInterfaceConfigFromAssets() {
		Log.i(LOG_TAG, "Siyah script NOT found! But testing is enabled, so continue...");
		Log.i(LOG_TAG, "Getting config xml from apk...");
		Boolean isOk = false;
		try 
		{     		
			InputStream is = getAssets().open("customconfig.xml");
			isOk = parseUIFromXml(is);
		} 
		catch (IOException e) 
		{
			Log.i(LOG_TAG, "No config xml inside apk...");
			//e.printStackTrace();
		}
		return isOk;
	}

	private boolean getUserInterfaceConfigFromScript() {
		Log.i(LOG_TAG, "Siyah script found.");
		Log.i(LOG_TAG, "Getting config xml via script...");
		Boolean isOk = false;
		String response = Utils.executeRootCommandInThread("/res/uci.sh config");
		if (response != null && !response.equals(""))
		{
			Log.i(LOG_TAG, "Config xml extracted from script!");				
			ByteArrayInputStream is = new ByteArrayInputStream(response.getBytes());
			isOk = parseUIFromXml(is);
		}
		return isOk;
	}

	private boolean isKernelSupportOk() {
		Boolean isOk = false;
		Log.i(LOG_TAG, "Searching siyah script...");
        File file = new File("/res/uci.sh");
        if (file.exists())
        {
            Log.i(LOG_TAG, "Kernel script(s) found.");
        	if (file.canExecute())
        	{
        		Log.i(LOG_TAG, "Kernel script(s) OK.");
        		isOk = true;
        	}
        	else
        	{
        		Log.e(LOG_TAG, "Kernel script(s) NOT OK!");
        	}
        }
        else
        {
        	Log.e(LOG_TAG, "Kernel script(s) NOT found!");        	
        }
		return isOk;
	}
	
	boolean parseUIFromXml(InputStream is)
	{
		Boolean isOk = true;
		syhTabList.clear();
		Log.i(LOG_TAG, "parseUIFromXml !");
		try {
			// get a new XmlPullParser object from Factory
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			// set input source
			parser.setInput(is, null);
			// get event type
			int eventType = parser.getEventType();
			
	    	SyhTab tab = null;
	    	SyhPane pane = null;
	    	SyhSpinner spinner = null;
	    	SyhSeekBar seekbar;
	    	SyhSwitch syhswitch;
	    	SyhButton button;

			// process tag while not reaching the end of document
			while(eventType != XmlPullParser.END_DOCUMENT) {
				String tagName; 
				switch(eventType) {
					// at start of document: START_DOCUMENT
					case XmlPullParser.START_DOCUMENT:
						//study = new Study();
						break;

					// at start of a tag: START_TAG
					case XmlPullParser.START_TAG:
						// get tag name
						tagName = parser.getName();
						// if <settingsTab>, get attribute: 'name'
						if(tagName.equalsIgnoreCase("settingsTab")) {
							//Log.w("parseUIFromXml", "settingsTab name = " + parser.getAttributeValue(null, "name"));
							tab = new SyhTab(this);
							tab.name = parser.getAttributeValue(null, "name");
						}
						// if <settingsPane>, get attribute: 'name' and 'description'
						else if(tagName.equalsIgnoreCase("settingsPane")) {
							//Log.w("parseUIFromXml", "settingsPane name = " + parser.getAttributeValue(null, "name"));
							//Log.w("parseUIFromXml", "settingsPane description = " + parser.getAttributeValue(null, "description"));
					    	pane = new SyhPane();	
					    	pane.description =parser.getAttributeValue(null, "description");
					    	pane.name = parser.getAttributeValue(null, "name");
						}
						// if <syhswitch>
						else if(tagName.equalsIgnoreCase("checkbox")) {
							//Log.w("parseUIFromXml", "syhswitch name = " + parser.getAttributeValue(null, "name"));
							//Log.w("parseUIFromXml", "syhswitch description = " + parser.getAttributeValue(null, "description"));
							//Log.w("parseUIFromXml", "syhswitch action = " + parser.getAttributeValue(null, "action"));
							//Log.w("parseUIFromXml", "syhswitch label = " + parser.getAttributeValue(null, "label"));
							if (pane != null)
							{
							   	syhswitch = new SyhSwitch(this);
						    	syhswitch.name = parser.getAttributeValue(null, "name");
						    	syhswitch.description = parser.getAttributeValue(null, "description");
						    	syhswitch.action =parser.getAttributeValue(null, "action");
						    	syhswitch.label = parser.getAttributeValue(null, "label");
						    	pane.controls.add(syhswitch);
							}
						}
						else if(tagName.equalsIgnoreCase("spinner")) {
							//Log.w("parseUIFromXml", "spinner name = " + parser.getAttributeValue(null, "name"));
							//Log.w("parseUIFromXml", "spinner description = " + parser.getAttributeValue(null, "description"));
							//Log.w("parseUIFromXml", "spinner action = " + parser.getAttributeValue(null, "action"));
							if (pane != null)
							{
								spinner = new SyhSpinner(this);
								spinner.name = parser.getAttributeValue(null, "name");
								spinner.description = parser.getAttributeValue(null, "description");
								spinner.action = parser.getAttributeValue(null, "action");
								pane.controls.add(spinner);							    	
							}
						}
						else if(tagName.equalsIgnoreCase("spinnerItem")) {
							//Log.w("parseUIFromXml", "spinnerItem name = " + parser.getAttributeValue(null, "name"));
							//Log.w("parseUIFromXml", "spinnerItem value = " + parser.getAttributeValue(null, "value"));
							if (spinner != null)
							{
								spinner.addNameAndValue(parser.getAttributeValue(null, "name"), parser.getAttributeValue(null, "value"));
							}
						}
						else if(tagName.equalsIgnoreCase("seekBar")) {
							//Log.w("parseUIFromXml", "seekBar name = " + parser.getAttributeValue(null, "name"));
							//Log.w("parseUIFromXml", "seekBar description = " + parser.getAttributeValue(null, "description"));
							//Log.w("parseUIFromXml", "seekBar action = " + parser.getAttributeValue(null, "action"));
							if (pane != null)
							{
								seekbar = new SyhSeekBar(this);
								seekbar.name = parser.getAttributeValue(null, "name");
								seekbar.description = parser.getAttributeValue(null, "description");
								seekbar.action =parser.getAttributeValue(null, "action");
								seekbar.max = Integer.parseInt(parser.getAttributeValue(null, "max"));
								seekbar.min = Integer.parseInt(parser.getAttributeValue(null, "min"));
								seekbar.step = Integer.parseInt(parser.getAttributeValue(null, "step"));
								seekbar.reversed = Boolean.parseBoolean(parser.getAttributeValue(null, "reversed"));
								seekbar.unit = parser.getAttributeValue(null, "unit");
								pane.controls.add(seekbar);							    	
							}
						}
						else if(tagName.equalsIgnoreCase("button")) {
							//Log.w("parseUIFromXml", "button name = " + parser.getAttributeValue(null, "name"));
							//Log.w("parseUIFromXml", "button description = " + parser.getAttributeValue(null, "description"));
							//Log.w("parseUIFromXml", "button action = " + parser.getAttributeValue(null, "action"));
							//Log.w("parseUIFromXml", "button label = " + parser.getAttributeValue(null, "label"));
							if (pane != null)
							{
								button = new SyhButton(this);
								button.name = parser.getAttributeValue(null, "name");
								button.description = parser.getAttributeValue(null, "description");
								button.action =parser.getAttributeValue(null, "action");
								button.label = parser.getAttributeValue(null, "label");
						    	pane.controls.add(button);								
							}
						}
						break;
					case XmlPullParser.END_TAG:
						// get tag name
						tagName = parser.getName();
						// if <settingsTab>, get attribute: 'name'
						if(tagName.equalsIgnoreCase("settingsTab")) {
							//Log.w("parseUIFromXml", "settingsTab name = " + parser.getAttributeValue(null, "name") + " ended!");
							if (tab != null)
							{
								syhTabList.add(tab);
								tab = null;
							}
						}
						// if <settingsPane>, get attribute: 'name' and 'description'
						else if(tagName.equalsIgnoreCase("settingsPane")) {
							//Log.w("parseUIFromXml", "settingsPane name = " + parser.getAttributeValue(null, "name") + " ended!");
							if ((tab != null) && (pane != null))
							{
								tab.panes.add(pane);
								pane = null;
							}
						}
						break;
				}
				// jump to next event
				eventType = parser.next();
			}
		// exception stuffs
		} catch (XmlPullParserException e) {
			isOk = false;
			e.printStackTrace();
		} catch (IOException e) {
			isOk = false;
			e.printStackTrace();
		}		
		
		return isOk;
	}
    	
	private void getScriptValuesWithoutUiChange() 
	{
		String exitInActions = Utils.executeRootCommandInThread("grep exit /res/customconfig/actions/*");
		boolean optimized = false;
		if(exitInActions == null || exitInActions.length() == 0)
        {
			Utils.executeRootCommandInThread("source /res/customconfig/customconfig-helper");
	        Utils.executeRootCommandInThread("read_defaults");
	        Utils.executeRootCommandInThread("read_config");
	        optimized = true;
        }
       	for (int i = 0; i < syhTabList.size(); i++)
    	{
   	   		SyhTab tab = syhTabList.get(i);
   	   		for  (int j = 0; j < tab.panes.size(); j++)
	        {
	        	SyhPane pane = tab.panes.get(j);
	        	for  (int k = 0; k < pane.controls.size(); k++)
	        	{
	        		SyhControl control = pane.controls.get(k);	        		
	        		control.getValueViaScript(optimized);
	        	}
	        }
    	}			
	}

	private void clearUserSelections() //UI access
	{
       	for (int i = 0; i < syhTabList.size(); i++)
    	{
   	   		SyhTab tab = syhTabList.get(i);
	        for  (int j = 0; j < tab.panes.size(); j++)
	        {
	        	SyhPane pane = tab.panes.get(j);
	        	for  (int k = 0; k < pane.controls.size(); k++)
	        	{
	        		SyhControl control = pane.controls.get(k);        		
	        		control.applyScriptValueToUserInterface();
	        	}
	        }
    	}			
	}

	private boolean applyUserSelections() //no UI access
	{
       	boolean isAllSelectionsOk = true;
       	valuesChanged = "";
		for (int i = 0; i < syhTabList.size(); i++)
    	{
   	   		SyhTab tab = syhTabList.get(i);
	        for  (int j = 0; j < tab.panes.size(); j++)
	        {
	        	SyhPane pane = tab.panes.get(j);
	        	for  (int k = 0; k < pane.controls.size(); k++)
	        	{
	        		SyhControl control = pane.controls.get(k);	        		
	        		if(control.isChanged() && control.canGetValueFromScript) //TODO: Move these checks into the SyhControl class
	        		{
	        			Log.i(LOG_TAG, "Changed control:" + control.name);
	        			String res =  control.setValueViaScript();
	        			isAllSelectionsOk = isAllSelectionsOk && (res.length() > 0);
	        			valuesChanged += control.name + ": " + res + "\r\n";
	        		}
	        	}
	        }
    	}
		return isAllSelectionsOk;
	}

	//=====================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        //-- actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        assert actionBar != null;
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD); //hide "only tabs" in action bar
		//-- createSyhUI(actionBar);
    	
    	LinearLayout acceptDecline = (LinearLayout)findViewById(R.id.AcceptDeclineLayout);
    	acceptDecline.setVisibility(LinearLayout.GONE);
		final Button button = (Button) acceptDecline.findViewById(R.id.AcceptButton);
		button.setOnClickListener(this);
		final Button button2 = (Button) acceptDecline.findViewById(R.id.DeclineButton);
		button2.setOnClickListener(this);
    	
	    //final ActionBar actionBar = getActionBar();
    	//actionBar.hide();

    
        TextView startTextView = (TextView)findViewById(R.id.textViewStart);
        kernelSupportOk = isKernelSupportOk();
        
        if (!kernelSupportOk && !testingWithNoKernelSupport)
        {
        	startTextView.setText(R.string.startmenu_nokernelsupport);
        }
        else
        {
         	if (Utils.canRunRootCommandsInThread())
         	{
         		new LoadDynamicUI().execute();
         		dialog = ProgressDialog.show(this, getResources().getText(R.string.app_name), "Loading! Please wait...", true);
         	}
         	else
         	{
         		startTextView.setText(R.string.startmenu_no_root);
         		Utils.reset();
         	}       	
        }

    	
//    	final Runnable r = new Runnable()
//    	{
//    	    public void run() 
//    	    {
//    	        TextView startTextView = (TextView)findViewById(R.id.textViewStart);
//    	        startTextView.setVisibility(TextView.GONE);
//    		    final ActionBar actionBar = getActionBar();
//    		    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//    		    //actionBar.show();
//    	    	mViewPager.setVisibility(ViewPager.VISIBLE);
//    	    }
//    	};
//    	Handler handler = new Handler();
//    	handler.postDelayed(r, 5000);   	

    }
    
	private void createSwipableTabs(final ActionBar actionBar) {
		// Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        /*
      The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
      sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
      keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
      to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
    	mViewPager.setVisibility(ViewPager.GONE);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        
        //WARNING: This is the trick preventing the Off-screen Fragments from going out of memory!!! 
        mViewPager.setOffscreenPageLimit(syhTabList.size());

        // When swiping between different sections, select the corresponding tab.
        // We can also use ActionBar.Tab#select() to do this if we have a reference to the
        // Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        Integer numOfTabs = mSectionsPagerAdapter.getCount();
        for (int i = 0; i < numOfTabs; i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //DONE: move extras tab to options menu
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        final Context mContext = this;
        switch (item.getItemId()) {
            case R.id.menu_about: {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                View v = LayoutInflater.from(mContext).inflate(R.layout.syh_extrastab, mViewPager, false);
                final TextView tv = (TextView) v.findViewById(R.id.textViewAppVersion);
                try {
                    final String appVersion = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
                    tv.setText("App Version: " + appVersion);
                } catch (PackageManager.NameNotFoundException e) {
                    tv.setText("App Version: Not found!");
                }

                final TextView tv2 = (TextView) v.findViewById(R.id.textViewKernelVersion);
                tv2.setText("Kernel version: " + System.getProperty("os.version"));

                String s = "";
                s += "\n Kernel Version: " + System.getProperty("os.version");
                s += "\n ROM Version: " + android.os.Build.VERSION.INCREMENTAL;
                s += "\n ROM API Level: " + android.os.Build.VERSION.SDK_INT;
                s += "\n ROM Codename: " + android.os.Build.VERSION.CODENAME;
                s += "\n ROM Release Version: " + android.os.Build.VERSION.RELEASE;
                s += "\n Hardware Serial: " + android.os.Build.SERIAL;
                s += "\n Radio Version: " + android.os.Build.getRadioVersion();
                s += "\n Device: " + android.os.Build.DEVICE;
                //s += "\n Model (and Product): " + android.os.Build.MODEL + " (" + android.os.Build.PRODUCT + ")";
                tv2.setText(s);
                builder.setView(v)

                .setPositiveButton("Back", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setTitle("About STweaks")
                .setIcon(R.drawable.ic_launcher)
                .create();
                builder.show();

            }
                return true;
            case R.id.menu_reset: {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("All settings will be reset. You will have to relaunch the application.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Handle Ok
                                Utils.executeRootCommandInThread("/res/uci.sh delete default");
                                System.exit(0);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Handle Cancel
                            }
                        })
                        .setTitle("Warning")
                        .setIcon(R.drawable.ic_launcher)
                        .create();
                        builder.show();
            }
                return true;
            case R.id.menu_flash: {
                Toast toast1 = Toast.makeText(mContext, R.string.coming_soon, Toast.LENGTH_LONG);
                toast1.show();
            }
            return true;
            default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new SyhTabFragment();
            fragment.setRetainInstance(true);
            Bundle args = new Bundle();
            args.putInt(SyhTabFragment.ARG_SECTION_NUMBER, i);
            fragment.setArguments(args);
        	//-- Log.i(LOG_TAG, "getItem getItem:" + i); 
            return fragment;
        }

        @Override
        public int getCount() {
        	return syhTabList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
        	if (position < syhTabList.size())
        	{
        		return syhTabList.get(position).name;
        	}
            return null;
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class SyhTabFragment extends Fragment {
        public SyhTabFragment() {
        }

        public static final String ARG_SECTION_NUMBER = "section_number";
        public Integer mTabIndex = -1;

        @Override
        public void onCreate (Bundle savedInstanceState){
    		super.onCreate(savedInstanceState);
  	   		
    		Bundle args = getArguments();
   	   		Integer tabIndex = args.getInt(ARG_SECTION_NUMBER);
   	   	    mTabIndex = tabIndex;
        	//--Log.i(LOG_TAG, "onCreate savedInstanceState:" + savedInstanceState + " mTabIndex:" + mTabIndex);       	
        }
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

        	//-- Log.i(LOG_TAG, "onCreateView savedInstanceState:" + savedInstanceState + " mTabIndex:" + mTabIndex);
        	
    		ScrollView tabEnclosingLayout = createSyhTab(mTabIndex);
	        
	        return tabEnclosingLayout;
        }

		private ScrollView createSyhTab(Integer tabIndex) {
			//ScrollView tabEnclosingLayout = new ScrollView(getActivity());
       		//tabEnclosingLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            ScrollView tabEnclosingLayout = (ScrollView) LayoutInflater.from(getActivity()).inflate(R.layout.template_tab_scrollview,mViewPager, false);

       		SyhTab tab = syhTabList.get(tabIndex);
       		
       		View  customView = tab.getCustomView(tabEnclosingLayout);
       		if(customView != null)
       		{
       			tabEnclosingLayout.addView(customView);
       		}
       		else
       		{
                //Moved to xml
                //LinearLayout tabContentLayout = new LinearLayout(getActivity());
           		//tabContentLayout.setBackgroundColor(Color.WHITE);
           		//tabContentLayout.setOrientation(LinearLayout.VERTICAL);
           		//tabContentLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                //tabContentLayout.setElevation(16); // Test

                LinearLayout tabContentLayout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.template_content_layout, mViewPager, false);

                for  (int j = 0; j < tab.panes.size(); j++)
		        {
		        	SyhPane pane = tab.panes.get(j);
		        	pane.addPaneToUI(getActivity(), tabContentLayout);
		        	for  (int k = 0; k < pane.controls.size(); k++)
		        	{
		        		SyhControl control = pane.controls.get(k);  
		        		control.create();
		        		tabContentLayout.addView(control.view);
		        	}
		        }
           		tabEnclosingLayout.addView(tabContentLayout);
       		}
	        
			return tabEnclosingLayout;
		}

    }

    private class DialogCancelling extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return null;
		}
		protected void onPostExecute(Boolean result) {
        	if (dialog != null && dialog.isShowing())
        	{
        		dialog.cancel();
        		dialog = null;
        	}
        }
    }
    //final AsyncTask<Params, Progress, Result>
    private class LoadDynamicUI extends AsyncTask<Void, Void, Boolean> {
        /** The system calls this to perform work in a worker thread and
          * delivers it the parameters given to AsyncTask.execute() */
        protected Boolean doInBackground(Void... params) 
        {
        	if (testingWithNoKernelSupport)
        	{
        		userInterfaceConfigSuccess = getUserInterfaceConfigFromAssets(); 
        		Log.e(LOG_TAG, "NumOfTabs:" + syhTabList.size());
        	}  
        	else if(kernelSupportOk)
        	{
        		userInterfaceConfigSuccess =  getUserInterfaceConfigFromScript();        	
        	}

        	if (userInterfaceConfigSuccess)
        	{
        		getScriptValuesWithoutUiChange();
        	}
        	
        	return userInterfaceConfigSuccess;//TODO:
        }
        
        /** The system calls this to perform work in the UI thread and delivers
          * the result from doInBackground() */
        protected void onPostExecute(Boolean result) {
        	if (result)
        	{
    	        TextView startTextView = (TextView)findViewById(R.id.textViewStart);
    	        startTextView.setVisibility(TextView.GONE);
    	        
    		    final ActionBar actionBar = getActionBar();
    		    createSwipableTabs(actionBar);
                assert actionBar != null;
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    		    //--actionBar.show();
    	    	mViewPager.setVisibility(ViewPager.VISIBLE);
        		        		
        		//initUI(mainLayout);
        		//clearUserSelections(); //apply script values to UI
        	}
        	new DialogCancelling().execute();
        }

    }

    //final AsyncTask<Params, Progress, Result>
    private class ApplyChangedValues extends AsyncTask<Void, Void, Boolean> {
        /** The system calls this to perform work in a worker thread and
          * delivers it the parameters given to AsyncTask.execute() */
        protected Boolean doInBackground(Void... params) 
        {
        	Boolean isAllOk = false;
        	if((kernelSupportOk || testingWithNoKernelSupport) && userInterfaceConfigSuccess)
        	{
        		isAllOk = applyUserSelections(); //apply scripts only, no UI change...
        	}
        	return isAllOk;
        }
        
        /** The system calls this to perform work in the UI thread and delivers
          * the result from doInBackground() */
        protected void onPostExecute(Boolean result) {
           	Toast toast1 = Toast.makeText(getApplicationContext(), valuesChanged, Toast.LENGTH_LONG);
        	toast1.show();  

        	if (!result)
        	{
               	//TODO: Fix this!
        		//Toast toast = Toast.makeText(getApplicationContext(), "Some selections failed to apply!", Toast.LENGTH_LONG);
            	//toast.show();
        	}
        	new DialogCancelling().execute();
        }
    }    
    
	@Override
	public void valueChanged() {
		if (!valueChanged){			
		   	LinearLayout acceptDecline = (LinearLayout)findViewById(R.id.AcceptDeclineLayout);
		   	acceptDecline.setVisibility(LinearLayout.VISIBLE);
		}
		valueChanged = true;
	}

	@Override
	public void onClick(View v) {
	   	LinearLayout acceptDecline = (LinearLayout)findViewById(R.id.AcceptDeclineLayout);
		switch(v.getId()) {
        case R.id.AcceptButton:
		   	acceptDecline.setVisibility(LinearLayout.GONE);
	    	new ApplyChangedValues().execute();
			//TODO: Too fast!!
	    	//-- dialog = ProgressDialog.show(this, getResources().getText(R.string.app_name), "Applying changed values! Please wait...", true);
        	valueChanged = false;
            break;
        case R.id.DeclineButton:
		   	acceptDecline.setVisibility(LinearLayout.GONE);
        	clearUserSelections(); //UI change only, no scripts...
			valueChanged = false;
            break;
		}		
	}


}


