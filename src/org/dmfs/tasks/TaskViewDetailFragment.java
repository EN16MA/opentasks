/*
 * TaskDetailFragment.java
 *
 * Copyright (C) 2012 Marten Gajda <marten@dmfs.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.dmfs.tasks;

import java.util.ArrayList;

import org.dmfs.provider.tasks.TaskContract.Tasks;
import org.dmfs.tasks.TaskListFragment.Callbacks;
import org.dmfs.tasks.model.Model;
import org.dmfs.tasks.utils.AsyncContentLoader;
import org.dmfs.tasks.utils.AsyncModelLoader;
import org.dmfs.tasks.utils.ContentValueMapper;
import org.dmfs.tasks.utils.OnContentLoadedListener;
import org.dmfs.tasks.utils.OnModelLoadedListener;
import org.dmfs.tasks.widget.TaskView;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


/**
 * A fragment representing a single Task detail screen. This fragment is either contained in a {@link TaskListActivity} in two-pane mode (on tablets) or a
 * {@link TaskDetailActivity} on handsets.
 * 
 * @author Arjun Naik <arjun@arjunnaik.in>
 */

public class TaskViewDetailFragment extends Fragment implements OnContentLoadedListener, OnModelLoadedListener
{
	/**
	 * The fragment argument representing the item ID that this fragment represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	private static final String TAG = "TaskViewDetailFragment";

	private static final String KEY_VALUES = "key_values";

	private static final ContentValueMapper CONTENT_VALUE_MAPPER = new ContentValueMapper()
		.addString(Tasks.ACCOUNT_TYPE, Tasks.ACCOUNT_NAME, Tasks.TITLE, Tasks.LOCATION, Tasks.DESCRIPTION, Tasks.GEO, Tasks.URL, Tasks.TZ, Tasks.DURATION)
		.addInteger(Tasks.PRIORITY, Tasks.LIST_COLOR, Tasks.TASK_COLOR, Tasks.COMPLETED, Tasks.STATUS, Tasks.CLASSIFICATION)
		.addLong(Tasks.LIST_ID, Tasks.DTSTART, Tasks.DUE);

	private static final String TASK_MODEL = null;

	/**
	 * The dummy content this fragment is presenting.
	 */
	private Uri taskUri;
	private Context appContext;

	ArrayList<ContentValues> mValues;
	ViewGroup mContent;
	Model mModel;

	private Intent appIntent;
	private Callback callback;


	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
	 */
	public TaskViewDetailFragment()
	{
	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		taskUri = getArguments().getParcelable(TaskViewDetailFragment.ARG_ITEM_ID);
		mValues = new ArrayList<ContentValues>();
		setHasOptionsMenu(true);
	}


	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);

		appContext = activity.getApplicationContext();
		appIntent = activity.getIntent();
		if (!(activity instanceof Callback)) {
			throw new IllegalStateException(
					"Activity must implement TaskViewDetailFragment callback.");
		}
		callback = (Callback) activity;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_task_view_detail, container, false);

		if (taskUri != null)
		{
			Log.d(TAG, "taskUri is not null");
			mContent = (ViewGroup) rootView.findViewById(R.id.content);

			if (savedInstanceState == null)
			{
				new AsyncContentLoader(appContext, this, CONTENT_VALUE_MAPPER).execute(taskUri);
			}
			else
			{
				mValues = savedInstanceState.getParcelableArrayList(KEY_VALUES);
				new AsyncModelLoader(appContext, this).execute("");
			}
		}
		else{
			Log.w(TAG, "taskUri is null!!!");
		}

		return rootView;
	}


	private void updateView()
	{
		final LayoutInflater inflater = (LayoutInflater) appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mContent.removeAllViews();
		for (ContentValues values : mValues)
		{
			TaskView editor = (TaskView) inflater.inflate(R.layout.task_view, mContent, false);
			editor.setModel(mModel);
			Log.d(TAG, "Values : " + values.toString());
			editor.setValues(values);
			mContent.addView(editor);
		}
		Log.d(TAG, "At the end of updateView");
	}


	@Override
	public void onContentLoaded(ContentValues values)
	{
		if (values == null)
		{
			Toast.makeText(appContext, "Could not load Task", Toast.LENGTH_LONG).show();
			return;
		}

		new AsyncModelLoader(appContext, this).execute(values.getAsString(Tasks.ACCOUNT_TYPE));

		mValues.add(values);
		// updateView();

	}


	@Override
	public void onModelLoaded(Model model)
	{
		if (model == null)
		{
			Toast.makeText(appContext, "Could not load Model", Toast.LENGTH_LONG).show();
			return;
		}

		mModel = model;

		updateView();

	}


	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList(KEY_VALUES, mValues);
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.task_detail_menu, menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.edit_task:
				callback.displayEditTask(taskUri);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public interface Callback
	{
		public void displayEditTask(Uri taskUri);
	}
}