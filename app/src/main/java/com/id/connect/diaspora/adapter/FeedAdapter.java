package com.id.connect.diaspora.adapter;

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
import android.widget.Toast;

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
import com.id.connect.diaspora.model.StoryModel;
import com.id.connect.diaspora.service.MessengerService;
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
public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {
    private Context context;
    private ReadMoreOption readMoreOption;
    private ArrayList<StoryModel> storyList;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView mTextView;
        FontAppCompatTextView mTextNama;
        FontAppCompatTextView mTextSpeciality;
        FontAppCompatTextView mTextTime;
        SelectableRoundedImageView mImage;
        LinearLayout ll_like;
        LinearLayout ll_comment;
        ImageView iv_like;
        FontAppCompatTextView tv_like;

        ViewHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.tv_status);
            mTextNama = v.findViewById(R.id.txt_nama);
            mTextSpeciality = v.findViewById(R.id.txt_passion);
            mTextTime = v.findViewById(R.id.txt_time);
            mImage = v.findViewById(R.id.iv_gambar);
            ll_like = v.findViewById(R.id.ll_like);
            ll_comment = v.findViewById(R.id.ll_comment);
            iv_like = v.findViewById(R.id.iv_like);
            tv_like = v.findViewById(R.id.tv_like);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FeedAdapter(Context context, ArrayList<StoryModel> storyList) {
        this.context = context;
        this.storyList = storyList;
        readMoreOption = new ReadMoreOption.Builder(context)
                .textLength(100)
                .moreLabel("Lainnya")
                .moreLabelColor(Color.RED)
                .build();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FeedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feed, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        StoryModel story = storyList.get(position);
        holder.ll_comment.setVisibility(View.GONE);
        loadProfileByKey(story.getDiaspora_key(), holder.mTextNama, holder.mTextSpeciality);
        holder.mTextTime.setText(story.getCreated_at());
        holder.ll_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                giveLike(story.getStory_key(), holder.iv_like, holder.tv_like);
            }
        });
        if(story.getImg_url().isEmpty()) {
            holder.mImage.setVisibility(View.GONE);
        } else {
            Picasso.get().load(story.getImg_url()).into(holder.mImage);
        }
        readMoreOption.addReadMoreTo(holder.mTextView,story.getStatus());
        loadLikes(story.getStory_key(), holder.tv_like);
    }

    int likesCount;

    private void loadLikes(String story_key, FontAppCompatTextView tv_like) {
        SharedPreferences sp = context.getSharedPreferences("userLogin", MODE_PRIVATE);
        String diaspora_key = sp.getString(Util.DIASPORAS_CODE, "");
        likesCount = 0;
        LoginActivity.db
                .collection("likes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.getData().get("diaspora_key").equals(diaspora_key)) {
                                    likesCount++;
                                }
                            }

                            tv_like.setText(tv_like.getText().toString());
                        } else {
                            Log.d("", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    boolean isLikes = false;
    private void giveLike(String story_key, ImageView iv_like, FontAppCompatTextView tv_like) {
        SharedPreferences sp = context.getSharedPreferences("userLogin", MODE_PRIVATE);
        String diaspora_key = sp.getString(Util.DIASPORAS_CODE, "");

        LoginActivity.db
                .collection("likes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.getData().get("diaspora_key").equals(diaspora_key)) {
                                    isLikes = true;
                                }
                            }
                        } else {
                            Log.d("", "Error getting documents: ", task.getException());
                        }
                    }
                });

        if(!isLikes) {
            Date currentTime = Calendar.getInstance().getTime();
            Map<String, Object> user = new HashMap<>();
            user.put("story_key", story_key);
            user.put("diaspora_key", diaspora_key);
            user.put("created_at", currentTime.toString());

            LoginActivity.db
                    .collection("likes")
                    .add(user)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            iv_like.setColorFilter(ContextCompat.getColor(context, R.color.link_blue));
                            tv_like.setTextColor(ContextCompat.getColor(context, R.color.link_blue));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("DocumentReference", "Error adding document", e);
                        }
                    });
        } else {
            LoginActivity.db.collection("likes").document(story_key)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            iv_like.setColorFilter(ContextCompat.getColor(context, R.color.colortextstory));
                            tv_like.setTextColor(ContextCompat.getColor(context, R.color.colortextstory));
                            Log.d("", "DocumentSnapshot successfully deleted!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("", "Error deleting document", e);
                        }
                    });
        }

        //loadLikes(story_key, tv_like);
    }

    private void loadProfileByKey(String diaspora_key, FontAppCompatTextView nama,
                                  FontAppCompatTextView speciality) {
        LoginActivity.db
                .collection("diasporas")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.getId().equals(diaspora_key)) {
                                    nama.setText(document.getData().get("full_name").toString());
                                    speciality.setText(document.getData().get("major").toString());
                                }
                            }
                        } else {
                            Log.d("", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return storyList.size();
    }
}