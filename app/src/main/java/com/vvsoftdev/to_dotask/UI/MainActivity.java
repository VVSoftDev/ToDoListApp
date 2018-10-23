package com.vvsoftdev.to_dotask.UI;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.vvsoftdev.to_dotask.AppExecutors;
import com.vvsoftdev.to_dotask.utilities.ReminderUtils;
import com.vvsoftdev.to_dotask.viewmodel.MainViewModel;
import com.vvsoftdev.to_dotask.R;
import com.vvsoftdev.to_dotask.database.AppDatabase;
import com.vvsoftdev.to_dotask.database.TaskEntry;

import java.util.List;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class MainActivity extends AppCompatActivity implements TaskAdapter.ItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    // Constant for logging
    private static final String TAG = MainActivity.class.getSimpleName();
    // Member variables for the adapter and RecyclerView
    private RecyclerView mRecyclerView;
    private TaskAdapter mAdapter;

    private AppDatabase mDb;

    //boolean used for know if notification already scheduled

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupSharedPreferences();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the RecyclerView to its corresponding view
        mRecyclerView = findViewById(R.id.recyclerViewTasks);

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new TaskAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        mRecyclerView.addItemDecoration(decoration);

        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Here is where you'll implement swipe to delete
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();
                        List<TaskEntry> tasks = mAdapter.getTasks();
                        mDb.taskDao().deleteTask(tasks.get(position));
                        if (mAdapter.getItemCount()<1)
                            ReminderUtils.cancelReminder();
                    }
                });
            }
        }).attachToRecyclerView(mRecyclerView);

        /*
         Set the Floating Action Button (FAB) to its corresponding View.
         Attach an OnClickListener to it, so that when it's clicked, a new intent will be created
         to launch the AddTaskActivity.
         */
        FloatingActionButton fabButton = findViewById(R.id.fab);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a new intent to start an AddTaskActivity
                Intent addTaskIntent = new Intent(MainActivity.this, AddTaskActivity.class);
                //if no data, then it'll be the first task added : notify it
                if(mAdapter.getItemCount()<1)
                    addTaskIntent.putExtra(AddTaskActivity.IS_FIRST_TASK, true);
                startActivity(addTaskIntent);
            }
        });

        mDb = AppDatabase.getInstance(getApplicationContext());
        setupViewModel();

    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getTasks().observe(this, new Observer<List<TaskEntry>>() {
            @Override
            public void onChanged(@Nullable List<TaskEntry> taskEntries) {
                Log.d(TAG, "Updating list of tasks from LiveData in ViewModel");
                mAdapter.setTasks(taskEntries);
                setupNotificationPreference();
            }
        });
    }

    @Override
    public void onItemClickListener(int itemId) {
        // Launch AddTaskActivity adding the itemId as an extra in the intent
        Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
        intent.putExtra(AddTaskActivity.EXTRA_TASK_ID, itemId);
        startActivity(intent);
    }

    private void setupSharedPreferences() {
        // Get all of the values from shared preferences to set it up
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        loadColorFromPreferences(sharedPreferences);
        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void setupNotificationPreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        launchNotification(sharedPreferences);
    }

    //Load the theme color picked in settings, by default red
    private void loadColorFromPreferences(SharedPreferences sharedPreferences) {
        if(sharedPreferences.getString(getString(R.string.pref_color_key),getString(R.string.pref_color_red_value))
            .equals(getString(R.string.pref_color_red_value))){
            this.setTheme(R.style.AppThemeRed);
        } else if(sharedPreferences.getString(getString(R.string.pref_color_key),getString(R.string.pref_color_red_value))
                .equals(getString(R.string.pref_color_blue_value))){
            this.setTheme(R.style.AppThemeBlue);
        } else if(sharedPreferences.getString(getString(R.string.pref_color_key),getString(R.string.pref_color_red_value))
                .equals(getString(R.string.pref_color_green_value))) {
            this.setTheme(R.style.AppThemeGreen);
        }

    }

    // launch or not
    private void launchNotification(SharedPreferences sharedPreferences){
        if (mAdapter.getItemCount()>0 && sharedPreferences.getBoolean(getString(R.string.pref_reminder_key),false)) {
            ReminderUtils.scheduleToDoTaskReminder(this);
        } else if (mAdapter.getItemCount()<1 && sharedPreferences.getBoolean(getString(R.string.pref_reminder_key),false)){
            ReminderUtils.cancelReminder();
        } else if (!sharedPreferences.getBoolean(getString(R.string.pref_reminder_key),false))
            ReminderUtils.cancelReminder();
    }


    // Updates the screen if the shared preferences change. This method is required when you make a
    // class implement OnSharedPreferenceChangedListener
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
         if (key.equals(getString(R.string.pref_color_key))) {
            loadColorFromPreferences(sharedPreferences);
            //todo need to find better way to achieve refresh
             recreate();
        } else if (key.equals(getString(R.string.pref_reminder_key)))
             launchNotification(sharedPreferences);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister MainActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Methods for setting up the menu
     **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our visualizer_menu layout to this menu */
        inflater.inflate(R.menu.menu_main, menu);
        /* Return true so that the visualizer_menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

