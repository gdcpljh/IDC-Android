package com.example.helmi.diaspora.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.devs.readmoreoption.ReadMoreOption;
import com.example.helmi.diaspora.R;

/**
 * Created by ${Deven} on 6/2/18.
 */
public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {
    private Context context;
    private ReadMoreOption readMoreOption;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView mTextView;
        ViewHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.tv_status);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FeedAdapter(Context context) {
        this.context = context;
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
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        readMoreOption.addReadMoreTo(holder.mTextView,context.getString(R.string.dummy_text));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return 20;
    }
}