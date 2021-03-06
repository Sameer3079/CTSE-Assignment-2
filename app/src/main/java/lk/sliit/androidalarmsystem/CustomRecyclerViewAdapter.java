package lk.sliit.androidalarmsystem;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import lk.sliit.androidalarmsystem.domain.Alarm;
import lk.sliit.androidalarmsystem.domain.AlarmCommand;

public class CustomRecyclerViewAdapter
        extends RecyclerView.Adapter<CustomRecyclerViewAdapter.ViewHolder> {

    private final static String TAG = "APP-CstmAdapter";

    private List<Alarm> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ItemLongClickListener mLongClickListener;
    private Context context;

    // Data is passed into the constructor
    public CustomRecyclerViewAdapter(Context context, List<Alarm> data) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // Inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    // Binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Alarm alarm = mData.get(holder.getAdapterPosition());
        holder.nameTextView.setText(alarm.getName());
        holder.timeTextView.setText(alarm.getTime());
        holder.isEnabled.setChecked(alarm.isEnabled());
        holder.isEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                alarm.setEnabled(isChecked);
                AlarmDatabaseHelper db = new AlarmDatabaseHelper(context);
                db.update(alarm);

                // Updating the alarm which as already been set
                Intent intent = new Intent(context, AlarmService.class);
                intent.putExtra("command", AlarmCommand.UPDATE_ALARM);
                intent.putExtra("alarmId", alarm.getId());
                context.startService(intent);
            }
        });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView nameTextView;
        TextView timeTextView;
        Switch isEnabled;

        ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.alarmName);
            timeTextView = itemView.findViewById(R.id.time);
            isEnabled = itemView.findViewById(R.id.alarmEnableSwitch);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Log.i(TAG, "onClick");
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            Log.i(TAG, "onLongClick");
            if (mLongClickListener != null)
                mLongClickListener.onItemLongClick(view, getAdapterPosition());
            return true;
        }
    }

    // convenience method for getting data at click position
    Alarm getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public void setLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface ItemLongClickListener {
        void onItemLongClick(View view, int position);
    }
}