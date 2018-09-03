package com.id.connect.diaspora.ui.activity;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ale.infra.application.RainbowIntent;
import com.ale.listener.SigninResponseListener;
import com.ale.listener.StartResponseListener;
import com.ale.rainbowsdk.Connection;
import com.ale.rainbowsdk.RainbowSdk;
import com.ale.service.RainbowService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.id.connect.diaspora.MainActivity;
import com.id.connect.diaspora.R;
import com.id.connect.diaspora.service.MessengerService;
import com.id.connect.diaspora.utils.Util;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity implements Validator.ValidationListener {

    private Validator validator;

    @NotEmpty
    @BindView(R.id.input_email)
    MaterialAutoCompleteTextView input_email;
    @NotEmpty
    @BindView(R.id.input_password)
    MaterialEditText input_password;
    @BindView(R.id.login_btn)
    LinearLayout login_btn;
    @BindView(R.id.btn_sign_up)
    CardView btn_sign_up;

    private String pUsername;
    private String pPassword;
    private String rUsername;
    private String rPassword;
    private String savedMail;
    private String []listsavedMail;
    private ProgressDialog pDialog;
    private FirebaseAuth mAuth;
    public static FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static FirebaseStorage storage = FirebaseStorage.getInstance();
    public static StorageReference storageRef = storage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_login);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait..");

        String[] perms = {
                "android.permission.CAMERA",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.RECORD_AUDIO"
        };
        int permsRequestCode = 200;

        ActivityCompat.requestPermissions(this, perms, permsRequestCode);

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pDialog.show();
                validator.validate();
            }
        });

        btn_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterFirstActivity.class);
                startActivity(intent);
            }
        });

        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    private void loginProcess() {
        pUsername = input_email.getText().toString();
        pPassword = input_password.getText().toString();

        if(pUsername.length() == 0 || pPassword.length() == 0){
            return;
        }

        mAuth.signInWithEmailAndPassword(pUsername, pPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("signInWithEmail", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            RainbowSdk.instance().connection().signin(pUsername, pPassword, Util.RAINBOW_HOST, signinResponseListener);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("signInWithEmail", "signInWithEmail:failure", task.getException());
                            Toasty.error(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private SigninResponseListener signinResponseListener = new SigninResponseListener() {
        @Override
        public void onSigninSucceeded() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    if( ! ArrayUtils.contains(listsavedMail, input_email.getText().toString())){
                        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();

                        editor.putString("saved_mail", input_email.getText().toString());
                        editor.commit();
                    }

                    NotificationCompat.Builder notificationBuilder = RainbowSdk.instance().getNotificationBuilder();
                    notificationBuilder.setContentText(getResources().getString(R.string.notif_connected));
                    PendingIntent contentIntent = PendingIntent.getActivity(RainbowSdk.instance().getContext(), 0, RainbowIntent.getLauncherIntent(getApplicationContext(), MainActivity.class), 0);
                    notificationBuilder.setContentIntent(contentIntent);

                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(RainbowService.EVENT_NOTIFICATION, notificationBuilder.build());

                    MessengerService.getInstance().startService();
                }
            });

            LoginActivity.db
                    .collection("diasporas")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if(document.getData().get("email").equals(input_email.getText().toString())) {
                                        Log.d("Koleksi", document.getId() + " => " + document.getData().get("email"));
                                        SharedPreferences.Editor editor = getSharedPreferences("userLogin", MODE_PRIVATE).edit();
                                        editor.putString(Util.USERNAME_CODE, rUsername);
                                        editor.putString(Util.PASSWORD_CODE, rPassword);
                                        editor.putString(Util.DIASPORAS_CODE, document.getId());
                                        editor.apply();
                                    }
                                }
                            } else {
                                Log.d("", "Error getting documents: ", task.getException());
                            }
                        }
                    });

            pDialog.dismiss();
            Intent intent = new Intent(RainbowSdk.instance().getContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        @Override
        public void onRequestFailed(RainbowSdk.ErrorCode errorCode, String s) {
            Util.log(errorCode.toString()+" "+s);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pDialog.dismiss();
                    Intent intent = new Intent(LoginActivity.this, InAppBrowserActivity.class);
                    intent.putExtra("url", "https://web.openrainbow.com/app/1.45.3/index.html#/subscribe");
                    startActivity(intent);
                    Toasty.info(getBaseContext(), "Account not registered at Rainbow, please register for Rainbow Platform").show();
                }
            });
        }
    };

    @Override
    public void onValidationSucceeded() {
        loginProcess();
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages ;)
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private StartResponseListener startResponseListener = new StartResponseListener() {
        @Override
        public void onStartSucceeded() {
            SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);

            Util.AUTO_TRANSLATE = sp.getString("AUTO_TRANSLATE", "off");

            String username = sp.getString(Util.USERNAME_CODE,"");
            String password = sp.getString(Util.PASSWORD_CODE,"");
            if(username.length() > 0 && password.length() > 0){
                RainbowSdk.instance().connection().signin(
                        username, password, "openrainbow.com", signingResponseListener
                );
            }else{
            }
        }

        @Override
        public void onRequestFailed(RainbowSdk.ErrorCode errorCode, String s) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Util.toast("Gagal menghubungi server", getApplicationContext());
                }
            });
        }
    };

    private SigninResponseListener signingResponseListener = new SigninResponseListener() {
        @Override
        public void onSigninSucceeded() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    NotificationCompat.Builder notificationBuilder = RainbowSdk.instance().getNotificationBuilder();
                    notificationBuilder.setContentText(getResources().getString(R.string.notif_connected));
                    PendingIntent contentIntent = PendingIntent.getActivity(RainbowSdk.instance().getContext(), 0, RainbowIntent.getLauncherIntent(getApplicationContext(), MainActivity.class), 0);
                    notificationBuilder.setContentIntent(contentIntent);

                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(RainbowService.EVENT_NOTIFICATION, notificationBuilder.build());
                    mNotificationManager.cancel(RainbowService.EVENT_NOTIFICATION);

                    MessengerService.getInstance().startService();
                }
            });

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        @Override
        public void onRequestFailed(RainbowSdk.ErrorCode errorCode, String s) {
            //showFrontPage();
        }
    };

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        Connection connection = RainbowSdk.instance().connection();

        if(connection.isConnected()){

        }else{
            connection.start(this.startResponseListener);
        }
    }
}
