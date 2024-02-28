package com.example.hw2


import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.hw2.ui.theme.Hw2Theme
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.hw2.adapter.Recycleadapter
import com.example.hw2.data.detail
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import android.provider.Settings
import android.app.NotificationManager
import android.content.Context
import android.widget.Button
import android.widget.PopupMenu
import androidx.activity.result.ActivityResultLauncher

import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var checkmore: Button
    private lateinit var addreminder: FloatingActionButton
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerViewReminders)
        searchView = findViewById(R.id.search_view)
        checkmore = findViewById(R.id.checkmore)
        addreminder = findViewById(R.id.addReminderButton)

        setupRecyclerView()
        setupSearchView()
        setupCheckMoreButton()
        setupAddReminderButton()

        // 启动后台任务
        val reminderWorkerRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this).enqueue(reminderWorkerRequest)
    }


    //初始化recyclerview，用于显示数据和删除数据
    private fun setupRecyclerView() {
        val detailList = queryDataFromDatabase(this)
        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        // registerForActivityResult()方法的第一个参数是一个ActivityResultContract对象，这个对象负责创建Intent，
        // 当Activity结束时，会将Intent传递给onActivityResult()方法。
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    updateRecyclerView()
                }
            }
        // Recycleadapter是自己写的一个类，用于将数据和视图绑定，这里的adapter就是Recycleadapter的一个实例
        // 通过adapter.setData()方法将detailList中的数据传递给Recycleadapter，然后通过adapter.notifyDataSetChanged()方法
        // 通知RecyclerView更新数据，resultLauncher是一个ActivityResultLauncher对象，用于启动detail_activity
        val adapter =
            Recycleadapter(detailList, resultLauncher, ::setFloatingActionButtonVisibility)
        recyclerView.adapter = adapter

        // ItemTouchHelper是一个辅助类，用于实现RecyclerView的拖拽和滑动删除功能
        val itemTouchHelperCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                // onMove()方法用于实现拖拽功能，这里不需要，所以返回false
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                // onSwiped()方法用于实现滑动删除功能
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    detailList.removeAt(position)
                    recyclerView.adapter!!.notifyItemRemoved(position)
                    // 删除数据库中的数据
                    deleteDataFromDatabase(this@MainActivity, position)
                }
            }
        // 将ItemTouchHelper和RecyclerView绑定
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    //初始化searchview，用于搜索功能
    private fun setupSearchView() {
        val nestedScrollView = findViewById<NestedScrollView>(R.id.NestedScrollView)
        nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY <= 0) {
                searchView.visibility = View.VISIBLE
                checkmore.visibility = View.GONE
            } else if (scrollY > 100) {
                searchView.visibility = View.GONE
                checkmore.visibility = View.VISIBLE
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                updateRecyclerView(newText)
                return false
            }
        })
    }

    private fun setupCheckMoreButton() {
        var clickshowfinished = false
        var showFinishedTitle = "显示已完成的任务"

        checkmore.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
            popupMenu.menu.findItem(R.id.showfinished).title = showFinishedTitle
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.showfinished -> {
                        clickshowfinished = !clickshowfinished
                        val adapter = recyclerView.adapter as Recycleadapter
                        adapter.setShowFinished = clickshowfinished
                        showFinishedTitle =
                            if (clickshowfinished) "隐藏已完成的任务" else "显示已完成的任务"
                        adapter.notifyDataSetChanged()
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    private fun setupAddReminderButton() {
        addreminder.setOnClickListener {
            val intent = Intent(this, detail_activity::class.java)
            intent.putExtra("actions", "main")
            resultLauncher.launch(intent)
        }
    }

    private fun updateRecyclerView(filter: String? = null) {
        val detailList = queryDataFromDatabase(this)
        // 如果filter不为空，就将detailList中的数据过滤，只保留包含filter的数据
        // 用于查询功能，不为空就只保存输入框里的内容进行查询
        val filteredList = if (filter != null) {
            detailList.filter { it.title.contains(filter) }
        } else {
            detailList
        }
        (recyclerView.adapter as Recycleadapter).apply {
            setData(filteredList)
            showFooter = filter.isNullOrEmpty() // 如果filter为空，就显示footer,否则不显示
            notifyDataSetChanged()
        }
    }

    private fun setFloatingActionButtonVisibility(isVisible: Boolean) {
        addreminder.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}