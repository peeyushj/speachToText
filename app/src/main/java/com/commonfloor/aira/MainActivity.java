package com.commonfloor.aira;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Build;
import android.webkit.WebSettings;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

public class MainActivity extends ActionBarActivity implements TextToSpeech.OnInitListener {
    public static int loaded_random_int = 0;
    public static String new_url = "";
    private WebView myWebView;
    private ImageButton microphone;
    private EditText voiceToText;
    private TextToSpeech tts;
    private String ipAddress = "192.168.88.64";
    public static String query = "";

    private static final int REQUEST_CODE = 1234;
    Dialog match_text_dialog;
    ListView textlist;
    ArrayList<String> matches_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myWebView = (WebView) findViewById(R.id.webview);
        myWebView.setWebViewClient(new WebViewClient());
        myWebView.post(new Runnable() {
            public void run() {
                String startURL = "http://" + ipAddress + ":8888/start";
                myWebView.loadUrl(startURL);
            }
        });

        microphone = (ImageButton) findViewById(R.id.microphone);

        voiceToText = (EditText) findViewById(R.id.voiceToText);
        tts = new TextToSpeech(this, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public String getURL(){
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(2);
        System.out.println("Random Int is " + randomInt);
        if(randomInt != loaded_random_int) {
            loaded_random_int = randomInt;
            if (randomInt == 0) {
                return "http://www.commonfloor.com";
            } else {
                return "http://www.google.com";
            }
        }
        else
            return null;
    }

    public void loadSpeechTestIntent(){
        new Thread(new Runnable() {
            //Thread to stop network calls on the UI thread
            public void run() {
                //Request the HTML
                try {
                    MediaPlayer mPlayer = new MediaPlayer();
                    mPlayer.setDataSource("http://www.vocalware.com/tts/gen.php?EID=3&LID=1&VID=3&TXT=Hello%2C+I+am+Aai+ra.+Please+let+me+know+your+requirement&EXT=mp3&FX_TYPE=&FX_LEVEL=&ACC=5463643&API=2458605&SESSION=&HTTP_ERR=&CS=3d6cc3b337f4f96a08ed260c90782c3b");
                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mPlayer.prepare();
                    mPlayer.start();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }).start();
    }

    public void loadHelloIntent(){
        new Thread(new Runnable() {
            //Thread to stop network calls on the UI thread
            public void run() {
                //Request the HTML
                try {
                    HttpClient Client = new DefaultHttpClient();
                    //String URL = "http://10.0.2.2:8888/url";
                    String URL = "http://" + ipAddress + ":8888/hello";
                    String responseJsonString = "";

                    // Create Request to server and get response
                    HttpGet httpget = new HttpGet(URL);
                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    responseJsonString = Client.execute(httpget, responseHandler);

                    JSONObject responseJSON = new JSONObject(responseJsonString);
                    // Show response on activity
                    speakOut(responseJSON.getString("speechText"));
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }).start();
    }

    public void loadIntent(){
        new Thread(new Runnable() {
            //Thread to stop network calls on the UI thread
            public void run() {
                //Request the HTML
                try {
                    HttpClient Client = new DefaultHttpClient();
                    //String URL = "http://10.0.2.2:8888/url";
                    String URL = "http://" + ipAddress + ":8888/intent?sentence=" + URLEncoder.encode(query, "utf-8").replace("+", "%20");
                    String responseJsonString = "";

                    // Create Request to server and get response
                    HttpGet httpget = new HttpGet(URL);
                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    responseJsonString = Client.execute(httpget, responseHandler);

                    JSONObject responseJSON = new JSONObject(responseJsonString);
                    // Show response on activity
                    speakOut(responseJSON.getString("speechText"));

                    // Show response on activity
                    String response_url = responseJSON.getString("url");
                    if(!response_url.equals(new_url))
                    {
                        new_url = response_url;
                        myWebView.post(new Runnable() {
                            public void run() {
                                myWebView.loadUrl(new_url);
                            }
                        });
                        //myWebView.loadUrl(new_url);
                    }
                    //System.out.println(responseString);

                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }).start();
    }

    public  boolean isConnected()
    {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo net = cm.getActiveNetworkInfo();
            if (net != null && net.isAvailable() && net.isConnected()) {
                return true;
            } else {
                return false;
            }
        }
        catch (Exception ex)
        {
            System.out.println("CheckConnectivity Exception: " + ex.getMessage());
            return false;
        }
    }

    public void microphoneClicked(View view) {
        if(isConnected()){
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            startActivityForResult(intent, REQUEST_CODE);
        }
        else{
            Toast.makeText(getApplicationContext(), "Please Connect to Internet", Toast.LENGTH_LONG).show();
        }
        //if (Build.VERSION.SDK_INT >= 19) {
            //myWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        //}
//        myWebView.getSettings().setJavaScriptEnabled(true);
        //microphone.setImageResource(R.drawable.microphonedisable);
        //myWebView.loadUrl(new_url);
//        Timer waitingTimer = new Timer();
//        waitingTimer.schedule(new TimerTask()
//        {
//            @Override
//            public void run()
//            {
//                runOnUiThread(new Runnable()
//                {
//                    public void run()
//                    {
//                        String url = getURL();
//                        if(url != null)
//                            myWebView.loadUrl(url);
//                    }
//                });
//            }
//        },0,10000); // mention time interval after which your xml will be hit.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

//            match_text_dialog = new Dialog(MainActivity.this);
//            match_text_dialog.setContentView(R.layout.dialog_matches_frag);
//            match_text_dialog.setTitle("Select Matching Text");
//            textlist = (ListView)match_text_dialog.findViewById(R.id.list);
//            matches_text = data
//                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//            ArrayAdapter<String> adapter =    new ArrayAdapter<String>(this,
//                    android.R.layout.simple_list_item_1, matches_text);
//            textlist.setAdapter(adapter);
//            textlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view,
//                                        int position, long id) {
//                    voiceToText.setText("You have said " +matches_text.get(position));
//                    match_text_dialog.hide();
//                }
//            });
//            match_text_dialog.show();
            matches_text = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            query = matches_text.get(0);

            voiceToText.setText(query);
            //Begin - Send this text to url and return a speech text

            //End -  - Send this text to url and return a speech text
            if(query.toLowerCase().equals("ask commonfloor"))
                loadHelloIntent();
            else if(query.toLowerCase().equals("hello"))
                loadSpeechTestIntent();
            else
                loadIntent();

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onInit(int status) {
        // TODO Auto-generated method stub

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            //tts.setPitch(0.8f); // set pitch level
            tts.setSpeechRate(1); // set speech speed rate

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language is not supported");
            } else {
                Log.e("TTS", "Language is supported");
            }

        } else {
            Log.e("TTS", "Initilization Failed");
        }

    }

    public String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void speakOut(String text) {
        /*String EID = "3";
        String LID = "1";
        String VID = "3";
        String ACC = "5463643";
        String API = "2458605";
        String SECRET= "94d7097cf58394e75c8bff55c94299e0";
        String EXT = "mp3";
        String FX_TYPE="";
        String FX_LEVEL="";
        String HTTP_ERR = "";
        String SESSION = "";
        //text = "Welcome";

        String CS = md5(EID + LID + VID + text + EXT + FX_TYPE + FX_LEVEL + ACC + API + SESSION + HTTP_ERR + SECRET);

        System.out.println(CS);

        String voiceURL = "http://www.vocalware.com/tts/gen.php?EID=" + EID + "&LID=" + LID + "&VID="+ VID +"&TXT=" + text + "&EXT="+ EXT +"&FX_TYPE=" + FX_TYPE + "&FX_LEVEL=" + FX_LEVEL + "&ACC=" + ACC + "&API=" + API + "&SESSION=" + SESSION + "&HTTP_ERR=" + HTTP_ERR + "&CS=" + CS;

        System.out.println(voiceURL);

        try {
            MediaPlayer mPlayer = new MediaPlayer();
            //        .execute("http://www.virginmegastore.me/Library/Music/CD_001214/Tracks/Track1.mp3");

            mPlayer.setDataSource("http://www.vocalware.com/tts/gen.php?EID=3&LID=1&VID=3&TXT=Hello I am Aai ra. Please tell me you requirement&EXT=mp3&FX_TYPE=&FX_LEVEL=&ACC=5463643&API=2458605&SESSION=&HTTP_ERR=&CS=b460761f2ef7d431c07530ead86dda14");
//            Uri myUri = Uri.parse(voiceURL);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mPlayer.setDataSource(getApplicationContext(), myUri);
            mPlayer.prepare();
            mPlayer.start();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }*/






        //For Text To Speech
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsGreater21(text);
        }
        else {
            ttsUnder20(text);
        }
    }

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        try{
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        try {
            String utteranceId = this.hashCode() + "";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }
}

