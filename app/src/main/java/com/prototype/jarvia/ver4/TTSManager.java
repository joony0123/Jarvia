package com.prototype.jarvia.ver4;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Joon.Y.K on 2017-03-19.
 */

public class TTSManager{

    private HashMap<String, String> map;
    private TextToSpeech mTts = null;
    public boolean isLoaded = false;
    private Context mainContext;

    public void init(Context context) {
        try {
            mainContext = context;
            mTts = new TextToSpeech(context, onInitListener, "com.google.android.tts");
            map = new HashMap<String, String>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private TextToSpeech.OnInitListener onInitListener = new TextToSpeech.OnInitListener() {
        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                Log.d("TTS", "Init Success");
                int result = mTts.setLanguage(Locale.KOREAN);
                mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        Log.d("TTS", "@@@@@@@@Speaking Started");
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        Log.d("TTS", "@@@@@@@@Speaking Complete");
                        if(mainContext instanceof MainActivity){

                            // Get a handler that can be used to post to the main thread
                            new Handler(mainContext.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(!(((MainActivity)mainContext).getIsTextMode()) &&
                                            !(((MainActivity)mainContext).getCallingState()) &&
                                            !(((MainActivity)mainContext).getCollectingState()) &&
                                    !(((MainActivity)mainContext).getStartModeChange())) {
                                        Log.d("TTS", "@@@@@@@@@@@@@@@@Start LISTENING Executed from TTS");
                                        ((MainActivity) mainContext).setIsListening(true);
                                        ((MainActivity) mainContext).getAiService().startListening();
                                    }
                                    ((MainActivity)mainContext).setStartModeChange(false);
                                }
                            }, 750);
                        }

                    }

                    @Override
                    public void onError(String utteranceId) {

                    }
                });
                isLoaded = true;

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("error", "This Language is not supported");
                }
            } else {
                Log.e("error", "Initialization Failed!");
            }
        }
    };

    public void shutDown() {
        mTts.shutdown();
    }



    public void addQueue(String text) {
        if (isLoaded)
            mTts.speak(text, TextToSpeech.QUEUE_ADD, map);
        else
            Log.e("error", "TTS Not Initialized");
    }

    public void initQueue(String text) {

        if (isLoaded)
            mTts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
        else
            Log.e("error", "TTS Not Initialized");
    }

    public boolean isSpeaking(){
        return mTts.isSpeaking();
    }



}