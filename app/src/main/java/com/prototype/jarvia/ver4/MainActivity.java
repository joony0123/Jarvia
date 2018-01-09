package com.prototype.jarvia.ver4;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.AlarmClock;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import ai.api.services.GoogleRecognitionServiceImpl;

import com.google.api.client.util.DateTime;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;


import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.RequestExtras;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

import android.app.SearchManager;
import android.content.Intent;
import android.telephony.SmsManager;
import android.provider.ContactsContract;
import android.widget.Toast;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;



//import android.speech.SpeechRecognizer;


/**
 * Created by Joon.Y.K on 2017-03-19.
 */

public class MainActivity extends AppCompatActivity implements AIListener, RecognitionListener, NavigationView.OnNavigationItemSelectedListener {
    // Layout Settings
    private Button listenButton;
    private TextView resultTextView;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Message> messageList = new ArrayList<Message>();
    private EditText editText;
    private ImageButton sendButton;

    private SharedPreferences defaultPref;


    // Call & Message Settings
    private SMS sms;
    private ArrayList<String> possibleComb = new ArrayList<String>();

    // Api.Ai Settings
    private GoogleRecognitionServiceImpl aiService;
    private AIResponse aiResponse;
    private AIDataService aiDataService;
    private AIServiceException exception;

    // TTS Settings
    private TTSManager ttsManager;

    // Sphinx Recognizer Settings
    private static final String KWS_SEARCH = "wakeup";
    private static String KEYPHRASE;
    private SpeechRecognizer SphinxRecognizer;

    // Openweather Settings
    OpenWeatherWithGPS weather = new OpenWeatherWithGPS(this); //Here the context is passing

    // Google Cal Settings
    private GoogleCalendarCall myCal;
    private String googleAccnt;

    // Wiki Settings
    WikiResponse wiki;

    // User & bot name Settings
    private String userName;
    private String botName;


    private volatile boolean isListening = false;
    private boolean sleeping = false;
    private Intent webIntent;
    private volatile boolean searching = false;
    private volatile boolean recognitionActive = false;
    private int recogActive = 0;
    private boolean isCollectingData = false;
    private boolean chatUIUp = false;
    Vibrator vibe;
    private boolean calling = false;
    private boolean startModeChange = false;



    public static final String ALARM_ALERT_ACTION = "com.samsung.sec.android.clockpackage.alarm.ALARM_ALERT";
    public static final String ALARM_SNOOZE_ACTION = "com.samsung.sec.android.clockpackage.alarm.ALARM_SNOOZE";
    public static final String ALARM_STOPPED_IN_ALERT_ACTION = "com.samsung.sec.android.clockpackage.alarm.ALARM_STOPPED_IN_ALERT";
    public static final String ALARM_ALARM_STOP_ACTION = "com.samsung.sec.android.clockpackage.alarm.ALARM_STOP";

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(AlarmClock.ACTION_DISMISS_ALARM) ||
                  //  action.equals("com.samsung.sec.android.clockpackage.alarm.ALARM_SNOOZE") ||
                  //  action.equals("com.samsung.sec.android.clockpackage.alarm.ALARM_ALERT") ||
                    action.equals(AlarmClock.ACTION_SNOOZE_ALARM) ||
                    action.equals(ALARM_ALARM_STOP_ACTION) ||
                    action.equals(ALARM_STOPPED_IN_ALERT_ACTION)
                    )
            {
                // Stop the screen lock application here...
                Toast.makeText(getApplicationContext(), "hello", Toast.LENGTH_LONG).show();
                weather.getLocation();
                weather.getWeatherData(weather.lat, weather.lng);
                myCal.getResultsFromApi();
                isCollectingData = true;
                //SphinxRecognizer.stop();
                SphinxRecognizer.cancel();
             //   sleeping = false;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                                String speechCal = " 그리고 오늘은 특정한 일정이 없으십니다.";
                                isCollectingData = false;
                                if(myCal.getEventAndTime() == null || myCal.getEventAndTime().isEmpty()){

                                } else {
                                    speechCal = "그리고 오늘은 ";
                                    for (int i = 0; i < myCal.getEventAndTime().size(); i++) {
                                        speechCal += "\n " + myCal.getEventAndTime().get(i);
                                    }
                                    speechCal += "가 있으십니다.";
                                }

                        ttsManager.initQueue(" 좋은 아침입니다 " + userName + "님. "+ weather.getResponse() + speechCal +" 좋은 하루 되십시요.");
                    }
                }, 5000);

            }
        }
    };


    long diff = 0;
    private BroadcastReceiver powerDoubleclick = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            Log.v("onReceive", "Power button is pressed.");

            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                isForeground = true;

                if(System.currentTimeMillis() - diff < 1000){
                    aiService.startListening();
                    vibe.vibrate(100);
                }
                diff = System.currentTimeMillis();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                if(System.currentTimeMillis() - diff < 1000){
                    aiService.startListening();
                    vibe.vibrate(100);
                }
                diff = System.currentTimeMillis();
            }
        }

    };

 //   private String task;
    private boolean preventTwice = true; // weird b/c of FLAG INCLUDE STOPPED PACKAGE
    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            String task = i.getStringExtra("task");

           // if(preventTwice) {
                Toast.makeText(c, userName + "님, " + task + "하셔야 할 시간입니다!", Toast.LENGTH_LONG).show();
                vibe.vibrate(1500);
                if (!textMode)
                    ttsManager.initQueue(userName + "님, " + task + "하셔야 할 시간입니다!");

          //      preventTwice = false;

                //   unregisterReceiver(br);
          //  }else{
          //      preventTwice = true;
          //  }
        }
    };





    //*** Switch Mode Boolean ***
    private boolean activeStandBy = true;
    private boolean textMode = false;
    private boolean isForeground = false;
    private boolean firstStart = true;
    private boolean changeMode = false;



    private ViewGroup rootLayout = null;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener = null;


    PendingIntent pi;
