package com.makbe.taskmanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.*;

public class MainActivity extends AppCompatActivity {
	private TaskAdapter taskAdapter;
	private List<Task> taskList;

	private TaskDbHelper dbHelper;

	private static final int ADD_TASK_REQUEST_CODE = 1;
	private static final int EDIT_TASK_REQUEST_CODE = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		dbHelper = new TaskDbHelper(this);

		RecyclerView recyclerView = findViewById(R.id.recycler_view_tasks);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));

		FloatingActionButton fabAddTask = findViewById(R.id.fab_add_task);
		fabAddTask.setOnClickListener(view -> {
			Intent intent = new Intent(this, AddEditTaskActivity.class);
			startActivityForResult(intent, ADD_TASK_REQUEST_CODE);
		});

		taskList = dbHelper.getAllTasks();
		sortTasksByDueDate();

		taskAdapter = new TaskAdapter(taskList);

		taskAdapter.setOnTaskOptionsClickListener(new TaskAdapter.OnTaskOptionsClickListener() {
			@Override
			public void onEditClick(int position) {
				Task task = taskList.get(position);
				Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
				intent.putExtra("isEditMode", true);
				intent.putExtra("position", position);
				intent.putExtra("taskId", task.getId());
				intent.putExtra("title", task.getTitle());
				intent.putExtra("description", task.getDescription());
				intent.putExtra("dueDate", task.getDueDate());
				startActivityForResult(intent, EDIT_TASK_REQUEST_CODE);
			}

			@Override
			public void onDeleteClick(int position) {
				Task task = taskList.get(position);
				dbHelper.deleteTask(task.getId());

				taskList.remove(position);
				taskAdapter.notifyDataSetChanged();

			}
		});

		recyclerView.setAdapter(taskAdapter);
	}

	private void sortTasksByDueDate() {
		taskList.sort(Comparator.comparing(Task::getDueDate));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == ADD_TASK_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
			String title = data.getStringExtra("title");
			String description = data.getStringExtra("description");
			String dueDate = data.getStringExtra("dueDate");

			Task newTask = new Task(title, description, dueDate);
			long id = dbHelper.addTask(newTask);
			newTask.setId(id);
			taskList.add(newTask);
			sortTasksByDueDate();
			taskAdapter.notifyDataSetChanged();
		}

		if (requestCode == EDIT_TASK_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
			String title = data.getStringExtra("title");
			String description = data.getStringExtra("description");
			String dueDate = data.getStringExtra("dueDate");
			long taskId = data.getLongExtra("taskId", 0);
			int position = data.getIntExtra("position", 0);

			Task updatedTask = new Task(title, description, dueDate);
			updatedTask.setId(taskId);

			int result = dbHelper.updateTask(updatedTask);

			if (result < 1) {
				Toast.makeText(this, "Couldn't update task!", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "Task updated successfully", Toast.LENGTH_LONG).show();
				taskList.set(position, updatedTask);
				sortTasksByDueDate();
				taskAdapter.notifyDataSetChanged();
			}
		}
	}

}
