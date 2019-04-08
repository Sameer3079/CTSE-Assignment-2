package lk.sliit.androidalarmsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CustomRecyclerViewAdapter
        extends RecyclerView.Adapter<CustomRecyclerViewAdapter.ViewHolder> {

    private List<Alarm> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean isInitializing = true;

    // data is passed into the constructor
    CustomRecyclerViewAdapter(Context context, List<Alarm> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_pref_name), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Alarm alarm = mData.get(position);
        holder.nameTextView.setText(alarm.getName());
        holder.timeTextView.setText(alarm.getTime());
        holder.isEnabled.setChecked(alarm.isEnabled());
        isInitializing = false;
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nameTextView;
        TextView timeTextView;
        CheckBox isEnabled;

        ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.alarmName);
            timeTextView = itemView.findViewById(R.id.time);
            isEnabled = itemView.findViewById(R.id.enabled);
            isEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isInitializing) {
                        return;
                    }
                    try {
                        RecyclerView recyclerView = (RecyclerView) buttonView.getParent().getParent();
                        int id = recyclerView.getChildAdapterPosition((View) buttonView.getParent());
                        String rawData = sharedPreferences.getString("alarms", "[]");
                        JSONArray alarmsArray = new JSONArray(rawData);
                        Object tempObj = alarmsArray.get(id);
                        JSONObject alarmObj = new JSONObject(tempObj.toString());
                        alarmObj.put("isEnabled", !alarmObj.getBoolean("isEnabled"));
                        alarmsArray.put(id, alarmObj.toString());
                        editor.putString("alarms", alarmsArray.toString());
                        editor.apply();
                        System.out.println("Saved");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            });
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            System.out.println("onClick() ----------------------");
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    Alarm getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}