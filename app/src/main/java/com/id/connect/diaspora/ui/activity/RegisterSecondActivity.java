package com.id.connect.diaspora.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.id.connect.diaspora.R;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

public class RegisterSecondActivity extends AppCompatActivity implements Validator.ValidationListener {

    private Validator validator;

    @NotEmpty
    @BindView(R.id.input_city)
    MaterialEditText input_city;
    @NotEmpty
    @BindView(R.id.input_hometown)
    MaterialEditText input_hometown;
    @NotEmpty
    @BindView(R.id.input_major)
    MaterialEditText input_major;
    @NotEmpty
    @BindView(R.id.input_status)
    MaterialEditText input_status;
    @NotEmpty
    @BindView(R.id.input_institution)
    MaterialEditText input_institution;
    @BindView(R.id.registBtn)
    LinearLayout registBtn;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private String email;
    private String nama;
    private String password;
    private String negara;
    private String kota;
    private String kota_asal;
    private String jurusan;
    private String status;
    private String institusi;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_daftar_user_next);
        ButterKnife.bind(this);
        getExtrasFromIntent();
        mAuth = FirebaseAuth.getInstance();

        registBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validator.validate();
            }
        });

        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    private void getExtrasFromIntent()
    {
        if(getIntent().getExtras()!=null) {
            email = getIntent().getStringExtra("email");
            negara = getIntent().getStringExtra("negara");
            nama = getIntent().getStringExtra("nama");
            password = getIntent().getStringExtra("password");
        }
    }

    @Override
    public void onValidationSucceeded() {
        progressBar.setVisibility(View.VISIBLE);
        kota = input_city.getText().toString();
        kota_asal = input_hometown.getText().toString();
        jurusan = input_major.getText().toString();
        status = input_status.getText().toString();
        institusi = input_institution.getText().toString();

        db.collection("diasporas")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.getData().containsValue(email)) {
                                    Toasty.error(RegisterSecondActivity.this, "Account has been registered!");
                                    progressBar.setVisibility(View.GONE);
                                    Intent intent = new Intent(RegisterSecondActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    addToFirestore();
                                }
                                Log.d("QueryDocumentSnapshot", document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w("QueryDocumentSnapshot", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void addToFirestore() {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("", "createUserWithEmail:success");
                            FirebaseUser users = mAuth.getCurrentUser();
                            Map<String, Object> user = new HashMap<>();
                            user.put("city", kota);
                            user.put("country", negara);
                            user.put("diaspora_id", "1815");
                            user.put("email", email);
                            user.put("full_name", nama);
                            user.put("hometown", kota_asal);
                            user.put("major", jurusan);
                            user.put("password", password);
                            user.put("profile_pic", "");
                            user.put("status", status);
                            user.put("university", institusi);

                            db.collection("diasporas")
                                    .add(user)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d("DocumentReference", "DocumentSnapshot added with ID: " + documentReference.getId());
                                            progressBar.setVisibility(View.GONE);
                                            Intent intent = new Intent(RegisterSecondActivity.this, LoginActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("DocumentReference", "Error adding document", e);
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("DocumentReference", "createUserWithEmail:failure", task.getException());
                            /*Toast.makeText(RegisterSecondActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();*/
                        }

                        // ...
                    }
                });
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
}
