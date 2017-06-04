package com.theneutrinos.struo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class EventsActivity extends AppCompatActivity {

    private DatePicker datePicker;
    private TextView todayTextView;
    private TextView tomoTextView;
    private RecyclerView todaysEventsRecyclerView;
    private RecyclerView tomorrowsEventRecyclerView;
    private DatabaseReference rootRef;
    private DatabaseReference rootRef2;
    private FirebaseAuth auth;
    private String tomoDayS;
    private String tomoMonthS;
    private String tomoYearS;
    private String initDayS;
    private String initMonthS;
    private String initYearS;
    private String dayOfMonthS;
    private String monthOfYearS;
    private String yearS;
    private String completeDateS;
    private String tomoCompleteDateS;
    private GregorianCalendar gregorianCalendar;
    private GregorianCalendar gregorianCalendar1;
    private int day;
    private int initDay;
    private int tomoDay;
    private int month;
    private int initMonth;
    private int tomoMonth;
    private int year;
    private int initYear;
    private int tomoYear;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        datePicker = (DatePicker) findViewById(R.id.date_picker);
        auth = FirebaseAuth.getInstance();
        todaysEventsRecyclerView = (RecyclerView) findViewById(R.id.todays_events_recycler_view);
        todaysEventsRecyclerView.setHasFixedSize(true);
        todaysEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tomorrowsEventRecyclerView = (RecyclerView) findViewById(R.id.tomorrows_events_recycler_view);
        tomorrowsEventRecyclerView.setHasFixedSize(true);
        tomorrowsEventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        day = datePicker.getDayOfMonth();
        month = datePicker.getMonth();
        year = datePicker.getYear();
        gregorianCalendar = new GregorianCalendar();
        gregorianCalendar1 = new GregorianCalendar();
        gregorianCalendar1.add(Calendar.DATE, 1);
        initDay = gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH);
        initMonth = gregorianCalendar.get(GregorianCalendar.MONTH) + 1;
        initYear = gregorianCalendar.get(GregorianCalendar.YEAR);
        tomoDay = gregorianCalendar1.get(GregorianCalendar.DAY_OF_MONTH);
        tomoMonth = gregorianCalendar.get(GregorianCalendar.MONTH) + 1;
        tomoYear = gregorianCalendar.get(GregorianCalendar.YEAR);
        initYearS = Integer.toString(initYear);
        initMonthS = Integer.toString(initMonth);
        initDayS = Integer.toString(initDay);
        tomoDayS = Integer.toString(tomoDay);
        tomoMonthS = Integer.toString(tomoMonth);
        tomoYearS = Integer.toString(tomoYear);
        if (Integer.toString(initDay).length() == 1)
        {
            initDayS = "0" + initDayS;
        }
        if (Integer.toString(initMonth).length() == 1)
        {
            initMonthS = "0" + initMonthS;
        }
        if (Integer.toString(tomoDay).length() == 1)
        {
            tomoDayS = "0" + tomoDayS;
        }
        if (Integer.toString(tomoMonth).length() == 1)
        {
            tomoMonthS = "0" + tomoMonthS;
        }
        completeDateS = initDayS + initMonthS + initYearS;
        tomoCompleteDateS = tomoDayS + tomoMonthS +tomoYearS;
        datePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                dayOfMonthS = Integer.toString(dayOfMonth);
                monthOfYearS = Integer.toString(monthOfYear + 1);
                yearS = Integer.toString(year);
                if (Integer.toString(dayOfMonth).length() == 1)
                {
                    dayOfMonthS = ("0" + dayOfMonthS);
                }
                if(Integer.toString(monthOfYear).length() == 1)
                {
                    monthOfYearS = ("0" + monthOfYearS);
                }
                completeDateS = dayOfMonthS + monthOfYearS + yearS;
                Intent intent = new Intent(EventsActivity.this, AddEventActivity.class);
                intent.putExtra("day", dayOfMonthS)
                        .putExtra("month", monthOfYearS)
                        .putExtra("year", yearS)
                        .putExtra("date", completeDateS);
                finish();
                startActivity(intent);
            }
        });
        rootRef = FirebaseDatabase.getInstance().getReference().child("Events").child(completeDateS);
        rootRef2 = FirebaseDatabase.getInstance().getReference().child("Events").child(tomoCompleteDateS);
        rootRef.keepSynced(true);
        rootRef2.keepSynced(true);
        new setRecyclerViewAdapters().execute();
        todayTextView = (TextView) findViewById(R.id.text_view_today);
        tomoTextView = (TextView) findViewById(R.id.text_view_tomorrow);
        rootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String todayTextViewS = Long.toString(dataSnapshot.getChildrenCount());
                todayTextViewS = "Today (" + todayTextViewS + ")";
                todayTextView.setText(todayTextViewS);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        rootRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String tomorrowTextViewS = Long.toString(dataSnapshot.getChildrenCount());
                tomorrowTextViewS = "Tomorrow (" + tomorrowTextViewS + ")";
                tomoTextView.setText(tomorrowTextViewS);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    class setRecyclerViewAdapters extends AsyncTask<Void, Void, Void>
    {
        FirebaseRecyclerAdapter<EventsFeed, EventsFeedViewHolder> eventsFeedEventsFeedViewHolderFirebaseRecyclerAdapter;
        FirebaseRecyclerAdapter<EventsFeed, EventsFeedViewHolder> eventsFeedEventsFeedViewHolderFirebaseRecyclerAdapter1;
        @Override
        protected Void doInBackground(Void... params) {
            eventsFeedEventsFeedViewHolderFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<EventsFeed, EventsFeedViewHolder>(EventsFeed.class, R.layout.small_event_row, EventsFeedViewHolder.class, rootRef) {
                @Override
                protected void populateViewHolder(EventsFeedViewHolder viewHolder, final EventsFeed model, int position) {
                    final String eventKey = getRef(position).getKey();
                    viewHolder.setTitle(model.getTitle());
                    viewHolder.setDesc(model.getDescription());
                    viewHolder.setEndTime(model.getEndTime());
                    viewHolder.setStartTime(model.getStartTime());
                    viewHolder.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            goToAddEventActivityToday();
                        }
                    });
                    viewHolder.view.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if ((auth.getCurrentUser().getUid()).equals(model.getUserid()))
                            {
                                new AlertDialog.Builder(EventsActivity.this)
                                        .setTitle("Delete Event?")
                                        .setMessage("Do you really want to delete this event?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                DatabaseReference deleteRef = FirebaseDatabase.getInstance().getReference().child("Events").child(eventKey);
                                                deleteRef.removeValue();
                                                StorageReference deleteStorageRef = FirebaseStorage.getInstance().getReference().child("EventImages").child(model.getImage());
                                                deleteStorageRef.delete();
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        }).show();
                            }
                            return true;
                        }
                    });
                }
            };
            eventsFeedEventsFeedViewHolderFirebaseRecyclerAdapter1 = new FirebaseRecyclerAdapter<EventsFeed, EventsFeedViewHolder>(EventsFeed.class, R.layout.small_event_row, EventsFeedViewHolder.class, rootRef2) {
                @Override
                protected void populateViewHolder(EventsFeedViewHolder viewHolder, final EventsFeed model, int position) {
                    final String eventKey = getRef(position).getKey();
                    viewHolder.setTitle(model.getTitle());
                    viewHolder.setEndTime(model.getEndTime());
                    viewHolder.setStartTime(model.getStartTime());
                    viewHolder.setDesc(model.getDescription());
                    viewHolder.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            goToAddEventActivityTomorrow();
                        }
                    });
                    viewHolder.view.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if ((auth.getCurrentUser().getUid()).equals(model.getUserid()))
                            {
                                new AlertDialog.Builder(EventsActivity.this)
                                        .setTitle("Delete Event?")
                                        .setMessage("Do you really want to delete this event?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                DatabaseReference deleteRef = FirebaseDatabase.getInstance().getReference().child("Events").child(eventKey);
                                                deleteRef.removeValue();
                                                StorageReference delStorageRef = FirebaseStorage.getInstance().getReference().child("EventImages").child(model.getImage());
                                                delStorageRef.delete();
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        }).show();
                            }
                            return true;
                        }
                    });
                }
            };
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            todaysEventsRecyclerView.setAdapter(eventsFeedEventsFeedViewHolderFirebaseRecyclerAdapter);
            tomorrowsEventRecyclerView.setAdapter(eventsFeedEventsFeedViewHolderFirebaseRecyclerAdapter1);
            eventsFeedEventsFeedViewHolderFirebaseRecyclerAdapter.notifyDataSetChanged();
            eventsFeedEventsFeedViewHolderFirebaseRecyclerAdapter1.notifyDataSetChanged();
        }

        private void goToAddEventActivityToday()
        {
            Intent intent = new Intent(EventsActivity.this, AddEventActivity.class);
            intent.putExtra("day", initDayS)
                    .putExtra("month", initMonthS)
                    .putExtra("year", initYearS)
                    .putExtra("date", completeDateS);
            finish();
            startActivity(intent);
        }

        private void goToAddEventActivityTomorrow()
        {
            Intent intent = new Intent(EventsActivity.this, AddEventActivity.class);
            intent.putExtra("day", tomoDayS)
                    .putExtra("month", tomoMonthS)
                    .putExtra("year", tomoYearS)
                    .putExtra("date", tomoCompleteDateS);
            finish();
            startActivity(intent);
        }
    }

    public static class EventsFeedViewHolder extends RecyclerView.ViewHolder
    {
        View view;

        public EventsFeedViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setTitle(String title)
        {
            TextView textView = (TextView) view.findViewById(R.id.small_event_title_card_view);
            textView.setText(title);
        }

        private void setDesc(String desc)
        {
            TextView textView = (TextView) view.findViewById(R.id.small_event_desc_card_view);
            textView.setText(desc);
        }

        private void setStartTime(String startTime)
        {
            TextView textView = (TextView) view.findViewById(R.id.small_event_start_time_card_view);
            String finalS = "From: " + startTime;
            textView.setText(finalS);
        }

        private void setEndTime (String endTime)
        {
            TextView textView = (TextView) view.findViewById(R.id.small_event_end_time_card_view);
            String finalS = "To: " + endTime;
            textView.setText(finalS);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(EventsActivity.this, MainActivity.class));
    }
}
