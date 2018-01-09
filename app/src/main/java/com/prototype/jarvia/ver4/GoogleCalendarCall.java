package com.prototype.jarvia.ver4;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.calendar.CalendarScopes;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.*;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;
//import android.text.TextUtils;
//import android.text.method.ScrollingMovementMethod;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.LinearLayout;
//import android.widget.TextView;

import java.io.IOException;
import java.text.DateFormat;
import java.util.*;
import java.util.Calendar;

public class GoogleCalendarCall {
    //implements EasyPermissions.PermissionCallbacks
    GoogleAccountCredential mCredential;
//    private TextView mOutputText;
//    private Button mCallApiButton;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String BUTTON_TEXT = "Call Google Calendar API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR, CalendarScopes.CALENDAR_READONLY };

    private Context mainContext;
    private List<String> eventAndTime;
    private com.google.api.services.calendar.Calendar mService = null;
    MainActivity mAct = new MainActivity();

    private List<String> twoHourActList = new ArrayList<String>();


    public GoogleCalendarCall(Context cont, String accntName){
        mainContext = cont;
        initializeCredential(accntName);
    }

    public List<String> getEventAndTime(){
        return eventAndTime;
    }

    public List<String> getTwoHourActList(){
        return twoHourActList;
    }

    public void setTwoHourActList(){
        twoHourActList.clear();
    }

    public void runCreateEvent(){
    }

//    public void createEvent() {
//
//        HttpTransport transport = AndroidHttp.newCompatibleTransport();
//        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
//        com.google.api.services.calendar.Calendar service = new com.google.api.services.calendar.Calendar.Builder(
//                transport, jsonFactory, mCredential)
//                .setApplicationName("R_D_Location Callendar")
//                .build();
//
//
//        Event event = new Event()
//                .setSummary("Event- April 2017")
//                .setLocation("Dhaka")
//                .setDescription("New Event 1");
//
//        DateTime startDateTime = new DateTime("2017-04-14T18:10:00+06:00");
//        EventDateTime start = new EventDateTime()
//                .setDateTime(startDateTime)
//                .setTimeZone("Asia/Dhaka");
//        event.setStart(start);
//
//        DateTime endDateTime = new DateTime("2017-04-14T18:40:00+06:00");
//        EventDateTime end = new EventDateTime()
//                .setDateTime(endDateTime)
//                .setTimeZone("Asia/Dhaka");
//        event.setEnd(end);
//
//        String[] recurrence = new String[]{"RRULE:FREQ=DAILY;COUNT=2"};
//        event.setRecurrence(Arrays.asList(recurrence));
//
//        EventAttendee[] attendees = new EventAttendee[]{
//                new EventAttendee().setEmail("abir@aksdj.com"),
//                new EventAttendee().setEmail("asdasd@andlk.com"),
//        };
//        event.setAttendees(Arrays.asList(attendees));
//
//        EventReminder[] reminderOverrides = new EventReminder[]{
//                new EventReminder().setMethod("email").setMinutes(24 * 60),
//                new EventReminder().setMethod("popup").setMinutes(10),
//        };
//        Event.Reminders reminders = new Event.Reminders()
//                .setUseDefault(false)
//                .setOverrides(Arrays.asList(reminderOverrides));
//        event.setReminders(reminders);
//
//        String calendarId = "primary";
//        try {
//            event = service.events().insert(calendarId, event).execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.printf("Event created: %s\n", event.getHtmlLink());
//
//    }


