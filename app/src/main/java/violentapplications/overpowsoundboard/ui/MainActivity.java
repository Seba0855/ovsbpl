package violentapplications.overpowsoundboard.ui;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.facebook.messenger.MessengerThreadParams;
import com.facebook.messenger.MessengerUtils;
import com.facebook.messenger.ShareToMessengerParams;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import violentapplications.overpowsoundboard.R;
import violentapplications.overpowsoundboard.Sound;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SHARE_TO_MESSENGER = 1;
    private MessengerThreadParams mThreadParams;
    private boolean mPicking;
    private MediaPlayer mp;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static final String TAG = "MainActivity";
    private InterstitialAd interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if (Intent.ACTION_PICK.equals(intent.getAction())) {
            mThreadParams = MessengerUtils.getMessengerThreadParamsForIntent(intent);
            mPicking = true;
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        Adapter myAdapter = new Adapter(this);
        myAdapter.setSoundList(getSoundList());
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(myAdapter);

        myAdapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(Sound item) {
                try {
                    onPause();
                    mp = MediaPlayer.create(getApplicationContext(), item.getUri());
                    mp.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onSendMessage(Sound item) {
                ShareToMessengerParams shareToMessengerParams =
                        ShareToMessengerParams.newBuilder(item.getUri(), "audio/mpeg")
                                .setMetaData("{ \"audio\" : \"warda postawcie\" }")
                                .build();

                if (mPicking) {
                    MessengerUtils.finishShareToMessenger(MainActivity.this, shareToMessengerParams);
                } else {
                    MessengerUtils.shareToMessenger(
                            MainActivity.this,
                            REQUEST_CODE_SHARE_TO_MESSENGER,
                            shareToMessengerParams);
                }
            }
        });

        /**TODO: SMS NOTIFICATION FUNCTION
         * myAdapter.setOnItemLongClickListener(new Adapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(Sound item) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Ustaw jako");
                builder.setCancelable(true);
                builder.setNeutralButton("Powiadomienia", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Ustawiono.", Toast.LENGTH_LONG).show();

                    }
                });
                builder.setPositiveButton("SMS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Ustawiono.", Toast.LENGTH_LONG).show();

                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });*/

        AdRequest adRequest = new AdRequest.Builder().build();
        interstitial = new InterstitialAd(MainActivity.this);
        interstitial.setAdUnitId("ca-app-pub-3913376764508228/3443035396");
        interstitial.loadAd(adRequest);
        interstitial.setAdListener(new AdListener() {
            public void onAdLoaded() {
                displayInterstitial();
            }
        });
        //end of onCreate
    }

    public void displayInterstitial() {
        if (interstitial.isLoaded()) {
            interstitial.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

    private List<Sound> getSoundList() {
        Field[] soundRaws = R.raw.class.getFields();
        String[] stringArray = getResources().getStringArray(R.array.strings);
        List<Sound> soundList = new ArrayList<>();
        for (int i = 0; i < stringArray.length; i++) {
            soundList.add(new Sound(stringArray[i],
                    Uri.parse("android.resource://" + getPackageName() + "/" + getResources().getIdentifier(soundRaws[i].getName(), "raw", getPackageName()))));
        }
        return soundList;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
