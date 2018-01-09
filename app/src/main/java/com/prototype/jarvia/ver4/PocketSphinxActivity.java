/* ====================================================================
 * Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 */

package com.prototype.jarvia.ver4;

import static android.widget.Toast.makeText;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;


public class PocketSphinxActivity extends Activity implements
        RecognitionListener {
		
    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";
    /*private static final String FORECAST_SEARCH = "forecast";
    private static final String DIGITS_SEARCH = "digits";
    private static final String PHONE_SEARCH = "phones";
    private static final String MENU_SEARCH = "menu"; */


//    private AIService aiService;
//    private AIResponse aiResponse;

    private TTSManager ttsManager = null;
    /* Keyword we are looking for to activate menu */
    private static final String KEYPHRASE = "jarvia";

    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;
    Context context;
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        


        // context=this.getApplicationContext();
        // Prepare the data for UI
      /*  captions = new HashMap<String, Integer>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        captions.put(MENU_SEARCH, R.string.menu_caption);
        captions.put(DIGITS_SEARCH, R.string.digits_caption);
        captions.put(PHONE_SEARCH, R.string.phone_caption);
        captions.put(FORECAST_SEARCH, R.string.forecast_caption);*/
        setContentView(R.layout.main);
        ((TextView) findViewById(R.id.caption_text))
                .setText("Preparing the recognizer");

        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(PocketSphinxActivity.this);
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
                    ((TextView) findViewById(R.id.caption_text))
                            .setText("Failed to init recognizer " + result);
                } else {
                    Toast.makeText(getApplicationContext(), "JARVIA is Activated, Sir", Toast.LENGTH_LONG).show();
                    recognizer.startListening(KWS_SEARCH);
                    //switchSearch(KWS_SEARCH);
                }
            }
        }.execute();

        ttsManager = new TTSManager();
        ttsManager.init(this);

        // Wiki Testing... SUCCESS!!! 4/17
        //WikiResponse wiki = new WikiResponse();


//        final AIConfiguration config = new AIConfiguration("f25aab40ac6747478e7e62364d63fb63 ",
//                AIConfiguration.SupportedLanguages.Korean,
//                AIConfiguration.RecognitionEngine.System);
//
//        aiService = AIService.getService(this, config);
//        aiService.setListener(this);

        moveTaskToBack(true);


    }




    @Override
    public void onDestroy() {
        super.onDestroy();
//        recognizer.cancel();
 //       recognizer.shutdown();
//        finish();
    }

//    @Override
//    public void onError(final AIError error) {
//        Log.d("ERR", "onError: ERRORRR");;
//    }


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
            Toast.makeText(getApplicationContext(), "JARVIA is Here", Toast.LENGTH_SHORT).show();
            recognizer.stop();
            //Start Activity(In Background), fixed 'SleepMode --> kill --> relaunch not working error'
            //Activity will not show since it is background but below code will make it appear
            Intent jarvis = new Intent(getApplicationContext(), MainActivity.class);
            jarvis.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(jarvis);

            recognizer.shutdown();

            // Relaunch the app to the foreground to the recent state (same as clicking app icon)
            // Without making another screen (Needed for Pulling up screen).
            //여기서는 이거 쓰면 채팅창 등장함
            PackageManager packageManager = getApplicationContext().getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
            if (intent != null)
            {
                intent.setPackage(null);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                getApplicationContext().startActivity(intent);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("POCKETSPHINX ACTIVITY", "SHUTDOWN >>>>>>>>>>>>>>>>>>");
                        finish();

                    }
                }, 2000);
            }

        }


    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {






        //For without UI.
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Log.d("POCKETSPHINX ACTIVITY", "SHUTDOWN >>>>>>>>>>>>>>>>>>");
//                finish();
//
//            }
//        }, 5000);



    }

//    public void onResult(final AIResponse response) {
//        Result result = response.getResult();
//
//        ttsManager.initQueue(result.getFulfillment().getSpeech());
//
//
//
//      //  Intent sendingQuery = new Intent(PocketSphinxActivity.this, MainActivity.class);
//       // sendingQuery.putExtra("query", result.getResolvedQuery());
//         //     startActivity(sendingQuery);
//
//    }

    @Override
    public void onBeginningOfSpeech() {
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            Toast.makeText(getApplicationContext(), "End of Speech", Toast.LENGTH_SHORT).show();
     //       switchSearch(KWS_SEARCH);
    }

    private void switchSearch(String searchName) {
        recognizer.stop();
        
        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 10000);

      //  String caption = getResources().getString(captions.get(searchName));
      //  ((TextView) findViewById(R.id.caption_text)).setText(caption);
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them
        
        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                
                // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                //.setRawLogDir(assetsDir)
                
                // Threshold to tune for keyphrase to balance between false alarms and misses
                .setKeywordThreshold(1e-5f)
                
                // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setBoolean("-allphone_ci", true)
                
                .getRecognizer();
        recognizer.addListener(this);

        /** In your application you might not need to add all those searches.
         * They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

    }

    @Override
    public void onError(Exception error) {
    //    ((TextView) findViewById(R.id.caption_text)).setText(error.getMessage());
    }

    @Override
    public void onTimeout() {
      //  switchSearch(KWS_SEARCH);
    }


}