//    BroadcastReceiver br;
    AlarmManager am;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_main_activity);


        defaultPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        defaultPref.registerOnSharedPreferenceChangeListener(listener);

        userName = getIntent().getStringExtra("userName");
        botName = getIntent().getStringExtra("botName");
        KEYPHRASE = botName.toLowerCase();

        setTitle(botName);

        // AI Configuration & Initiate
        final AIConfiguration config = new AIConfiguration("f25aab40ac6747478e7e62364d63fb63",
                AIConfiguration.SupportedLanguages.Korean,
                AIConfiguration.RecognitionEngine.System);

        aiService = (GoogleRecognitionServiceImpl) AIService.getService(this, config);
        aiService.setListener(this);

        aiDataService = new AIDataService(config);

        // Layout Settings
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        // smooth scrolling
        mRecyclerView.setNestedScrollingEnabled(false);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MessageAdapter(getBaseContext(), messageList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //set variable for text input
        editText = (EditText) findViewById(R.id.edit_text);
        // Needed for Text Message focusing when keyboard up
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect r = new Rect();
                mRecyclerView.getWindowVisibleDisplayFrame(r);
                int screenHeight = mRecyclerView.getRootView().getHeight();

                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                int keypadHeight = screenHeight - r.bottom;

                // Log.d(TAG, "keypadHeight = " + keypadHeight);

                if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                    // keyboard is opened
                    if(messageList.size() != 0)
                    mRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                }
                else {
                    // keyboard is closed
//                    mRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                }
            }
        });
        sendButton = (ImageButton) findViewById((R.id.send_button));
        sendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                sendMessage();
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        updateNotification(defaultPref.getString("mode_list", ""));

        // Register Broadcast Receivers
        IntentFilter powerFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        powerFilter.addAction(Intent.ACTION_SCREEN_ON);
        IntentFilter filter = new IntentFilter(AlarmClock.ACTION_DISMISS_ALARM);
        filter.addAction(AlarmClock.ACTION_SNOOZE_ALARM);
        filter.addAction(ALARM_STOPPED_IN_ALERT_ACTION);
        filter.addAction(ALARM_ALARM_STOP_ACTION);
        registerReceiver(broadcastReceiver, filter);
        registerReceiver(powerDoubleclick, powerFilter);
        registerReceiver(br, new IntentFilter("Alarm"));




        vibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
  //      vibe.vibrate(100);



        // Wiki Response Settings
        wiki = new WikiResponse();

        // weather Settings
        OpenWeatherWithGPS weather = new OpenWeatherWithGPS(this); //Here the context is passing

        // Initiate TTS
        ttsManager = new TTSManager();
        ttsManager.init(this);

        // Initiate Google Cal and set it with the selected account name
        googleAccnt = getIntent().getStringExtra("accountName");
        myCal = new GoogleCalendarCall(getApplicationContext(), googleAccnt);




        //Start Sphinx if active standby. 4/20 Merging Pocket with Main

        if(defaultPref.getString("mode_list", "").equals("항시 대기 모드")){
            PocketSphinxInitiater();
        }



        moveTaskToBack(true);


    } // end of onCreate()


    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            // Implementation
            if ( key.equals("mode_list") ) {
                    if (prefs.getString(key, "").equals("일반 대기 모드")) {
                        if (SphinxRecognizer != null) {
                            SphinxRecognizer.cancel();
                        //    SphinxRecognizer.shutdown();
                        }

                        updateNotification(prefs.getString(key, ""));


                        activeStandBy = false;
                        textMode = false;
                        startModeChange = true;
                        ttsManager.initQueue("일반 대기 모드로 전환합니다. ");
                        messageList.add(new Message("일반 대기 모드로 전환합니다. ", "BOT"));
                        mAdapter.notifyDataSetChanged();
                        mRecyclerView.smoothScrollToPosition(messageList.size() - 1);

                        Toast.makeText(getApplicationContext(), "일반 대기 모드 활성화", Toast.LENGTH_SHORT).show();
                    } else if (prefs.getString(key, "").equals("항시 대기 모드")) {
                        updateNotification(prefs.getString(key, ""));
                        activeStandBy = true;
                        textMode = false;
                        startModeChange = true;
                        ttsManager.initQueue("항시 대기 모드로 전환합니다. ");
                        messageList.add(new Message("항시 대기 모드로 전환합니다. ", "BOT"));
                        mAdapter.notifyDataSetChanged();
                        mRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                        Toast.makeText(getApplicationContext(), "항시 대기 모드 활성화", Toast.LENGTH_SHORT).show();
                        if (SphinxRecognizer != null) {
                            SphinxRecognizer.cancel();
                     //       SphinxRecognizer.shutdown();
                        }
                      //  PocketSphinxInitiater();
                        Toast.makeText(getApplicationContext(), botName + " 항시 대기 중", Toast.LENGTH_LONG).show();
                        SphinxRecognizer.startListening(KWS_SEARCH);
                    } else {
                        if (SphinxRecognizer != null) {
                            SphinxRecognizer.cancel();
                     //       SphinxRecognizer.shutdown();
                        }
                        updateNotification(prefs.getString(key, ""));
                        textMode = true;
                        messageList.add(new Message("채팅 모드로 전환합니다. ", "BOT"));
                        mAdapter.notifyDataSetChanged();
                        mRecyclerView.smoothScrollToPosition(messageList.size() - 1);
                        Toast.makeText(getApplicationContext(), "채팅 모드 활성화", Toast.LENGTH_SHORT).show();
                    }
            }
        }
    };

    public void updateNotification(String mode){
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int notifyID = 1337;
        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.bot)
                .setContentTitle(botName.toUpperCase())
                .setContentText(mode.substring(0, mode.indexOf("모")) + "중")
                .setPriority(Notification.PRIORITY_MAX);

        mNotificationManager.notify(
                notifyID,
                mBuilder.build());
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile_and_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(settings);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


