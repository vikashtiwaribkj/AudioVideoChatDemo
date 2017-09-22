package com.hiappz.audiovideochatdemo;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;

import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener{

    private static String API_KEY = "45964682";
//    secret b54bdcbf5c26a203c170f718ad995b78e9226fd0
    private static String SESSION_ID = "2_MX40NTk2NDY4Mn5-MTUwNjA2NDIxODE4OX4zbnV6QkFKa0lhUGNmbHh4blljcVpET1h-UH4";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00NTk2NDY4MiZzaWc9Zjk3NzgzM2QwNGZmMjJjMDIxMWJmMTRiMWRmZjNmMTA1ODgzZWQ5OTpzZXNzaW9uX2lkPTJfTVg0ME5UazJORFk0TW41LU1UVXdOakEyTkRJeE9ERTRPWDR6Ym5WNlFrRkthMGxoVUdObWJIaDRibGxqY1ZwRVQxaC1VSDQmY3JlYXRlX3RpbWU9MTUwNjA2NDI3OSZub25jZT0wLjIxMDI1MDg5NjM2NTM1OCZyb2xlPXN1YnNjcmliZXImZXhwaXJlX3RpbWU9MTUwNjY2OTA3NyZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;
    private Session mSession;
    private Publisher mPublisher;
    private FrameLayout mPublisherViewContainer, mSubscriberViewContainer;
    private Subscriber mSubscriber;
    private boolean isSubscribed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
        if (EasyPermissions.hasPermissions(this, perms)) {
            // initialize view objects from your layout
            mPublisherViewContainer = findViewById(R.id.subscriber_container);
            mSubscriberViewContainer = findViewById(R.id.publisher_container);

            // initialize and connect to the session
            mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();
            mSession.setSessionListener(this);
            mSession.connect(TOKEN);


        } else {
            EasyPermissions.requestPermissions(this, "This app needs access to your camera and mic to make video calls", RC_VIDEO_APP_PERM, perms);
        }
    }

    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, " -->> Session Connected");

        boolean canPublish = session.getCapabilities().canPublish;
        boolean canSubscribe = session.getCapabilities().canSubscribe;

        Log.i(LOG_TAG, "Session Connected: -->> canPublish "+canPublish + " -->> canSubscribe -->> "+canSubscribe);

        mPublisher = new Publisher.Builder(getApplicationContext()).build();
        mPublisher.setPublisherListener(this);

        mPublisherViewContainer.addView(mPublisher.getView());
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, " -->> Session Disconnected");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG, " -->> Stream Received");
        if (mSubscriber == null){
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewContainer.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, " -->> Stream Dropped");
        if (mSubscriber != null){
            mSubscriber = null;
            mSubscriberViewContainer.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.e(LOG_TAG, "onError: -->> opentok error code -->> "+opentokError.getErrorCode().getErrorCode());
        Log.e(LOG_TAG, " -->> Session error: " + opentokError.getMessage());
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, " -->> Publisher onStreamCreated");
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, " -->> Publisher onStreamDestroyed");
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.e(LOG_TAG, "Publisher error: " + opentokError.getMessage());
        Log.e(LOG_TAG, "Publisher error: -->> opentok error code -->> "+opentokError.getErrorCode().getErrorCode());
    }
}