    public void initializeCredential(String accountEmail){

        mProgress = new ProgressDialog(mainContext);
        mProgress.setMessage("Calling Google Calendar API ...");

     //   setContentView(activityLayout);

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                mainContext, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        mCredential.setSelectedAccountName(accountEmail);
    }





    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    public void getResultsFromApi() {
//        if (! isGooglePlayServicesAvailable()) {
//            acquireGooglePlayServices();
//        } else if (mCredential.getSelectedAccountName() == null) {
//            chooseAccount();
//        } else if (! isDeviceOnline()) {
////           mOutputText.setText("No network connection available.");
//        } else {
            MakeRequestTask req = new MakeRequestTask(mCredential);
            AsyncTaskCompat.executeParallel(req);
//        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    //보류 왜냐면 이건 처음에 시작할 때 써야할듯
//    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
//    private void chooseAccount() {
//        if (EasyPermissions.hasPermissions(
//                mainContext, Manifest.permission.GET_ACCOUNTS)) {
//            String accountName = getPreferences(Context.MODE_PRIVATE)
//                    .getString(PREF_ACCOUNT_NAME, null);
//            if (accountName != null) {
//                mCredential.setSelectedAccountName(accountName);
//                getResultsFromApi();
//            } else {
//                // Start a dialog from which the user can choose an account
//                startActivityForResult(
//                        mCredential.newChooseAccountIntent(),
//                        REQUEST_ACCOUNT_PICKER);
//            }
//        } else {
//            // Request the GET_ACCOUNTS permission via a user dialog
//            EasyPermissions.requestPermissions(
//                    this,
//                    "This app needs to access your Google account (via Contacts).",
//                    REQUEST_PERMISSION_GET_ACCOUNTS,
//                    Manifest.permission.GET_ACCOUNTS);
//        }
//    }

//    /**
//     * Called when an activity launched here (specifically, AccountPicker
//     * and authorization) exits, giving you the requestCode you started it with,
//     * the resultCode it returned, and any additional data from it.
//     * @param requestCode code indicating which activity result is incoming.
//     * @param resultCode code indicating the result of the incoming
//     *     activity result.
//     * @param data Intent (containing result data) returned by incoming
//     *     activity result.
//     */
//    @Override
//    protected void onActivityResult(
//            int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch(requestCode) {
//            case REQUEST_GOOGLE_PLAY_SERVICES:
//                if (resultCode != RESULT_OK) {
//                    mOutputText.setText(
//                            "This app requires Google Play Services. Please install " +
//                                    "Google Play Services on your device and relaunch this app.");
//                } else {
//                    getResultsFromApi();
//                }
//                break;
//            case REQUEST_ACCOUNT_PICKER:
//                if (resultCode == RESULT_OK && data != null &&
//                        data.getExtras() != null) {
//                    String accountName =
//                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
//                    if (accountName != null) {
//                        SharedPreferences settings =
//                                getPreferences(Context.MODE_PRIVATE);
//                        SharedPreferences.Editor editor = settings.edit();
//                        editor.putString(PREF_ACCOUNT_NAME, accountName);
//                        editor.apply();
//                        mCredential.setSelectedAccountName(accountName);
//                        getResultsFromApi();
//                    }
//                }
//                break;
//            case REQUEST_AUTHORIZATION:
//                if (resultCode == RESULT_OK) {
//                    getResultsFromApi();
//                }
//                break;
//        }
//    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        EasyPermissions.onRequestPermissionsResult(
//                requestCode, permissions, grantResults, this);
//    }

//    /**
//     * Callback for when a permission is granted using the EasyPermissions
//     * library.
//     * @param requestCode The request code associated with the requested
//     *         permission
//     * @param list The requested permission list. Never null.
//     */
//    @Override
//    public void onPermissionsGranted(int requestCode, List<String> list) {
//        // Do nothing.
//    }
//
//    /**
//     * Callback for when a permission is denied using the EasyPermissions
//     * library.
//     * @param requestCode The request code associated with the requested
//     *         permission
//     * @param list The requested permission list. Never null.
//     */
//    @Override
//    public void onPermissionsDenied(int requestCode, List<String> list) {
//        // Do nothing.
//    }

//    /**
//     * Checks whether the device currently has a network connection.
//     * @return true if the device has a network connection, false otherwise.
//     */
//    private boolean isDeviceOnline() {
//        ConnectivityManager connMgr =
//                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//        return (networkInfo != null && networkInfo.isConnected());
//    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(mainContext);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

//    /**
//     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
//     * Play Services installation via a user dialog, if possible.
//     */
//    private void acquireGooglePlayServices() {
//        GoogleApiAvailability apiAvailability =
//                GoogleApiAvailability.getInstance();
//        final int connectionStatusCode =
//                apiAvailability.isGooglePlayServicesAvailable(mainContext);
//        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
//            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
//        }
//    }


//    /**
//     * Display an error dialog showing that Google Play Services is missing
//     * or out of date.
//     * @param connectionStatusCode code describing the presence (or lack of)
//     *     Google Play Services on this device.
//     */
//    void showGooglePlayServicesAvailabilityErrorDialog(
//            final int connectionStatusCode) {
//        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
//        Dialog dialog = apiAvailability.getErrorDialog(
//                GoogleCalendarCall.this,
//                connectionStatusCode,
//                REQUEST_GOOGLE_PLAY_SERVICES);
//        dialog.show();
//    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {

        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("JARVIA Google Calendar Call")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // List the next 10 events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            Date nowToday = new Date(System.currentTimeMillis());
            Date now2 = new Date();
            Log.d("TIME", now2.toString()); // Fri Apr 14 11:45:53 GMT-04:00 2017
            String show = DateFormat.getTimeInstance().format(nowToday); // 오후 4:22:40
            String show2 = DateFormat.getDateInstance().format(nowToday); // 2017. 4. 7.
            String show3 = DateFormat.getDateTimeInstance().format(nowToday); // 2017. 4. 7. 오후 4:22:40
          //  String show4 = DateFormat.getDateInstance().format(now); 이건 안됌 에러
            Log.d("@@@@@LOOK HERE TIME@@", show);
            Log.d("@@@@@LOOK HERE DATE@@", show2);
            Log.d("@@@@@LOOK HERE DATETIME", show3);
            List<String> eventStrings = new ArrayList<String>();
            Events events = mService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();


            List<Event> items = events.getItems();
           // List<Map<String, String>> pairList = new ArrayList<Map<String, String>>();
            String nowDay = now.toString().substring(0, now.toString().indexOf("T"));


            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    // All-day events don't have start times, so just use
                    // the start date.
                    start = event.getStart().getDate();
                }