//4/19 Comment out for now we are using POWER BUTTON
    // Also executes when starting the app. For homebutton doubletap/power button
//    @Override
//    public void onResume(){
//        super.onResume();
//        // Should not perform any of these if bringing chat UI
//   //     if(!firstStart) {
//            if (!chatUIUp && !textMode && !searching) {
//                if (SphinxRecognizer != null) {
//                    super.onResume();
//                    isListening = true;
//                    SphinxRecognizer.shutdown();
//                    sleeping = false;
//                    aiService.startListening();
//                    vibe.vibrate(100);
//                    moveTaskToBack(true);
//                } else {
//                    isListening = true;
//                    sleeping = false;
//                    aiService.startListening();
//                    vibe.vibrate(100);
//                    moveTaskToBack(true);
//                }
//            }
//            if (textMode && !isForeground && !searching) {
//                // For double Tap, if chat mode, then just bring chat ui
//                isForeground = true;
//                vibe.vibrate(100);
//                PackageManager packageManager = getApplicationContext().getPackageManager();
//                Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
//                if (intent != null) {
//                    intent.setPackage(null);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    getApplicationContext().startActivity(intent);
//                }
//            }
//    //    }
//    //    firstStart = false;
//    }

    @Override
    public void onPause(){
        super.onPause();
        isForeground = false;
    }

    @Override
    public void onBackPressed(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START, true);
        } else {
            //super.onBackPressed();
           // onPause();
            moveTaskToBack(true);
        }
    }

    public boolean getStartModeChange(){
        return startModeChange;
    }

    public void setStartModeChange(boolean set){
        startModeChange = set;
    }

    public boolean getIsTextMode(){return textMode;}

    public boolean getCollectingState(){
        return isCollectingData;
    }

    public boolean getCallingState(){
        return calling;
    }

    public boolean getIsSearchng() {
        return searching;
    }

    public boolean getIsChatUIUp() {
        return chatUIUp;
    }

    public boolean getIsSleeping(){
        return sleeping;
    }

    public AIService getAiService(){
        return aiService;
    }

    public boolean getIsListening(){
        return isListening;
    }

    public void setIsListening(boolean set){
        isListening = set;
    }

    public void onResult(final AIResponse response) { // start of onResult(AIResponse) *************************************
        Result result = response.getResult();

        // Get parameters
        String parameterString = "";
        String SMSResponse = "";
        String SMSreceiver = "";
        String receiverNum = "";

        String callReceiver = "";
        String searchKeyword = "";
        String setTime = "";

        //onResult is called each time it records so its fine to not make it false again.
        chatUIUp = false;
        boolean sending = false;
        calling = false;
        //boolean sleeping = false;
        searching = false;
        boolean weatherCheck = false;
      //  sleeping = false;
        boolean settingAlarm = false;
        String textModeMessage = "";
        boolean timeCheck = false;
        changeMode = false;

        messageList.add(new Message(result.getResolvedQuery(), "USER"));
        mAdapter.notifyDataSetChanged();
        mRecyclerView.smoothScrollToPosition(messageList.size() - 1);


//        if(result.getAction().equals("playDefaultMusicPlayer")){
//
//            PackageManager packageManager = getApplicationContext().getPackageManager();
//            Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
//            if (intent != null) {
//                intent.setPackage(null);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                getApplicationContext().startActivity(intent);
//            }
//
//            Intent mintent = new Intent();
//            mintent.setAction(android.content.Intent.ACTION_VIEW);
//            Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC);
//            startActivity(mintent);
//        }

        if(result.getAction().equals("alarmManager")){
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, result.getIntParameter("number"));
            Toast.makeText(getApplicationContext(), result.getIntParameter("number") + "분 뒤에 알려드리겠습니다", Toast.LENGTH_SHORT).show();
            String task = result.getStringParameter("any");

            Intent alarmIntent = new Intent("Alarm");
            alarmIntent.putExtra("task", task);
            alarmIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); // to make it work even after the app is killed accidently

            final int _id = (int) System.currentTimeMillis(); // set unique id for each alarm so I can set multiple
            pi = PendingIntent.getBroadcast( this, _id, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
            am = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));
            //am.set( AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, pi);
            //If lollipop or higher, use setExact(set() is not accurate from here, but battery saving)
            if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                  am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            else
                am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        }

        if(result.getAction().equals("whatTime")){
            timeCheck = true;
            whatTime(result.getStringParameter("time"));
        }

        if(result.getAction().equals("silentChatMode")){
            changeMode = true;
            // Shutdown the recognizer first
            if(SphinxRecognizer != null){
                SphinxRecognizer.cancel();
          //      SphinxRecognizer.shutdown();
            }
            SharedPreferences.Editor editor = defaultPref.edit();
            editor.putString("mode_list", "채팅 모드");
            editor.apply();

            textMode = true;
            activeStandBy = false;  // To shut down active listening.
            //ttsManager.initQueue(result.getFulfillment().getSpeech());
            Toast.makeText(getApplicationContext(), "채팅 모드 활성화", Toast.LENGTH_SHORT).show();
            PackageManager packageManager = getApplicationContext().getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
            if (intent != null) {
                intent.setPackage(null);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
            }
        }

        //Switch to Active Standby Mode
        if(result.getAction().equals("activeStandBy")){
            changeMode = true;
            SharedPreferences.Editor editor = defaultPref.edit();
            editor.putString("mode_list", "항시 대기 모드");
            editor.apply();

            activeStandBy = true;
            textMode = false;
            Toast.makeText(getApplicationContext(), "항시 대기 모드 활성화", Toast.LENGTH_SHORT).show();


        }

        //Switch to Battery Saving (Normal) Mode
        if(result.getAction().equals("batterySavingStandBy")) {
            changeMode = true;
            //Shutdown the recognizer first
            if(SphinxRecognizer != null){
                SphinxRecognizer.cancel();
       //         SphinxRecognizer.shutdown();
            }
            SharedPreferences.Editor editor = defaultPref.edit();
            editor.putString("mode_list", "일반 대기 모드");
            editor.apply();


            activeStandBy = false;
            textMode = false;

            Toast.makeText(getApplicationContext(), "일반 대기 모드 활성화", Toast.LENGTH_SHORT).show();
        }

        // Insert an event in Google Calendar
        if(result.getAction().equals("insertEvent")){
            insertGoogleCalEvent(result);
        }

        // Brings chatting UI foreground
        if(result.getAction().equals("bringChatUI")){
            if(!textMode) {
                bringChatUI(result);
            } else {
                textModeMessage = "현재 채팅 모드여서 이미 여기 있어요!";
            }
        }

        // Briefly inform today's schedule according to Google Calendar
        if(result.getAction().equals("googleCalendarCall")){
            googleCalCall();
        }


        // Enter Sleep Mode
