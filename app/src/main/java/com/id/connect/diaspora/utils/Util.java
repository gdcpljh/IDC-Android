package com.id.connect.diaspora.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ale.infra.contact.Contact;
import com.ale.infra.manager.room.Room;
import com.id.connect.diaspora.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class Util {
    public static String USERNAME_CODE = "u123321u";
    public static String PASSWORD_CODE = "p123321p";
    public static String DIASPORAS_CODE = "d123321d";

    public static String AUTO_TRANSLATE = "off";

    public static String BOT_JABBER_ID = "9ae962aed05543299153972ac4405a40@openrainbow.com";
    public static String BOT_MAIL = "ale.hotel.bot@gmail.com";
    public static String BOT_INTERNAL_JABBER_ID = "3402a13e33f54d289a9ad9d07e98481b@openrainbow.com";
    public static String BOT_RECEPTIONIST_JABBER_ID = "8ae895c398454426badbd6637b68f3fc@openrainbow.com";
    public static String BOT_ROOM_SERVICE_JABBER_ID = "5080b72e950240aaa94e7055e9dac7f6@openrainbow.com";
    public static String BOT_HOUSE_KEEPING_JABBER_ID = "a5e6ddb662cf44b4bcf015b7b94fbb13@openrainbow.com";
    public static String BOT_RESTAURANT_JABBER_ID = "13191e3cf5d94af384a71711323ca154@openrainbow.com";
    public static String BOT_EMERGENCY_JABBER_ID = "6cdb22f6e0cf4be58a81cb5d40335dd9@openrainbow.com";

    public static String FAKE_HOST = "@aptiv8.demo";
    public static String RAINBOW_HOST = "openrainbow.com";

    public static void log(String msg){
        Log.d("ALE_HOTEL", msg);
    }
    private static Resources resources = Resources.getSystem();

    public static String lastStatus = "";

    public static void toast(String msg, Context context){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static Contact tempContact;
    public static Room tempRoom;
    public static int tempSID;

    public static String[] blockedwords;

    public static String txtFileSize(long fileSize){
        if(fileSize >= (1024*1024)){
            return String.valueOf(fileSize/(1024*1024))+" MB";
        }else{
            return String.valueOf(fileSize / 1024)+" KB";
        }
    }

    public static String getMime(String extension){
        if(extension.startsWith(".")) extension = extension.substring(1);

        if(extension.equals("jpg") || extension.equals("jpeg") || extension.equals("bmp") || extension.equals("png")){
           return "image";
        }else if(extension.equals("pdf")){
            return "pdf";
        }else if(extension.equals("doc") || extension.equals("docx")){
            return "doc";
        }else if(extension.equals("xls") || extension.equals("xlsx")){
            return "xls";
        }else if(extension.equals("ppt") || extension.equals("pptx")){
            return "ppt";
        }else if(extension.equals("mp3") || extension.equals("wav") || extension.equals("flac") || extension.equals("mid") || extension.equals("m4a")){
            return "audio";
        }else if(extension.equals("mp4") || extension.equals("mpeg") || extension.equals("3gp")){
            return "video";
        }else{
            return "file";
        }
    }

    public static String strBitmap(Bitmap bitmap){
        bitmap = scaledBitmap(bitmap);
        return "data:image/png;base64, "+encodeTobase64(bitmap);
    }

    public static String encodeTobase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();

        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public static Bitmap scaledBitmap(Bitmap src){
        return _scaledBitmap(src, 200, 200);
    }
    public static Bitmap _scaledBitmap(Bitmap src, float width, float height){
        Bitmap background = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);

        float originalWidth = src.getWidth();
        float originalHeight = src.getHeight();

        Canvas canvas = new Canvas(background);

        float scale = width / originalWidth;

        float xTranslation = 0.0f;
        float yTranslation = (height - originalHeight * scale) / 2.0f;

        Matrix transformation = new Matrix();
        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);

        Paint paint = new Paint();
        paint.setFilterBitmap(true);

        canvas.drawBitmap(src, transformation, paint);

        return background;
    }

    public static int hash(String s) {
        int h = 0;
        for (int i = 0; i < s.length(); i++) {
            h = 31 * h + s.charAt(i);
        }
        return h;
    }

    public static void bottomNavdisableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                item.setShiftingMode(false);
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
    }

    public static CharSequence txtTime(Date d){
        SimpleDateFormat t_format = new SimpleDateFormat("hh:mm a");
        return t_format.format(d);
    }

    public static CharSequence txtDate(Date d){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        Calendar now = Calendar.getInstance();
        StringBuffer dateStr = new StringBuffer();

        SimpleDateFormat t_format = new SimpleDateFormat("hh:mm a");
        SimpleDateFormat d_format = new SimpleDateFormat("dd/MM/yy");
        SimpleDateFormat d_format_week = new SimpleDateFormat("EEE ");

        int days = daysBetween(calendar.getTime(), now.getTime());

        if(days == 0){
            int minutes = hoursBetween(calendar.getTime(), now.getTime());
            //int hours = minutes / 60;

            if(minutes < 1){
                dateStr.append(resources.getString(R.string.just_now));
            }else{
                dateStr.append(t_format.format(d));
            }
        }else{
            if(days == 1){
                dateStr.append(resources.getString(R.string.yesterday)+" ");
                dateStr.append(t_format.format(d));
            }else if(days < 7){
//                String[] arrdays = new String[] { "min", "sen", "sel", "rab", "kam", "jum", "sab" };
//                String _day = arrdays[calendar.get(Calendar.DAY_OF_WEEK)];
//                dateStr.append(_day);
                dateStr.append(d_format_week.format(d));
                dateStr.append(t_format.format(d));
            }else{
                dateStr.append(d_format.format(d));
            }
        }

        return dateStr;
        //return DateUtils.getRelativeTimeSpanString(d.getTime(), now, DateUtils.MINUTE_IN_MILLIS);
    }

    private static int minuteBetween(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / DateUtils.SECOND_IN_MILLIS);
    }

    public static int hoursBetween(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / DateUtils.MINUTE_IN_MILLIS);
    }

    public static int daysBetween(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / DateUtils.DAY_IN_MILLIS);
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Bitmap mergeMultiple(Bitmap[] parts){
        Bitmap result = Bitmap.createBitmap(50 * 2, 50 * 2, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();

            for (int i = 0; i < parts.length; i++) {
                Bitmap _b = Bitmap.createScaledBitmap(parts[i], 50,50,false);
                canvas.drawBitmap(_b, _b.getWidth() * (i % 2), _b.getHeight() * (i / 2), paint);
            }

            return result;
    }

    public static String presenceStatus(String _presence){
        HashMap<String, String> presenceList = new HashMap<String, String>();
        presenceList.put("offline", "offline");
        presenceList.put("online","online");
        presenceList.put("mobile_online","mobile");
        presenceList.put("away","away");
        presenceList.put("manual_away", "away");
        presenceList.put("xa","xa");
        presenceList.put("DoNotDisturb","don\'t disturb");
        presenceList.put("DND_presentation","DND presentation");
        presenceList.put("busy","busy");
        presenceList.put("busy_audio","busy (audio)");
        presenceList.put("busy_video","busy (video)");
        presenceList.put("busy_phone","busy (phone)");
        presenceList.put("subscribe","subscribe");
        presenceList.put("unsubscribed","unsubscribe");

        return presenceList.get(_presence);
    }

    public static String getPath(Context context, Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            else
            if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static void listDynamicHeight(ListView mListView) {
        ListAdapter mListAdapter = mListView.getAdapter();
        if (mListAdapter == null) {
            return;
        }
        int height = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        for (int i = 0; i < mListAdapter.getCount(); i++) {
            View listItem = mListAdapter.getView(i, null, mListView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            height += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = mListView.getLayoutParams();
        params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));
        mListView.setLayoutParams(params);
        mListView.requestLayout();
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    public static int generateColor(String str){
        int [] pallette = {
                Color.parseColor("#3498db"), //biru laut
                Color.parseColor("#2980b9"), //biru gelap
                Color.parseColor("#d35400"), //oranye labu,
                Color.parseColor("#f39c12"), //kuning
                Color.parseColor("#e74c3c"), //merah
                Color.parseColor("#16a085"), //green sea
                Color.parseColor("#27ae60"), //toska
                Color.parseColor("#8e44ad"), //ungu
                Color.parseColor("#34495e"), //aspal
        };
        int randColor = Color.parseColor("#F26722");

        try{
            Calendar calendar = Calendar.getInstance();
            int i = calendar.get(Calendar.DAY_OF_WEEK);
            randColor = pallette[Math.abs((str.hashCode()+i) % pallette.length)];
        }catch (Exception e){}

        return randColor;
    }
}
