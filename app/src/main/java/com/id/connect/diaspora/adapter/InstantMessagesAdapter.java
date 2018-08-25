package com.id.connect.diaspora.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ale.infra.contact.IRainbowContact;
import com.ale.infra.manager.IMMessage;
import com.ale.infra.manager.fileserver.RainbowFileDescriptor;
import com.ale.infra.xmpp.xep.calllog.CallLogPacketExtension;
import com.ale.rainbowsdk.RainbowSdk;
import com.amulyakhare.textdrawable.TextDrawable;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.id.connect.diaspora.R;
import com.id.connect.diaspora.utils.Util;

import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Pattern;

import nl.changer.audiowife.AudioWife;

/**
 * Created by Alcatel-Dev on 04/09/2017.
 */

public class InstantMessagesAdapter extends ArrayAdapter<IMMessage> {
    Context context;
    public InstantMessagesAdapter(Context context, List<IMMessage> instantMessages) {
        super(context, 0, instantMessages);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        IMMessage message  = getItem(position);

        boolean isOwner;
        String msg_content = "";
        String msg_date = "";

        if(message.isWebRtcEventType()){
            isOwner = message.getCallLogEvent().getCallerJid().equals(RainbowSdk.instance().myProfile().getConnectedUser().getImJabberId());
        }else{
            isOwner = (!message.getDeliveryState().equals(IMMessage.DeliveryState.READ) && !message.getDeliveryState().equals(IMMessage.DeliveryState.RECEIVED));
        }

        convertView = isOwner
                ? LayoutInflater.from(getContext()).inflate(R.layout.list_immesage_owner, parent, false)
                : LayoutInflater.from(getContext()).inflate(R.layout.list_immesage, parent, false);

        final InstantMessagesViewHolder viewHolder = new InstantMessagesViewHolder(convertView, isOwner);
        IRainbowContact contact = RainbowSdk.instance().contacts().getContactFromJabberId(message.getContactJid());

        if(!isOwner){
            Bitmap bmp = contact.getPhoto();
            if(bmp != null){
                viewHolder.tSenderPhoto.setImageBitmap(Util.getRoundedCornerBitmap(contact.getPhoto(), bmp.getWidth()));
            }else{
                String initName = "?";
                try{
                    initName = contact.getFirstName().substring(0,1).toUpperCase() + contact.getLastName().substring(0,1).toUpperCase();
                }catch (Exception e){}
                viewHolder.tSenderPhoto.setImageDrawable(TextDrawable.builder().buildRoundRect(initName, Util.generateColor(contact.getFirstName()+" "+contact.getLastName()), 50));
            }
        }

        if(message.isEventType()){
            if( message.isWebRtcEventType()){
                CallLogPacketExtension callog = message.getCallLogEvent();

                if(callog.getState().equals("answered")){
                    msg_content = getContext().getString(R.string.answered_call);
                }else{
                    msg_content = getContext().getString(R.string.missed_call);
                }
            }else if(message.isRoomEventType()){

            }
        }
        else{
            msg_content = message.getMessageContent();

            if(isOwner){
                if(message.getDeliveryState().equals(IMMessage.DeliveryState.SENT_CLIENT_RECEIVED)){
                    viewHolder.icStatus.setImageResource(R.drawable.icon_received);
                }else if(message.getDeliveryState().equals(IMMessage.DeliveryState.SENT_CLIENT_READ)){
                    viewHolder.icStatus.setImageResource(R.drawable.icon_read);
                }else{
                    viewHolder.icStatus.setImageResource(R.drawable.icon_sent);
                }
            }
        }

        //FILE CHECKING
            if (message.isFileDescriptorAvailable()) {
                final RainbowFileDescriptor fileDescriptor = message.getFileDescriptor();

                if(msg_content.equals("") || msg_content.equals(null)){
                    viewHolder.tContentMessage.setVisibility(View.GONE);
                }

                String fileName = fileDescriptor.getFileName();
                if(fileName != null){
                    if(fileName.length() > 15) fileName = fileName.substring(0, 15);
                }
                viewHolder.fileInfo.setText(fileName+" ("+Util.txtFileSize(fileDescriptor.getSize())+")");

                if(RainbowSdk.instance().fileStorage().isDownloaded(fileDescriptor)){
                    if(fileDescriptor.isImageType()){
                        try{
                            Bitmap fileImage = fileDescriptor.getImage();
                            if(fileImage != null){
                                viewHolder.filePreview.setImageBitmap(fileImage);
                                viewHolder.downloadProgress.setVisibility(View.GONE);
                            }else{
                                viewHolder.downloadProgress.setProgress(View.VISIBLE);
                            }
                        }catch (Exception e){}
                    }else if(fileDescriptor.isAudioVideoFileType()) {
                        viewHolder.fileIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_audio_video));
                        viewHolder.fileIcon.setVisibility(View.VISIBLE);
                    }else if(fileDescriptor.isPdfFileType()){
                        viewHolder.fileIcon.setImageDrawable(getContext().getDrawable(R.drawable.ic_pdf));
                        viewHolder.fileIcon.setVisibility(View.VISIBLE);
                    }else if(fileDescriptor.isDocumentFileType()){
                        viewHolder.fileIcon.setImageDrawable(getContext().getDrawable(R.drawable.ic_doc));
                        viewHolder.fileIcon.setVisibility(View.VISIBLE);
                    }else if(fileDescriptor.isPptFileType()){
                        viewHolder.fileIcon.setImageDrawable(getContext().getDrawable(R.drawable.ic_ppt));
                        viewHolder.fileIcon.setVisibility(View.VISIBLE);
                    }else if(fileDescriptor.isExcelFileType()){
                        viewHolder.fileIcon.setImageDrawable(getContext().getDrawable(R.drawable.ic_excel));
                        viewHolder.fileIcon.setVisibility(View.VISIBLE);
                    }
                    else{
                        viewHolder.fileIcon.setImageDrawable(getContext().getDrawable(R.drawable.ic_file));
                        viewHolder.fileIcon.setVisibility(View.VISIBLE);
                    }

                    viewHolder.filePreview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            File downloadedFile = RainbowSdk.instance().fileStorage().getFileDownloaded(fileDescriptor);

                            if(Util.getMime(fileDescriptor.getExtension()).equals("audio")){
                                viewHolder.fileIcon.setVisibility(View.GONE);
                                viewHolder.mediaPlayerContainer.setVisibility(View.VISIBLE);

                                Uri fileUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                                        ? FileProvider.getUriForFile(getContext(), getContext().getPackageName()+".provider", downloadedFile)
                                        : Uri.fromFile(downloadedFile);

                                AudioWife aw = new AudioWife();
                                aw.init(getContext(), fileUri).useDefaultUi(viewHolder.mediaPlayerContainer, ((Activity) getContext()).getLayoutInflater());
                                aw.play();
                            }else{
                                Intent intent = new Intent();
                                intent.setAction(android.content.Intent.ACTION_VIEW);
                                String typeMime = fileDescriptor.getTypeMIME();
//                                intent.setDataAndType(Uri.fromFile(fileDescriptor.getFile()), typeMime);
                                intent.setDataAndType(Uri.fromFile(downloadedFile), typeMime);

                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                                    getContext().startActivity(Intent.createChooser(intent, "Open "+fileDescriptor.getExtension()));
                                }else{
                                    getContext().startActivity(intent);
                                }
                            }
                        }
                    });
                }
            }else{
                viewHolder.downloadProgress.setVisibility(View.GONE);
                viewHolder.filePreview.setVisibility(View.GONE);
                viewHolder.fileInfo.setVisibility(View.GONE);
            }
            //END FILE CHECKING
        try{
            if(message.getMessageDate() != null){
                msg_date = Util.txtDate(message.getMessageDate()).toString();
            }
        }catch (Exception e){}

        String msg_content_edit = msg_content;
        viewHolder.tContentMessage.setText(msg_content_edit);

        String lang = Util.AUTO_TRANSLATE;

        if( ! lang.equals("off")){
            try{
                String host = "https://translate.yandex.net/api/v1.5/tr.json/translate?";
                String key = "trnsl.1.1.20171114T160531Z.d80c7dc051e65103.bb38eac179b3269eb7a407816d0d808d455c28cd";

                String url = host + "lang="+lang+"&key="+key+"&text="+ URLEncoder.encode(msg_content, "utf-8");
                Util.log("TRANSLATING!!!");
                Util.log(url);
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                try{
                                    String res = response.getString("text");
                                    res = res.substring(2, res.length() - 2);

                                    String res_edit = res;
                                    for (String word : Util.blockedwords) {
                                        Pattern rx = Pattern.compile("\\b" + word + "\\b", Pattern.CASE_INSENSITIVE);
                                        res_edit = rx.matcher(res_edit).replaceAll(new String(new char[word.length()]).replace('\0', '*'));
                                    }
                                    viewHolder.tContentMessage.setText(res_edit);
                                }catch (Exception e){
                                    Util.log(e.toString());
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Util.log("translating error!!!");
                        Util.log(error.toString());
                    }
                });

                RequestQueue queue = Volley.newRequestQueue(context);
                queue.add(request);
            }catch (Exception e){
                Util.log(e.toString());
            }
        }


        viewHolder.tMessageDate.setText(msg_date);



        return convertView;

    }
}

class InstantMessagesViewHolder{
    public TextView tContentMessage;
    public TextView tMessageDate;
    public ImageView tSenderPhoto;
    public ImageView icStatus;
    public ImageView filePreview;
    public TextView fileInfo;
    public ProgressBar downloadProgress;
    public ImageView fileIcon;
    public RelativeLayout mediaPlayerContainer;

    public InstantMessagesViewHolder(View v, boolean isOwner){
        this.tContentMessage = (TextView) v.findViewById(R.id.content_message);
        this.tMessageDate = (TextView) v.findViewById(R.id.message_date);
        this.filePreview = (ImageView) v.findViewById(R.id.file_preview);
        this.fileInfo = (TextView) v.findViewById(R.id.file_info);
        this.downloadProgress = (ProgressBar) v.findViewById(R.id.download_progress);
        this.fileIcon = (ImageView) v.findViewById(R.id.file_icon);
        this.mediaPlayerContainer = (RelativeLayout) v.findViewById(R.id.media_player_container);

        if(!isOwner){
            this.tSenderPhoto = (ImageView) v.findViewById(R.id.sender_photo);
        }else{
            this.icStatus = (ImageView) v.findViewById(R.id.msg_status);
        }
    }
}