//        if(result.getAction().equals("sleepMode")){
//            // textMode, so cannot perorm sleepMode
//            if(!textMode) {
//                ttsManager.initQueue(result.getFulfillment().getSpeech());
//                Toast.makeText(getApplicationContext(), "Sleep Mode Activated", Toast.LENGTH_SHORT).show();
//                PocketSphinxInitiater();
//                sleeping = true;
//            }else{
//                textModeMessage = "현재 채팅 모드여서 대기할 수 없습니다. 항시 대기 모드나 일반 대기 모드로 전환해 주세요.";
//            }


            //If you do THIS below, it will not restart when sleep after terminating
            //   sleepMode.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            //  Intent intent = getPackageManager().getLaunchIntentForPackage("com.google.an‌​droid.gm");
            //   intent.setClassName("com.google.android.gm", "com.google.android.gm.ConversationListActivityGmail");
            //   startActivity(intent);
       /*     new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 2300);*/

     //   }

        // Informs today's weather information
        if(result.getAction().equals("weatherCheck")){
            weatherCheck = weatherCall(weatherCheck);
        }

        // Collect required entries that will be used in result from this if statement
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";

                //Get SMSResponse
                if(result.getAction().equals("Sending")){
                    if(entry.getKey().equals("SMSContent") && !entry.getValue().toString().equals("[]")){
                        SMSResponse = entry.getValue().getAsString();
                        sending = true;
                    }
                    if(entry.getKey().equals("names") && !entry.getValue().toString().equals("[]")) {
                        SMSreceiver = entry.getValue().getAsString();
                    }
                }

                //Call
                if(result.getAction().equals("Call")){
                    if(entry.getKey().equals("names") && !entry.getValue().toString().equals("[]")){
                        callReceiver = entry.getValue().getAsString();
                        calling = true;
                    }
                }

                //Search
                if(result.getAction().equals("googleSearch")){
                    if(entry.getKey().equals("any") && !entry.getValue().toString().equals("[]")){
                        searchKeyword = entry.getValue().getAsString();
                        searching = true;
                    }
                }

                //Setting Alarm
                if(result.getAction().equals("setAlarm")){
                    if(entry.getKey().equals("time") && !entry.getValue().toString().equals("[]")){
                        setTime = entry.getValue().getAsString();
                        settingAlarm = true;
                    }
                }
            }
        }

        if(settingAlarm){
            Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
            i.putExtra(AlarmClock.EXTRA_HOUR, Integer.parseInt(setTime.substring(0, 2)));
            i.putExtra(AlarmClock.EXTRA_MINUTES, Integer.parseInt(setTime.substring(3, 5)));
            i.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
            i.putExtra(AlarmClock.ALARM_SEARCH_MODE_LABEL, "Alarm");
            startActivity(i);

//            ttsManager.initQueue(result.getFulfillment().getSpeech());
//            messageList.add(new Message(result.getFulfillment().getSpeech(), "BOT"));
//            mAdapter.notifyDataSetChanged();
//            mRecyclerView.smoothScrollToPosition(messageList.size() - 1);
        }


        // Calls the callReceiver
        if(calling){
          calling(callReceiver);
        }

        //Sending SMS Message
        if(sending) {
            //Get Rid of All space
            SMSreceiver = SMSreceiver.replaceAll("\\s", "");
            //Find the phone number
            receiverNum = findMatchContact(SMSreceiver);
            //send Message
            sendSMS(receiverNum, SMSResponse);
        }

        // Performs Google Search
        if(searching) {
            googleSearch(result, searchKeyword);
        }

        // If not searching or weatherCheck, do original message.
        //Calling, Sending, setting alarm, bringChatUI uses this
        if(!searching && !weatherCheck && !timeCheck &&!changeMode) {
            //Bot Response Message
            if(!textMode) {
                ttsManager.initQueue(result.getFulfillment().getSpeech());
                messageList.add(new Message(result.getFulfillment().getSpeech(), "BOT"));
            } else{
                if(textModeMessage.equals(""))
                    messageList.add(new Message(result.getFulfillment().getSpeech(), "BOT"));
                else
                    messageList.add(new Message(textModeMessage, "BOT"));
            }
            mAdapter.notifyDataSetChanged();
            mRecyclerView.smoothScrollToPosition(messageList.size() - 1);

            if(!chatUIUp && !textMode)
                moveTaskToBack(true);
        }




        //Erase for Testing on Chat UI
        /*
        // Show results in TextView.
        resultTextView.setText("Query:" + result.getResolvedQuery() +
                "\nAction: " + result.getAction() +
                "\nParameters: " + parameterString + "\n" + result.getFulfillment().getSpeech() +
                result.getFulfillment().getDisplayText());
                */


    } // end of onResult(AIResponse) **********************************************************************


    public void whatTime(String time){
        myCal.execGetTwoHourTask();


        String hour = time.substring(0, 2);
        int intHour = Integer.parseInt(hour);
        String minute = time.substring(3, 5);
        int intMinute = Integer.parseInt(minute);
        String amOrPm = "";

        if(intHour > 12){
            intHour -= 12;
            amOrPm = "오후";
        } else if(intHour == 12) {
            amOrPm = "오후";
        }else
            amOrPm = "오전";

        if(intHour == 0){
            intHour = 12;
        }

       final String finalTime;

        if(intMinute == 0)
            finalTime = "현재 "+ amOrPm + " " +  intHour +"시입니다. ";
        else if (intMinute == 30)
            finalTime = "현재 " + amOrPm + " " + intHour +"시 반입니다. ";
        else
            finalTime = "현재 " + amOrPm + " " + intHour +"시 " + intMinute + "분"  + "입니다. ";




        new Handler().postDelayed(new Runnable() {
            String speak = "";
            @Override
            public void run() {
                //prints null of not exist
                String formatted = finalTime;

                // if there is an event within 2 hours
                if(myCal.getTwoHourActList().size() != 0 || !myCal.getTwoHourActList().isEmpty()){
                    String event = myCal.getTwoHourActList().get(0);
                    String[] splitted = event.split("  ////  ");
                    String summ = splitted[0];
                    String dateTime = splitted[1];

                    java.util.Calendar futCalendar = java.util.Calendar.getInstance();
                    java.util.Calendar currCalendar = java.util.Calendar.getInstance();

                    dateTime = dateTime.substring(0, 20);
                    java.text.SimpleDateFormat dt    = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date future = new Date();
                    try {
                        future = dt.parse(dateTime);
                    } catch (ParseException ex) {
                        Toast.makeText(MainActivity.this, "Parsing ERROR??", Toast.LENGTH_SHORT).show();
                    }

                    futCalendar.setTime(future);
                    currCalendar.setTime(new Date());
                    long diff = futCalendar.getTimeInMillis() - currCalendar.getTimeInMillis();
                    diff = diff/DateUtils.SECOND_IN_MILLIS;
                    //  String formatted = DateUtils.formatElapsedTime(diff);

                    long s = diff % 60;
                    long m = (diff / 60) % 60;
                    long h = (diff / (60 * 60)) % 24;
                    formatted = String.format("%02d:%02d:%02d", h,m,s);

                    if(h != 0){
                        formatted = finalTime + h +"시간 " + m + "분 뒤에 " + summ + "가 있으시네요?";
                    } else {
                        formatted = finalTime + m + "분 뒤에 " + summ + "가 있으신 것 아시죠?";
                    }
                } //  end if

                if(!textMode)
                    ttsManager.initQueue(formatted);

                messageList.add(new Message(formatted, "BOT"));
                mAdapter.notifyDataSetChanged();
                mRecyclerView.smoothScrollToPosition(messageList.size() - 1);

                //reset two hour act list to null
                myCal.getTwoHourActList().clear();

            }
        }, 700);


}


    public void insertGoogleCalEvent(Result result){
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            String summary = result.getStringParameter("summary");
            String AMorPM = result.getStringParameter("TimeAMPM");

            String time = result.getStringParameter("time");
            String dateString = result.getStringParameter("date");

            Date date = new Date();
            boolean allday = false;

            // If it is today
            if(dateString.equals("today")) {
                dateString = new SimpleDateFormat("yyyy-MM-dd").format(date);
            } else{
                date = result.getDateParameter("date");
            }

            String endDateString = dateString;

            // set if it is allday event
            if(time.equals("")){
                allday = true;
            }


            //Date Format for getting day name ex) Friday
            java.text.SimpleDateFormat dayName = new java.text.SimpleDateFormat("EEEE", Locale.ENGLISH);
            Date todayDate = new Date();
            String todayName = dayName.format(todayDate);

            java.util.Calendar calendar = java.util.Calendar.getInstance();
            int num = 0;
            if(todayName.equals("Monday")){
                num = 7;
            } if(todayName.equals("Tuesday")){
                num = 6;
            } if(todayName.equals("Wednesday")){
                num = 5;
            } if(todayName.equals("Thursday")){
                num = 4;
            } if(todayName.equals("Friday")){
                num = 3;
            } if(todayName.equals("Saturday")){
                num = 2;
            } if(todayName.equals("Sunday")){
                num = 1;
            }

            calendar.setTime(todayDate);
            calendar.add(java.util.Calendar.DATE, num - 1);
            Date nextWeekStartDate = calendar.getTime();

            calendar.setTime(todayDate);
            calendar.add(java.util.Calendar.DATE, 14 -(7 - num));
            Date nextWeekEndDate = calendar.getTime();

            SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd");
            //Check if there is nextweek(다음 주)
            if(!result.getStringParameter("nextWeek").equals("")){
                //compare dates

                if(date.after(nextWeekStartDate) && date.before(nextWeekEndDate)){
                    dateString = dFormat.format(date);
                } else{ // add 7 more days
                    calendar.setTime(date);
                    calendar.add(java.util.Calendar.DATE, 7);
                    date = calendar.getTime();
                    Log.d("Revised Date", date.toString()); // correct
                    dateString = dFormat.format(date);
                }
            } else { // if there is no nextweek, just go ahead

            }

            if(!allday)
                endDateString = dateString;
            else {
                calendar.setTime(date);
                calendar.add(java.util.Calendar.DATE, 1);
                date = calendar.getTime();
                endDateString = dFormat.format(date);
            }

            // Calculates current GMT offset, including daylight savings. getRawOffSet() does not include
            Date now = new Date();
            int offset = TimeZone.getDefault().getOffset(now.getTime());
            String gmtTZ = String.format("%s%02d:%02d",
                    offset < 0 ? "-" : "+",
                    Math.abs(offset) / 3600000,
                    Math.abs(offset) / 60000 % 60);
            Log.d("TIME", now.toString()); // Fri Apr 14 11:45:53 GMT-04:00 2017

            //default for all-day event
            String newHour = "00:00:00";
            String defaultEndHour = "00:00:00";
            //set if it is allday or not
            if(time.equals("")){
                allday = true;
            }else { // if it is not an all-day event
                newHour = time;
                if(AMorPM.equals("PM")) {
                    String hour = time.substring(0, 2);
                    if(Integer.parseInt(hour) < 12)
                        newHour = String.format("%02d", Integer.parseInt(hour) + 12);
                    else
                        newHour = hour;
                    newHour = newHour + time.substring(2);
                }
                //set default end hour as +1 hour
                defaultEndHour = newHour.substring(0, 2);
                defaultEndHour = String.format("%02d", Integer.parseInt(defaultEndHour) + 1) + newHour.substring(2);
            }


            String finalEventTime = dateString + "T" + newHour + gmtTZ;
            String finalDefaultEndTime = endDateString + "T" + defaultEndHour +gmtTZ;
            //myCal.createEventAsync("Test Successs222", "Atlanta", "desc",  new DateTime("2017-04-14T12:00:00"),  new DateTime("2017-04-14T13:10:00"));
            if(allday)
                myCal.createEventAsync(summary, "", "", new DateTime(dateString), new DateTime(endDateString), allday);
            else
                myCal.createEventAsync(summary, "", "", new DateTime(finalEventTime), new DateTime(finalDefaultEndTime), allday);
        }
    }

    public void bringChatUI(Result result){
        chatUIUp = true;
 //       sleeping = true;
 //       Toast.makeText(getApplicationContext(), "Sleep Mode Activated", Toast.LENGTH_SHORT).show();

        PackageManager packageManager = getApplicationContext().getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.setPackage(null);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
        }

        //For now 대기타게.. 나중에는 채팅모드만들면 해제
        if(activeStandBy) {
         //   PocketSphinxInitiater();
            Toast.makeText(getApplicationContext(), botName + " 항시 대기 중", Toast.LENGTH_LONG).show();
            SphinxRecognizer.startListening(KWS_SEARCH);
        }
        //  chatUIUp = false;
    }




    public void googleCalCall(){
        myCal.getResultsFromApi();
        isCollectingData = true;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String speechCal = "오늘은 특정한 일정이 없으십니다.";
                isCollectingData = false;
                if(myCal.getEventAndTime() == null || myCal.getEventAndTime().isEmpty()){
                    ttsManager.initQueue(speechCal);
                } else {
                    speechCal = "오늘은 ";
                    for (int i = 0; i < myCal.getEventAndTime().size(); i++) {
                        speechCal += "\n " + myCal.getEventAndTime().get(i);
                    }
                    speechCal += "가 있으십니다. 좋은 하루 되십시요.";
                    if(!textMode)
                        ttsManager.initQueue(speechCal);
                }
                //   Toast.makeText(getApplicationContext(), myCal.getEventAndTime().get(0), Toast.LENGTH_LONG).show();
                //  ttsManager.initQueue(myCal.getEventAndTime().get(2));
                messageList.add(new Message(speechCal, "BOT"));
                mAdapter.notifyDataSetChanged();
                mRecyclerView.smoothScrollToPosition(messageList.size() - 1);

                //          myCal.createEventAsync("Hellooo", "Atlanta", "yolo", new DateTime("2017-04-17T18:10:00+06:00"), new DateTime("2017-04-17T20:10:00+06:00"));
            }
        }, 2000);


    }

    public void googleSearch(final Result result, final String searchKeyword){
        wiki.wikiResponseExec(searchKeyword);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!textMode) {
                    searching = false;
                    ttsManager.initQueue(result.getFulfillment().getSpeech() + wiki.getRetStr());

                }
                messageList.add(new Message(searchKeyword + "을(를) 검색합니다.", "BOT"));
                mAdapter.notifyDataSetChanged();
                mRecyclerView.smoothScrollToPosition(messageList.size() - 1);



                PackageManager packageManager = getApplicationContext().getPackageManager();
                Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
                if (intent != null) {
                    intent.setPackage(null);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                }

                    webIntent = new Intent(Intent.ACTION_WEB_SEARCH);
                    webIntent.putExtra(SearchManager.QUERY, searchKeyword);
                    //webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                  //  webIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    startActivity(webIntent);

