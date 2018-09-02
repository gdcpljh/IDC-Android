package com.id.connect.diaspora.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.devs.readmoreoption.ReadMoreOption;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.id.connect.diaspora.MainActivity;
import com.id.connect.diaspora.R;
import com.id.connect.diaspora.model.JobModel;
import com.id.connect.diaspora.model.StoryModel;
import com.id.connect.diaspora.ui.activity.LoginActivity;
import com.id.connect.diaspora.utils.Util;
import com.joooonho.SelectableRoundedImageView;
import com.pixplicity.fontview.FontAppCompatTextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ${Deven} on 6/2/18.
 */
public class JobAdapter extends RecyclerView.Adapter<JobAdapter.ViewHolder> {
    private Context context;
    private ReadMoreOption readMoreOption;
    private ArrayList<JobModel> jobList;
    private ProgressDialog pDialog;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        FontAppCompatTextView mTextNama;
        FontAppCompatTextView mTextDesc;
        FontAppCompatTextView mTextLocation;
        FontAppCompatTextView mTextPosition;
        FontAppCompatTextView mTextPublished;
        FontAppCompatTextView mTextApply;

        ViewHolder(View v) {
            super(v);
            mTextNama = v.findViewById(R.id.tv_nama);
            mTextDesc = v.findViewById(R.id.tv_desc);
            mTextPosition = v.findViewById(R.id.tv_position);
            mTextLocation = v.findViewById(R.id.tv_location);
            mTextPublished = v.findViewById(R.id.tv_published);
            mTextApply = v.findViewById(R.id.tv_apply);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public JobAdapter(Context context, ArrayList<JobModel> jobList) {
        this.context = context;
        this.jobList = jobList;
        pDialog = new ProgressDialog(context);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public JobAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                    int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_job, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        JobModel job = jobList.get(position);
        pDialog.setMessage("Please wait..");

        SharedPreferences sp = context.getSharedPreferences("userLogin", MODE_PRIVATE);
        String diaspora_key = sp.getString(Util.DIASPORAS_CODE, "");

        holder.mTextNama.setText(job.getCompany_name());
        holder.mTextDesc.setText(job.getDescription());
        holder.mTextLocation.setText(job.getLocation());
        holder.mTextPosition.setText(job.getPosition());
        holder.mTextPublished.setText("Published by: "+job.getPublished_by());
        holder.mTextApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pDialog.show();
                Date currentTime = Calendar.getInstance().getTime();
                Map<String, Object> user = new HashMap<>();
                user.put("job_key", job.getJob_key());
                user.put("diaspora_key", diaspora_key);
                user.put("created_at", currentTime.toString());

                LoginActivity.db
                        .collection("apply")
                        .add(user)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                pDialog.dismiss();
                                Toasty.success(context, "Apply has been sent!").show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("DocumentReference", "Error adding document", e);
                            }
                        });
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return jobList.size();
    }
}