package edu.sutd.organice;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import androidx.recyclerview.widget.RecyclerView;

public class EventDataRecyclerAdapter extends RecyclerView.Adapter<EventDataRecyclerAdapter.EventDataViewHolder> {

    private static final String LOG_TAG = "EventDataRecyclerAdapter";

    private EventData[] eventData;
    private EventSelectionActivity activity;

    public static class EventDataViewHolder extends RecyclerView.ViewHolder {
        public View itemView;
        private TextView titleText;
        private TextView startText;
        private TextView endText;
        private TextView venueText;
        private TextView noteText;

        public EventData eventData;

        public EventDataViewHolder(
                View view,
                final EventSelectionActivity activity
        ){
            super(view);
            itemView = view;
            titleText = view.findViewById(R.id.title_text);
            startText = view.findViewById(R.id.start_text);
            endText = view.findViewById(R.id.end_text);
            venueText = view.findViewById(R.id.venue_text);
            noteText = view.findViewById(R.id.note_text);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.onItemClick(eventData);
                }
            });

        }

        public void updateData(EventData eventData) {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/YYYY HH:mm");

            this.eventData = eventData;
            titleText.setText(eventData.title);
            startText.setText("Start: " + format.format(eventData.dateStart));
            endText.setText("End: " + format.format(eventData.dateEnd));
            if (eventData.venue != null && !eventData.venue.isEmpty()) {
                venueText.setText("Venue: " + eventData.venue);
            }
            if (eventData.note != null && !eventData.note.isEmpty()) {
                noteText.setText("Note: " + eventData.note);
            }
        }
    }

    public EventDataRecyclerAdapter(EventSelectionActivity activity, EventData[] data) {
        this.activity = activity;
        eventData = data;
    }

    @Override
    public EventDataRecyclerAdapter.EventDataViewHolder onCreateViewHolder(
            ViewGroup parent,
            int viewType
    ) {
        View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.event_data_recycler_item, parent, false);
        EventDataViewHolder vh = new EventDataViewHolder(v, activity);
        return vh;
    }

    @Override
    public void onBindViewHolder(EventDataViewHolder holder, int position) {
        holder.updateData(eventData[position]);
    }

    @Override
    public int getItemCount() {
        return eventData.length;
    }
}