//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        searching = false;
//                    }
//                }, 1000);

//                if(!textMode) {
//                    PocketSphinxInitiater();
//                    sleeping = true;
//                    // Enter SleepMode
//                    Toast.makeText(getApplicationContext(), "Sleep Mode Activated", Toast.LENGTH_SHORT).show();
//                }
            }
        }, 800);







    }

    public void calling(String callReceiver){
        callReceiver = callReceiver.replaceAll("\\s", "");
        String phoneNum = findMatchContact(callReceiver);


        String number = "tel:" + phoneNum;
        final Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number));

        // Security Exception Required


        try{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    PackageManager packageManager = getApplicationContext().getPackageManager();
                    Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
                    if (intent != null) {
                        intent.setPackage(null);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                    }
                    startActivity(callIntent);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                         //   activateSilentSleepMode();
                            moveTaskToBack(true);
                        }
                    }, 2000);
                }
            }, 2000);
        } catch (SecurityException e)
        {
            messageList.add(new Message("보안상 문제로 전화를 걸 수 없습니다.", "BOT"));
            mAdapter.notifyDataSetChanged();
            mRecyclerView.smoothScrollToPosition(messageList.size() - 1);
            if(!textMode)
                ttsManager.initQueue("보안상 문제로 전화를 걸 수 없습니다.");
        }

    }

    public boolean weatherCall(boolean weatherCheck){

        // try Volley
        weather.getLocation();
        weather.getWeatherData(weather.lat, weather.lng);


        //weather.getWeatherData(23, 24);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!textMode)
                ttsManager.initQueue(weather.getResponse());

                messageList.add(new Message(weather.getResponse(), "BOT"));
                mAdapter.notifyDataSetChanged();
                mRecyclerView.smoothScrollToPosition(messageList.size() - 1);
            }
        }, 1000);
        return weatherCheck = true;
    }







    public void sendMessage(){
        // Don't need to. Handled in onResult()
//        messageList.add(new Message(editText.getText().toString(), "USER"));
//        mAdapter.notifyDataSetChanged();
//        mRecyclerView.smoothScrollToPosition(messageList.size()-1);

        if(!editText.getText().toString().isEmpty())
            new DoTextRequestTask().execute(editText.getText().toString());
        //navigateUsingText(editText.getText().toString());
        editText.getText().clear();
    }

    // Option 1: Use textRequest for Messaging
    class DoTextRequestTask extends AsyncTask<String, Void, AIResponse> {
        private Exception exception = null;
        protected AIResponse doInBackground(String... text) {
            AIResponse resp = null;
            try {
                resp = aiService.textRequest(text[0], new RequestExtras());
            } catch (Exception e) {
                this.exception = e;
            }
            return resp;
        }
        protected void onPostExecute(AIResponse response) {
            if (this.exception == null) {
                // todo : handle the exception
            }
            // Send the response to onResult to handle situations
            onResult(response);

//            messageList.add(new Message(response.getResult().getFulfillment().getSpeech(), "BOT"));
//            mAdapter.notifyDataSetChanged();
//            mRecyclerView.smoothScrollToPosition(messageList.size()-1);
//
//            ttsManager.initQueue(response.getResult().getFulfillment().getSpeech());
        }
    }

    // Option 2: Use normal request to get response for Messaging
    public void navigateUsingText(String text) {
        // Setup a request object
        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery(text);
        //Perform a asynchronous task to understand the text
        new AsyncTask<AIRequest, Void, AIResponse>() {
            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                try {
                    final AIResponse response = aiDataService.request(aiRequest);
                    return response;
                } catch (AIServiceException e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(AIResponse aiResponse) {
                onResult(aiResponse);

//                messageList.add(new Message(aiResponse.getResult().getFulfillment().getSpeech(), "BOT"));
//                mAdapter.notifyDataSetChanged();
//                mRecyclerView.smoothScrollToPosition(messageList.size()-1);
//
//                ttsManager.initQueue(aiResponse.getResult().getFulfillment().getSpeech());
            }
        }.execute(aiRequest);
    }

//
//    public void listenButtonOnClick(final View view) {
//        isListening = true;
//        aiService.startListening();
//
//
//
//
//    }

    public void sendSMS(String phoneNumber, String message)
    {
        if(phoneNumber.equals("")){
            if(!textMode)
                ttsManager.initQueue("전화 번호를 찾을 수 없습니다.");

            messageList.add(new Message("전화 번호를 찾을 수 없습니다.", "BOT"));
            mAdapter.notifyDataSetChanged();
            mRecyclerView.smoothScrollToPosition(messageList.size() - 1);
        } else {
            PackageManager packageManager = getApplicationContext().getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
            if (intent != null) {
                intent.setPackage(null);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
            }
            PendingIntent pi = PendingIntent.getActivity(this, 0,
                    new Intent(this, SMS.class), 0);
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, pi, null);

            if(!textMode)
                moveTaskToBack(true);
        }
    }