                eventStrings.add(
                            String.format("%s (%s)", event.getSummary(), start));
            }
            List<String> realStrings = new ArrayList<String>();
            for (String a:eventStrings
                 ) {
          //      Log.d("@@@@", a);
                String day;
                String korTimeSpeech = null;
                String newSpeech = null;
                    day = a.substring(a.indexOf("("), a.indexOf(")"));
                    day = day.substring(1);

                if(day.length() > 16) {
                    int hour = Integer.parseInt(day.substring(11, 13));
                    if(hour < 12) {
                        korTimeSpeech = "오전 ";
                    } else{
                        korTimeSpeech = "오후 ";
                        hour = hour - 12;
                    }
                    korTimeSpeech = korTimeSpeech + hour + "시 " +  day.substring(14, 16) + "분에 ";
                    newSpeech = a.substring(0, a.indexOf("("));
                    newSpeech = korTimeSpeech + newSpeech;
                    //Make it in day format
                    day = day.substring(0, day.indexOf("T"));
                }else {
                   // korTimeSpeech = day.substring(11, 13) + "시 " +  day.substring(14, 16) + "분에 ";
                    newSpeech = a.substring(0, a.indexOf("("));
                    newSpeech = newSpeech;
                }

                if (day.equals(nowDay)) {
                    realStrings.add(newSpeech);
                }
            }

            return realStrings;
        }


        @Override
        protected void onPreExecute() {
//            mOutputText.setText("");
//            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            eventAndTime =  new ArrayList<String>();
            eventAndTime = output;


//            if (output == null || output.size() == 0) {
//                mOutputText.setText("No results returned.");
//            } else {
//                output.add(0, "Data retrieved using the Google Calendar API:");
//                mOutputText.setText(TextUtils.join("\n", output));
//            }
        }



