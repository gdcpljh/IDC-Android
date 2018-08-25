package com.id.connect.diaspora.ui.activity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ale.infra.application.RainbowContext;
import com.ale.infra.manager.call.ITelephonyListener;
import com.ale.infra.manager.call.PeerSession;
import com.ale.infra.manager.call.WebRTCCall;
import com.ale.rainbow.phone.session.MediaState;
import com.ale.rainbowsdk.RainbowSdk;
import com.id.connect.diaspora.R;
import com.id.connect.diaspora.utils.Util;

import org.webrtc.MediaStream;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

import java.util.Map;

public class VoiceCallActivity extends AppCompatActivity  implements ITelephonyListener, SensorEventListener {
    private boolean allowGetVideo = false;
    private LinearLayout m_outgoingCallLayout;
    private LinearLayout m_incomingCallLayout;
    private RelativeLayout m_ongoingCallLayout;

    private FrameLayout m_cardViewPhoto;
    private ImageView m_answerVideoCallButton;

    private SurfaceViewRenderer m_bigVideoView;
    private SurfaceViewRenderer m_littleVideoView;
    private VideoRenderer m_remoteVideoRenderer;
    private VideoRenderer m_localVideoRenderer;
    private boolean m_localVideoOnLittleView = true;

    private VideoTrack m_localVideoTrack;
    private MediaPlayer m_mediaPlayer;

    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private int field = 0x00000020;