//    public void activateSilentSleepMode(){
//        if(!sleeping && !textMode) {
//            Toast.makeText(getApplicationContext(), "Sleep Mode Activated", Toast.LENGTH_SHORT).show();
//            //aiService.pause();
//
//            sleeping = true;
//            PocketSphinxInitiater();
//            moveTaskToBack(true);
//
//        }
//    }

    public void getCombinations(String str){
        for (int i = 1; i < str.length(); i++) {
            String newStr = str.substring(0,i) + " " + str.substring(i, str.length());
            possibleComb.add(newStr);
        }
    }

    // Finds the matching phone number using name and combinations
    public String findMatchContact(String receiverName){

        possibleComb.clear();

        //Add nonspace first
        possibleComb.add(receiverName);
        // Get Combinations with spaces
        getCombinations(receiverName);


        String newphoneNum = null;

        //
        for (String rec: possibleComb) {
            Cursor cursor = null;
            try {
                cursor = getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                int contactIdIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID);
                int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int phoneNumberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int photoIdIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID);
                cursor.moveToFirst();
                do {
                    String idContact = cursor.getString(contactIdIdx);
                    String name = cursor.getString(nameIdx);
                    String phoneNumber = cursor.getString(phoneNumberIdx);

                    if (name.equals(rec)) {
                        newphoneNum = phoneNumber;
                        //break out the loop
                        break;
                    }

                    //...
                } while (cursor.moveToNext());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } // end of foreach loop

        return newphoneNum;

    }


    //Used when nothing is heard
    @Override
    public void onError(final AIError error) {
        Log.d("ERR", "onError: ERRORRR");

        aiService.cancel();

    }




        @Override
    public void onListeningStarted() {
        isListening = true;


        Log.d("@@@@@@@@@@@@@@@@@@", "ONLISTENING STARTED");
////           final int newRecogActive = ++recogActive;
////            int decider = recogActive;
//
            new CountDownTimer(10000, 1000) {

                public void onTick(long millisUntilFinished) {
                    //do nothing, just let it tick 1초마다 한번씩 돌아감
                    Log.d("@@@@@", "TICKINGGGGGGG");

                    //Finished Listening so cancel Ticking
                    if(!isListening){
                        Log.d("@@@@@", "TICKING CANCELLED");
                        cancel();
                    }
                }

                public void onFinish() {
                    Log.d("@@@@@", "STOP AFTER TICKINGGG");

                    //Means It got stuck so sleep
                    if(isListening && !searching) {
                        Log.d("@@@@@", "STOP AFTER TICKINGGG RECOG TRUE = 그대로여야");
                        aiService.cancel();      // aiservice.stoplistening = 두둥 사운드
                        aiService.pause();
//                        if(!textMode && activeStandBy){
//                            PocketSphinxInitiater();
//                        }
                    }
                }
            }.start();



        //If do a handler here, then will have some problems. Works smooth 90% if leave it like this

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if(isListening && !ttsManager.isSpeaking()) {
//                    Log.d("@@@@@@@@@@@@@@@@@@", "FORCE STOPPING");
//                    aiService.stopListening();
//                    activateSilentSleepMode();
//                }
//                }
//
//        }, 10000);
    }


    //거의 안불려짐
    @Override
    public void onListeningCanceled() {
        isListening = false;
        Log.d("@@@@@@@@@@@@@@@@@@", "ONLISTENING CANCELED");


    //    activateSilentSleepMode();
        if(!activeStandBy && !textMode){
            Toast.makeText(getApplicationContext(), botName + " 일반 대기 중", Toast.LENGTH_LONG).show();
        }

        if(activeStandBy) {
            Toast.makeText(getApplicationContext(), botName + " 항시 대기 중", Toast.LENGTH_LONG).show();
            SphinxRecognizer.startListening(KWS_SEARCH);
        }
            //PocketSphinxInitiater();


    }

    @Override
    public void onListeningFinished() {
        isListening = false;
        Log.d("@@@@@@@@@@@@@@@@@@", "ONLISTENING FINISHED");
  //      activateSilentSleepMode();
//        if(defaultPref.getString("mode_list", "").equals("항시 대기 모드"))
//            PocketSphinxInitiater();


//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (!isListening && !ttsManager.isSpeaking() && !sleeping) {
//                    Log.d("SPEAKING???", String.valueOf(ttsManager.isSpeaking()));
//                    Log.d("@@@@@@@@@@@@@@@@@@", "ONLISTENING FINISHED AFTER 5 SEC");
//                    // Need this to clear well
//                    aiService.pause();
//                    Log.d("@@@@@@@@@@@@@@@@@@", "PAUSED WITH CLEARING");
//                    activateSilentSleepMode();
//
//                }
//            }
//        }, 5000);

    }
    @Override
    public void onAudioLevel(final float level) {}



    /**
     * Releases the resources used by the TextToSpeech engine.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        ttsManager.shutDown();
        finish();
    }


    //**************************************PockSphinx Recognizer*********************************//

    // Initializer method for PocketSphinx
    public void PocketSphinxInitiater(){
        //stop current recognition service
//        if(aiService != null)
//            aiService.stopListening();



        Log.d("POCKETSPHINXINITIATER", "INITIATING  >>>>>>>>>>>>>>>>>>>>>");
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(getApplicationContext());
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    Toast.makeText(MainActivity.this, "Failed to init recognizer " + result.toString(), Toast.LENGTH_LONG).show();

                } else {

                    isListening = true;
                    // Disable for battery testing. Perform when it is in active standby mode
                    if(activeStandBy && !textMode) {
                        SphinxRecognizer.startListening(KWS_SEARCH);
                        Toast.makeText(getApplicationContext(), botName + " 항시 대기 중", Toast.LENGTH_LONG).show();
                    }
                    //switchSearch(KWS_SEARCH);
                }
            }
        }.execute();

    }


    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        if (text.equals(KEYPHRASE)){
            SphinxRecognizer.stop();
            Log.d("LOOPP????", "@@@@@@@@@@@@@@@@@@@@@@@무한루프????????");
            Toast.makeText(getApplicationContext(), botName + "입니다", Toast.LENGTH_SHORT).show();
        }



       /* else if (text.equals(DIGITS_SEARCH))
            switchSearch(DIGITS_SEARCH);
        else if (text.equals(PHONE_SEARCH))
            switchSearch(PHONE_SEARCH);
        else if (text.equals(FORECAST_SEARCH))
            switchSearch(FORECAST_SEARCH);
        else
            ((TextView) findViewById(R.id.result_text)).setText(text);*/
    }

    /**
     * This callback is called when we stop the recognizer.
     * In here, it is used as calling back to foreground after SLEEP MODE
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
    //    SphinxRecognizer.shutdown();
        aiService.startListening();
        vibe.vibrate(100);
      //  sleeping = false;


    }

    @Override
    public void onBeginningOfSpeech() {

    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {

        //       switchSearch(KWS_SEARCH);
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        SphinxRecognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))

                // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                //.setRawLogDir(assetsDir)

                // Threshold to tune for keyphrase to balance between false alarms and misses
                .setKeywordThreshold(1e-5f)

                // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setBoolean("-allphone_ci", true)

                .getRecognizer();
        SphinxRecognizer.addListener(this);

        /** In your application you might not need to add all those searches.
         * They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        SphinxRecognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

    }

    @Override
    public void onTimeout() {
        //  switchSearch(KWS_SEARCH);
    }
    @Override
    public void onError(Exception error) {
        Log.d("POCKETSPHINX IN MAIN", "onERRORRRRRRR");

    }
    //**************************************PockSphinx Recognizer*********************************//



} //  end of class Main Activity