//        @Override
//        protected void onCancelled() {
//            mProgress.hide();
//            if (mLastError != null) {
//                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
//                    showGooglePlayServicesAvailabilityErrorDialog(
//                            ((GooglePlayServicesAvailabilityIOException) mLastError)
//                                    .getConnectionStatusCode());
//                } else if (mLastError instanceof UserRecoverableAuthIOException) {
//                    startActivityForResult(
//                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
//                            GoogleCalendarCall.REQUEST_AUTHORIZATION);
//                } else {
////                    mOutputText.setText("The following error occurred:\n"
////                            + mLastError.getMessage());
//                }
//            } else {
////                mOutputText.setText("Request cancelled.");
//            }
//        }
    } // end of private class Maker

    public void execGetTwoHourTask(){
        getTwoHourRequestTask req = new getTwoHourRequestTask(mCredential);
        AsyncTaskCompat.executeParallel(req);
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class getTwoHourRequestTask extends AsyncTask<Void, Void, List<String>> {

        private Exception mLastError = null;

        getTwoHourRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("JARVIA Google Calendar Call")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // List the next 10 events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            Date nowToday = new Date(System.currentTimeMillis());
            Date now2 = new Date();
            Log.d("TIME", now2.toString()); // Fri Apr 14 11:45:53 GMT-04:00 2017
            String show = DateFormat.getTimeInstance().format(nowToday); // 오후 4:22:40
            String show2 = DateFormat.getDateInstance().format(nowToday); // 2017. 4. 7.
            String show3 = DateFormat.getDateTimeInstance().format(nowToday); // 2017. 4. 7. 오후 4:22:40
            //  String show4 = DateFormat.getDateInstance().format(now); 이건 안됌 에러


            java.text.SimpleDateFormat toDateTime    = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            //Date todayDate = new Date();
          //  String todayName = dayName.format(todayDate);

            java.util.Calendar calendar = java.util.Calendar.getInstance();

            calendar.setTime(nowToday);
            calendar.add(Calendar.HOUR, 2);
            Date twoFromNow = calendar.getTime();



            List<String> eventStrings = new ArrayList<String>();
            Events events = mService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setTimeMax(new DateTime(twoFromNow, TimeZone.getDefault()))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();


            List<Event> items = events.getItems();
            // List<Map<String, String>> pairList = new ArrayList<Map<String, String>>();
       //     String nowDay = now.toString().substring(0, now.toString().indexOf("T"));


            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();

                // Only get events with exact start time set, exclude all day
                if (start != null) {
                    eventStrings.add(
                            String.format("%s  ////  %s)", event.getSummary(), start));
                }


            }

            return eventStrings;
        }


        @Override
        protected void onPreExecute() {
//            mOutputText.setText("");
//            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            twoHourActList =  new ArrayList<String>();
            twoHourActList = output;


        }



    } // end of private class getTwoHourRequestTask


    public void createEventAsync(final String summary, final String location, final String des, final DateTime startDate, final DateTime endDate, final boolean allday) {

        new AsyncTask<Void, Void, String>() {
            private com.google.api.services.calendar.Calendar mService = null;
            private Exception mLastError = null;
            private boolean FLAG = false;


            @Override
            protected String doInBackground (Void...voids){
                try {
                    insertEvent(summary, location, des, startDate, endDate, allday);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute (String s){
                super.onPostExecute(s);
                getResultsFromApi();
            }
        }.execute();
    }
    void insertEvent(String summary, String location, String des, DateTime startDate, DateTime endDate, boolean allday) throws IOException {
        Event event = new Event()
                .setSummary(summary);
       //         .setLocation(location)
      //          .setDescription(des);

        if(!allday) {
            //Need all of the requirements. Uses current phone's timezone. TimeZone.getDefault().getID() gives out "America/New York"
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDate)
                    .setTimeZone(TimeZone.getDefault().getID());
            Log.d("TIMEZONE", TimeZone.getDefault().getID());
            event.setStart(start);

            EventDateTime end = new EventDateTime()
                    .setDateTime(endDate)
                    .setTimeZone(TimeZone.getDefault().getID());
            event.setEnd(end);
        } else{
            EventDateTime start = new EventDateTime()
                    .setDate(startDate)
                    .setTimeZone(TimeZone.getDefault().getID());
            Log.d("TIMEZONE", TimeZone.getDefault().getID());
            event.setStart(start);

//            java.util.Calendar calendar = java.util.Calendar.getInstance();
//            calendar.setTime(endDate.);

            EventDateTime end = new EventDateTime()
                    .setDate(endDate)
                    .setTimeZone(TimeZone.getDefault().getID());
            event.setEnd(end);
        }

        //Not required
       // String[] recurrence = new String[] {"RRULE:FREQ=DAILY;COUNT=1"};
      //  event.setRecurrence(Arrays.asList(recurrence));

        //Not required
//        EventAttendee[] attendees = new EventAttendee[]{
//                new EventAttendee().setEmail("abir@aksdj.com"),
//                new EventAttendee().setEmail("asdasd@andlk.com"),
//        };
 //       event.setAttendees(Arrays.asList(attendees));

        EventReminder[] reminderOverrides = new EventReminder[] {
                new EventReminder().setMethod("popup").setMinutes(60)
        };
        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(Arrays.asList(reminderOverrides));
        event.setReminders(reminders);

        String calendarId = "primary";

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("JARVIA Google Calendar Call")
                .build();
        //event.send
        try {
            if (mService != null)
                mService.events().insert(calendarId, event).setSendNotifications(true).execute();
        }catch (Exception e){
            e.printStackTrace();
        }


    }



}