    private SensorManager mSensorManager;
    private Sensor mProximity;
    private static final int SENSOR_SENSITIVITY = 4;

//    @Override
//    protected void attachBaseContext(Context base) {
//        super.attachBaseContext(LanguageHelper.onAttach(base));
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_voice_call);

        WebRTCCall currentCall = RainbowSdk.instance().webRTC().getCurrentCall();

        if (currentCall == null) {
            finish();
            return;
        } else if (currentCall.getDistant() == null) {
            finish();
            return;
        }

        m_cardViewPhoto = (FrameLayout) findViewById(R.id.card_view_photo);

        ImageView imageViewPhoto = (ImageView) findViewById(R.id.photo_image_view);
        if (currentCall.getDistant().getPhoto() == null) {
            imageViewPhoto.setImageResource(R.drawable.contact);
        } else {
            imageViewPhoto.setImageBitmap(currentCall.getDistant().getPhoto());
        }

        // ===== m_incomingCallLayout
        m_incomingCallLayout = (LinearLayout) findViewById(R.id.incoming_call_layout);

        ImageView answerAudioCallButton = (ImageView) findViewById(R.id.answer_audio_button);
        answerAudioCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RainbowSdk.instance().webRTC().takeCall(false);
            }
        });

        m_answerVideoCallButton = (ImageView) findViewById(R.id.answer_video_button);
        m_answerVideoCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RainbowSdk.instance().webRTC().takeCall(true);
            }
        });

        ImageView rejectCallButton = (ImageView) findViewById(R.id.reject_call_button);
        rejectCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RainbowSdk.instance().webRTC().rejectCall();
            }
        });

        // ===== m_outgoingCallLayout
        m_outgoingCallLayout = (LinearLayout) findViewById(R.id.outgoing_call_layout);

        ImageView imageViewhangupCall = (ImageView) findViewById(R.id.hangup_call_button);
        imageViewhangupCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RainbowSdk.instance().webRTC().hangupCall();
            }
        });

        // ===== m_ongoingCallLayout
        m_ongoingCallLayout = (RelativeLayout) findViewById(R.id.ongoing_call_layout);

        // Switch camera
        ImageView imageViewSwitchCamera = (ImageView) findViewById(R.id.switch_camera_button);
        imageViewSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RainbowSdk.instance().webRTC().switchCamera();
            }
        });

        // Mute / unmute
        final ImageView imageViewStateMicOff = (ImageView) findViewById(R.id.state_mic_off);
        final ImageView imageViewMute = (ImageView) findViewById(R.id.mute_image_button);
        imageViewMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RainbowSdk.instance().webRTC().mute(!RainbowSdk.instance().webRTC().isMuted(), false);
                imageViewMute.setImageResource(RainbowSdk.instance().webRTC().isMuted() ? R.drawable.btn_mic_off : R.drawable.btn_mic_on);
                //imageViewStateMicOff.setVisibility(RainbowSdk.instance().webRTC().isMuted() ? View.VISIBLE : View.GONE);
            }
        });

        // Add / remove video
        final ImageView imageViewAddVideo = (ImageView) findViewById(R.id.add_video_button);
        imageViewAddVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RainbowSdk.instance().webRTC().getLocalVideoTrack() != null) {
                    if (RainbowSdk.instance().webRTC().dropVideo()) {
                        imageViewAddVideo.setImageResource(R.drawable.btn_camera_off);
                        m_littleVideoView.setVisibility(View.GONE);
                    }
                } else {
                    if (RainbowSdk.instance().webRTC().addVideo()) {
                        imageViewAddVideo.setImageResource(R.drawable.btn_camera_on);
                        m_littleVideoView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        ImageView imageViewRecord = (ImageView) findViewById(R.id.record_button);
        imageViewRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // Hang up call
        ImageView imageViewHangupCall = (ImageView) findViewById(R.id.hang_up_image_view);
        imageViewHangupCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RainbowSdk.instance().webRTC().hangupCall();
            }
        });

        // Remote video view
        m_bigVideoView = (SurfaceViewRenderer) findViewById(R.id.big_video_view);
        m_bigVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        m_bigVideoView.setMirror(true);
        m_bigVideoView.requestLayout();

        // Local video view
        m_littleVideoView = (SurfaceViewRenderer) findViewById(R.id.little_video_view);
        m_littleVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        m_littleVideoView.setZOrderMediaOverlay(true);
        m_littleVideoView.setMirror(true);
        m_littleVideoView.requestLayout();

        // Init these two surface views
        try {
            m_bigVideoView.init(RainbowSdk.instance().webRTC().getCurrentCall().getEglBaseContext(), null);
            m_littleVideoView.init(RainbowSdk.instance().webRTC().getCurrentCall().getEglBaseContext(), null);
        } catch (RuntimeException e) {
            finish();
            return;
        }

        // Animation to show or hide buttons menu when video is used
        final Animation.AnimationListener animationListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                if (m_ongoingCallLayout.getVisibility() != View.VISIBLE) {
                    m_ongoingCallLayout.setVisibility(View.VISIBLE);
                } else {
                    m_ongoingCallLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };

        m_bigVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_ongoingCallLayout.getVisibility() != View.VISIBLE) {
                    AlphaAnimation fade_in = new AlphaAnimation(0.0f, 1.0f);
                    fade_in.setDuration(500);
                    fade_in.setAnimationListener(animationListener);
                    m_ongoingCallLayout.startAnimation(fade_in);
                } else {
                    AlphaAnimation fade_out = new AlphaAnimation(1.0f, 0.0f);
                    fade_out.setDuration(500);
                    fade_out.setAnimationListener(animationListener);
                    m_ongoingCallLayout.startAnimation(fade_out);
                }
            }
        });

        // Listen to events
        RainbowSdk.instance().webRTC().registerTelephonyListener(this);
        updateLayoutWithCall(RainbowSdk.instance().webRTC().getCurrentCall());

        handleRinging();

        try {
            field = PowerManager.class.getClass().getField("PROXIMITY_SCREEN_OFF_WAKE_LOCK").getInt(null);
        } catch (Throwable ignored) {}

        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(field, getLocalClassName());

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

    @Override
    protected void onDestroy() { super.onDestroy(); }

    @Override
    public void onCallAdded(WebRTCCall call) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Util.log("onCallAdded");
            }
        });
    }

    @Override
    public void onCallModified(WebRTCCall call) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Util.log("onCallModified");
            }
        });

        updateLayoutWithCall(call);
        stopRinging();
    }

    @Override
    public void onCallRemoved(final WebRTCCall call) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Util.log("onCallRemoved");
            }
        });

        stopRinging();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });
    }

    private void handleRinging() {
        if (m_mediaPlayer != null) {
            stopRinging();
        }

        try {
            String ringtoneString;
            ringtoneString = "android.resource://id.co.ale.rainbowDev/" + R.raw.call;


            if (RainbowSdk.instance().webRTC().getCurrentCall().getState() == MediaState.RINGING_OUTGOING) {
                ringtoneString = "android.resource://id.co.ale.rainbowDev/" + R.raw.outgoingrings;
            }

            Uri ringtoneUri = Uri.parse(ringtoneString);

            m_mediaPlayer = new MediaPlayer();
            m_mediaPlayer.setLooping(true);
            m_mediaPlayer.setDataSource(getApplicationContext(), ringtoneUri);

            m_mediaPlayer.prepare();
            m_mediaPlayer.start();

        } catch (Exception e) {}
    }

    private void stopRinging() {
        if (m_mediaPlayer != null) {
            if (m_mediaPlayer.isPlaying()) {
                m_mediaPlayer.stop();
            }
            m_mediaPlayer.release();
            m_mediaPlayer = null;
        }
    }

    private void updateLayoutWithCall(final WebRTCCall call) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (call.getState() == MediaState.RINGING_OUTGOING) {
                    showLayoutAndHideOthers(m_outgoingCallLayout);
                } else if (call.getState() == MediaState.RINGING_INCOMING) {
                    showLayoutAndHideOthers(m_incomingCallLayout);

                    m_answerVideoCallButton.setEnabled(call.wasInitiatedWithVideo());
                    if (call.wasInitiatedWithVideo()) {
                        m_answerVideoCallButton.setImageResource(R.drawable.bt_answercall_video_active);
                    } else {
                        m_answerVideoCallButton.setImageResource(R.drawable.bt_answercall_video_inactive);
                    }
                } else if (call.getState() == MediaState.ACTIVE) {
                    showLayoutAndHideOthers(m_ongoingCallLayout);

                    m_bigVideoView.setVisibility(call.wasInitiatedWithVideo() ? View.VISIBLE : View.GONE);
                    m_littleVideoView.setVisibility(call.wasInitiatedWithVideo() ? View.VISIBLE : View.GONE);

                    Map<PeerSession.PeerSessionType, MediaStream> streams = RainbowSdk.instance().webRTC().getAddedStreams();
                    if(streams.containsKey(PeerSession.PeerSessionType.AUDIO_VIDEO_SHARING)){
                        MediaStream stream = streams.get(PeerSession.PeerSessionType.AUDIO_VIDEO_SHARING);

                        if(stream.videoTracks.size() > 0){
                            streamAdded(stream);
                        }else{
                            streamRemoved(stream);
                        }
                    }

                    if(call.wasInitiatedWithVideo()){
                        onLocalVideoTrackCreated(RainbowSdk.instance().webRTC().getLocalVideoTrack());
                    }
                }
            }
        });
    }

    private void showLayoutAndHideOthers(final ViewGroup layout) {
        m_outgoingCallLayout.setVisibility(View.GONE);
        m_incomingCallLayout.setVisibility(View.GONE);
        m_ongoingCallLayout.setVisibility(View.GONE);

        if (layout != null) {
            layout.setVisibility(View.VISIBLE);
        }
    }

    public void streamAdded(final MediaStream stream) {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (m_localVideoOnLittleView) {
                    renderRemoteVideo(stream, m_bigVideoView);
                } else {
                    renderRemoteVideo(stream, m_littleVideoView);
                }

                if(stream.videoTracks.size() > 0){
                    if(!allowGetVideo && !RainbowSdk.instance().webRTC().getCurrentCall().wasInitiatedWithVideo()){

                        m_cardViewPhoto.setVisibility(View.INVISIBLE);
                        m_bigVideoView.setVisibility(View.VISIBLE);
                        m_littleVideoView.setVisibility(View.VISIBLE);

                        RainbowSdk.instance().webRTC().getCurrentCall().setInitiatedWithVideo(true);

                        allowGetVideo = true;
                    }
                }
            }
        });
    }

    public void streamRemoved(final MediaStream stream) {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (RainbowContext.getInfrastructure().getCapabilities().isVideoWebRtcAllowed() && stream.videoTracks.size() > 0)
                {
                    stream.videoTracks.get(0).removeRenderer(m_remoteVideoRenderer);
                    m_remoteVideoRenderer.dispose();
                }
            }
        });
    }

    private void renderRemoteVideo(MediaStream stream, SurfaceViewRenderer surfaceViewRenderer)
    {
        try{
            if (RainbowContext.getInfrastructure().getCapabilities().isVideoWebRtcAllowed() && stream.videoTracks.size() > 0)
            {
                if (m_remoteVideoRenderer != null){
                    stream.videoTracks.get(0).removeRenderer(m_remoteVideoRenderer);
                }

                m_remoteVideoRenderer = new VideoRenderer(surfaceViewRenderer);
                stream.videoTracks.get(0).addRenderer(m_remoteVideoRenderer);
            }
            surfaceViewRenderer.requestLayout();

            m_cardViewPhoto.setVisibility(View.INVISIBLE);
            m_bigVideoView.setVisibility(View.VISIBLE);
            m_littleVideoView.setVisibility(View.VISIBLE);
        }catch (Exception e){}
    }

    private void renderLocalVideo(VideoTrack videoTrack, SurfaceViewRenderer surfaceViewRenderer)
    {
        try{
            if (m_localVideoRenderer != null){
                videoTrack.removeRenderer(m_localVideoRenderer);
            }

            m_localVideoRenderer = new VideoRenderer(surfaceViewRenderer);
            videoTrack.addRenderer(m_localVideoRenderer);
        }catch (Exception e){}
    }

    //@Override
    public void onLocalVideoTrackCreated(final VideoTrack videoTrack) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_localVideoTrack = videoTrack;
                m_cardViewPhoto.setVisibility(View.INVISIBLE);

                if (m_localVideoOnLittleView) {
                    renderLocalVideo(videoTrack, m_littleVideoView);
                } else {
                    renderLocalVideo(videoTrack, m_bigVideoView);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] >= -SENSOR_SENSITIVITY && event.values[0] <= SENSOR_SENSITIVITY) {
                if(!wakeLock.isHeld()) {
                    wakeLock.acquire();
                }
            } else {
                if(wakeLock.isHeld()) {
                    wakeLock.release();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}