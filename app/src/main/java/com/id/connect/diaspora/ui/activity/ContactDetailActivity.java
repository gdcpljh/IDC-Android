package com.id.connect.diaspora.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ale.infra.contact.Contact;
import com.ale.infra.contact.RainbowPresence;
import com.ale.rainbowsdk.RainbowSdk;
import com.amulyakhare.textdrawable.TextDrawable;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.id.connect.diaspora.R;
import com.id.connect.diaspora.utils.Util;
import com.jsibbold.zoomage.ZoomageView;

import jp.wasabeef.blurry.Blurry;

public class ContactDetailActivity extends AppCompatActivity {
    private ImageView contactPhoto;
    private TextView contactName;
    private TextView contactStatus;
    private TextView contactJob;
    private TextView contactCorp;
    private ImageView bannerImage;

    private boolean fromChat = false;
    private Contact contact;

    private Dialog dialog;
    private ZoomageView dialogImage;

    private ImageView qrCodeDisplay;
    private boolean isSetQR = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        this.contactName = (TextView) findViewById(R.id.participant_name);
        this.contactStatus = (TextView) findViewById(R.id.contact_status);
        this.contactJob = (TextView) findViewById(R.id.contact_job);
        this.contactCorp = (TextView) findViewById(R.id.contact_corp);
        this.bannerImage = (ImageView) findViewById(R.id.banner_image);

        this.contactPhoto = (ImageView) findViewById(R.id.contact_photo);
        dialog = new Dialog(this, R.style.dialogImageFull);
        dialog.setContentView(R.layout.image_dialog);
        dialogImage = (ZoomageView) dialog.findViewById(R.id.fullscreen_image);
        dialog.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        this.contactPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bmp = contact.getPhoto();
                if(bmp != null){
                    dialogImage.setImageBitmap(bmp);
                    dialog.show();
                }
            }
        });

        //QR CODE
        this.qrCodeDisplay = (ImageView) findViewById(R.id.qrcode_view);
        //---------

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        this.contact = Util.tempContact;
        updateContact();
        this.contact.registerChangeListener(contactListener);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if(bundle != null)
            this.fromChat = bundle.getBoolean("FROM_CHAT", false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.contact_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.contact_chat:
                if(fromChat){
                    finish();
                }else{
                    Intent intent = new Intent(getBaseContext(), ChatActivity.class);
                    startActivity(intent);
                }
                return true;
            case R.id.call:
                RainbowSdk.instance().webRTC().makeCall((Contact) this.contact, false);
                Intent intentCall = new Intent(getBaseContext(), VoiceCallActivity.class);
                startActivity(intentCall);
                return true;
            case R.id.video_call:
                RainbowSdk.instance().webRTC().makeCall((Contact) this.contact, true);
                Intent intentVCall = new Intent(getBaseContext(), VoiceCallActivity.class);
                startActivity(intentVCall);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateContact(){
        this.contactName.setText(this.contact.getFirstName()+" "+this.contact.getLastName());
        Bitmap bmp = this.contact.getPhoto();
        if(bmp != null){
            this.contactPhoto.setImageBitmap(Util.getRoundedCornerBitmap(bmp, bmp.getWidth()));
            Blurry.with(getBaseContext()).from(bmp).into(bannerImage);
        }else{
            String initName = "?";
            try{
                initName = this.contact.getFirstName().substring(0,1)+this.contact.getLastName().substring(0,1);
            }catch (Exception e){}
            this.contactPhoto.setImageDrawable(TextDrawable.builder()
                    .beginConfig()
                    .textColor(Color.parseColor("#F26722"))
                    .endConfig()
                    .buildRound(initName, Color.WHITE));
        }
        this.contactStatus.setText(Util.presenceStatus(this.contact.getPresence().getPresence()));
        this.contactJob.setText(this.contact.getJobTitle());
        this.contactCorp.setText(this.contact.getCompanyName());

        if(contact.getImJabberId().equals(Util.BOT_JABBER_ID)){
            RelativeLayout mainContent = (RelativeLayout) findViewById(R.id.main_content);
            mainContent.setVisibility(View.GONE);
            RelativeLayout botContent = (RelativeLayout) findViewById(R.id.bot_content);
            botContent.setVisibility(View.VISIBLE);

            this.qrCodeDisplay.setVisibility(View.GONE);
            this.contactStatus.setVisibility(View.GONE);
        }else if(contact != null && ! isSetQR){
            QRCodeWriter writer = new QRCodeWriter();
            try {
                String qrContent =
                        this.contact.getCorporateId()
                                + ":"
                                + this.contact.getMainEmailAddress();

                BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200);
                int width = bitMatrix.getWidth();
                int height = bitMatrix.getHeight();
                Bitmap qrBmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

                int [] pallette = {
                        Color.parseColor("#3498db"),
                        Color.parseColor("#e74c3c"),
                        Color.parseColor("#8e44ad"),
                        Color.parseColor("#e67e22"),
                        Color.parseColor("#16a085"),
                };

                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int idx = (int) x / (200 / pallette.length);
                        if(idx >= pallette.length) idx = pallette.length - 1;
                        int c = pallette [idx];
                        qrBmp.setPixel(x, y, bitMatrix.get(x, y) ? c : Color.parseColor("#ffffff"));
                    }
                }
                this.qrCodeDisplay.setImageBitmap(qrBmp);
                isSetQR = true;

            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }

    private Contact.ContactListener contactListener = new Contact.ContactListener() {
        @Override
        public void contactUpdated(Contact contact) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateContact();
                }
            });
        }

        @Override
        public void onPresenceChanged(Contact contact, RainbowPresence rainbowPresence) {}

        @Override
        public void onActionInProgress(boolean b) {}
    };
}
