package com.example.hw2.adapter

//recyclerview
import android.annotation.SuppressLint
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.hw2.R
import com.example.hw2.addDataToDatabase
import com.example.hw2.data.detail
import com.example.hw2.queryIdFromDatabase
import com.example.hw2.updateDatabase




// 创建一个适配器类，继承自RecyclerView.Adapter
class Recycleadapter(
    val DetailList: MutableList<detail>,
    private val resultLauncher: ActivityResultLauncher<Intent>, // 用于跳转到detail界面
    private val setFloatingActionButtonVisibility: (Boolean) -> Unit,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_FOOTER = 1
    }

    var showFooter = true // 默认显示footer
    var setShowFinished = false // 默认不显示已完成的任务

    override fun getItemViewType(position: Int): Int {
        return if (position == DetailList.size) {
            TYPE_FOOTER
        } else {
            TYPE_ITEM
        }
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //在这里绑定控件
        val detailtitle: EditText = view.findViewById(R.id.detailtitle1)
        val radio_button: RadioButton = view.findViewById(R.id.radio_button)
        val more: ImageView = view.findViewById(R.id.more)
        val forpriority: TextView = view.findViewById(R.id.forpriority)
        val showtime: TextView = view.findViewById(R.id.showtime)
        val showbeizhu: TextView = view.findViewById(R.id.showbeizhu)
    }

    inner class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //在这里绑定控件
        val addtext: EditText = view.findViewById(R.id.footview_et)
        val addbutton: Button = view.findViewById(R.id.footview_btn)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem, parent, false)
            ViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.footview, parent, false)
            FooterViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val detail = DetailList[position]
            // 默认隐藏已经完成的item，只有点击查看已完成的任务才会显示
            if (setShowFinished) {
                holder.itemView.visibility = View.VISIBLE
                holder.itemView.layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ) //设置布局参数，宽度为match_parent，高度为wrap_content
            } else {
                if (detail.isChecked) {
                    holder.itemView.visibility = View.GONE
                    holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
                } else {
                    holder.itemView.visibility = View.VISIBLE
                    holder.itemView.layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ) //设置布局参数，宽度为match_parent，高度为wrap_content
                }
            }


            val priorities = arrayOf("!!!", "!!", "!")
            if (detail.priority in 0..2) {
                holder.forpriority.text = priorities[detail.priority!!]
                holder.forpriority.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.orange
                    )
                )
                holder.forpriority.visibility = View.VISIBLE
            } else {
                holder.forpriority.visibility = View.GONE
            }

            holder.detailtitle.setText(detail.title)//设置edittext的内容
            holder.radio_button.isChecked = detail.isChecked

            holder.detailtitle.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    holder.more.visibility = View.VISIBLE
                    holder.more.isClickable = true
                    // 隐藏floatingActionButton
                    setFloatingActionButtonVisibility(false)
                } else {
                    holder.more.visibility = View.GONE
                    setFloatingActionButtonVisibility(true)
                }
            }

            holder.detailtitle.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    // 在文本变化后，更新数据库
                    val position = holder.bindingAdapterPosition //获取位置
                    val detail = DetailList[position]//获取位置的实例
                    // 如何更新数据库！！！问题：将detail的title修改了然后保存，但是数据库中的title没有修改
                    //同时执行update数据库，这会导致数据库找不到数据项目，因为是根据title查找的
                    //解决方法：在update数据库之前，先获取修改前的title，然后查找这个id的实例，然后再update
                    // 获取beforeTextChanged中存入的id

                    val id =
                        holder.detailtitle.tag as Int //tag是Any类型，需要转换为Int，tag不属于detailtitle，而是属于holder
                    // 通过id查找实例

                    // 获取修改后的title
                    val title = holder.detailtitle.text.toString()
                    // 如果title和数据所有的title都不同，那么就更新数据库
                    var isTitleExist = false
                    for (item in DetailList) {
                        if (item.title == title && !item.isChecked) {
                            isTitleExist = true
                            break
                        }
                    }

                    // 更新数据库
                    if (!isTitleExist) {
                        detail.title = title
                        updateDatabase(holder.itemView.context, detail, id)
                    }
                    Log.d("afterTextChanged", "afterTextChanged: $id")
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // 在文本变化前，你可以在这里做一些事情，如果需要的话
                    val position = holder.bindingAdapterPosition //获取位置
                    val detail = DetailList[position]//获取位置的实例

                    val id = queryIdFromDatabase(holder.itemView.context, detail)
                    // 传给afterTextChanged函数
                    holder.detailtitle.tag =
                        id //将id存入tag中，以便在afterTextChanged中获取，因为afterTextChanged中无法获取position
                    Log.d("beforeTextChanged", "beforeTextChanged: $id")
                    Log.d("beforeTextChanged", "beforeTextChanged: $detail")

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // 在文本变化时，你可以在这里做一些事情，如果需要的话
                }
            })


            holder.radio_button.setOnClickListener {
                // 更新数据库
                val id = queryIdFromDatabase(holder.itemView.context, detail)
                // 点击后，将该item的isChecked属性取反
                detail.isChecked = !detail.isChecked
                holder.radio_button.isChecked = detail.isChecked
                updateDatabase(holder.itemView.context, detail, id)
            }

            holder.more.setOnClickListener {
                //跳转到detail界面
                val intent = Intent(
                    holder.itemView.context,
                    com.example.hw2.detail_activity::class.java
                ).apply {
                    putExtra("actions", "adapter")
                    // 传入detail实例
                    putExtra("detail", detail)
                    putExtra("originalId", queryIdFromDatabase(holder.itemView.context, detail))
                }
                resultLauncher.launch(intent) //启动activity
            }

            // 显示备注
            detail.description?.let {
                if (it.isNotEmpty()) {
                    holder.showbeizhu.text = "备注: $it"
                    holder.showbeizhu.visibility = View.VISIBLE
                }
            }


            // 显示时间
            fun formatDateTime(date: Long?, time: Long?): String? {
                if (date == null || date == 0L) return null
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd")
                val dateString = dateFormat.format(java.util.Date(date))
                var dateTimeText = "日期: $dateString"
                if (time != null && time != 0L) {
                    val timeFormat = java.text.SimpleDateFormat("HH:mm")
                    val timeString = timeFormat.format(java.util.Date(time))
                    dateTimeText += " $timeString"
                }
                return dateTimeText
            }

            val beginDateTimeText = formatDateTime(detail.begindate, detail.begintime)
            val endDateTimeText = formatDateTime(detail.enddate, detail.endtime)

            if (beginDateTimeText != null) {
                holder.showtime.text = "开始$beginDateTimeText"
                holder.showtime.visibility = View.VISIBLE
            }
            if (endDateTimeText != null) {
                holder.showtime.text = holder.showtime.text.toString() + "\n结束$endDateTimeText"
                holder.showtime.visibility = View.VISIBLE
            }
        } else if (holder is FooterViewHolder) {
            if (showFooter) {
                holder.addtext.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                    holder.addbutton.visibility = if (hasFocus) View.VISIBLE else View.GONE
                    setFloatingActionButtonVisibility(false)
                }
                holder.addbutton.setOnClickListener {
                    val title = holder.addtext.text.toString()
                    if (title.isEmpty()) {
                        Toast.makeText(holder.itemView.context, "标题不能为空", Toast.LENGTH_SHORT)
                            .show()
                    }
                    // 标题已存在
                    else if (DetailList.any { it.title == title && !it.isChecked }) {
                        Toast.makeText(holder.itemView.context, "标题已存在", Toast.LENGTH_SHORT)
                            .show()
                        // 清空输入框
                        holder.addtext.setText("")
                    } else {
                        val detail = detail(title)
                        addDataToDatabase(holder.itemView.context, detail)
                        DetailList.add(detail)
                        notifyItemInserted(DetailList.size - 1)
                        holder.addtext.setText("")
                    }
                }
            } else {
                holder.addbutton.visibility = View.GONE
            }
        }
    }


    override fun getItemCount(): Int {
        return if (showFooter) DetailList.size + 1 else DetailList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(detailList: List<detail>) {
        // 更新数据
        DetailList.clear()
        DetailList.addAll(detailList)
        notifyDataSetChanged()
    }


}