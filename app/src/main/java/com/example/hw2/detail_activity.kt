package com.example.hw2

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.util.Linkify
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import android.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.hw2.data.detail
import com.example.hw2.databinding.ActivityDetailBinding
import java.io.File
import java.io.FileOutputStream


class detail_activity : AppCompatActivity() {
    private lateinit var photoFile: File
    private lateinit var imageUri: Uri

    @SuppressLint("SimpleDateFormat", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        // 获取传递的detail实例
        val detail = intent.getParcelableExtra<detail>("detail")
        val beizhu = binding.reminderBeizhu
        // 设置标题
        val detailtitle = binding.detailtitle
        if (detail != null) {
            detailtitle.setText(detail.title)
            beizhu.setText(detail.description)
            // 设置开始日期
            if (detail.begindate != 0.toLong() && detail.begindate != null) {
                val format = java.text.SimpleDateFormat("yyyy-MM-dd") // 设置日期格式
                val date = format.format(detail.begindate)
                val begindate = binding.begindate
                begindate.isChecked = true
                begindate.text = date
            }

            // 设置结束日期
            if (detail.enddate != 0.toLong() && detail.enddate != null) {
                val format = java.text.SimpleDateFormat("yyyy-MM-dd") // 设置日期格式
                val date = format.format(detail.enddate)
                val enddate = binding.enddate
                enddate.isChecked = true
                enddate.text = date
            }
            // 设置开始时间
            if (detail.begintime != 0.toLong() && detail.begintime != null) {
                val format = java.text.SimpleDateFormat("HH:mm") // 设置日期格式
                val time = format.format(detail.begintime)
                val begintime = binding.begintime
                begintime.isChecked = true
                begintime.text = time
            }
            // 设置结束时间
            if (detail.endtime != 0.toLong() && detail.endtime != null) {
                val format = java.text.SimpleDateFormat("HH:mm") // 设置日期格式
                val time = format.format(detail.endtime)
                val endtime = binding.endtime
                endtime.isChecked = true
                endtime.text = time
            }
            // 初始化图片
            val file = detail.imageUrl?.let { File(filesDir, it) }
            if (file != null && file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                val userimage = binding.userimage
                userimage.setImageBitmap(bitmap)
                userimage.visibility = View.VISIBLE
            }
            // 初始化文件
            val file2 = detail.fileUrl?.let { File(filesDir, it) }
            if (file2 != null && file2.exists()) {
                val userfile = binding.filename
                userfile.text = file2.name
                userfile.textSize = 20F //设置字体大小
                userfile.paint.isUnderlineText = true //设置下划线
                // 设置字体颜色为蓝色
                userfile.setTextColor(resources.getColor(R.color.darkblue))
                Linkify.addLinks(userfile, Linkify.WEB_URLS)
                userfile.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    val uri =
                        FileProvider.getUriForFile(this, "com.example.hw2.fileprovider", file2)
                    intent.setDataAndType(uri, "*/*")
                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    startActivity(intent)
                }
                userfile.visibility = View.VISIBLE
            }

            // 备注


        } else {
            detailtitle.hint = "请输入标题"
        }


        detailtitle.setOnClickListener {
            detailtitle.setSelection(detailtitle.text.length)//设置光标位置
        }

        // 设置返回按钮
        val cancel = findViewById<Button>(R.id.cancel_btn)
        cancel.setOnClickListener {
            finish()
        }

        // 设置begindate按钮，
        val begindate = findViewById<SwitchCompat>(R.id.begindate)
        begindate.setOnClickListener {
            if (begindate.isChecked) {
                val datePickerDialog = DatePickerDialog(this)
                datePickerDialog.show()
                datePickerDialog.setOnDateSetListener { _, year, month, dayOfMonth ->
                    val date = "$year-${month + 1}-$dayOfMonth"
                    begindate.text = date
                }
            } else {
                // 取消修改，没有日期
                begindate.text = ""
            }
        }

        // 设置enddate按钮
        val enddate = findViewById<SwitchCompat>(R.id.enddate)
        enddate.setOnClickListener {
            if (enddate.isChecked) {
                val datePickerDialog = DatePickerDialog(this)
                datePickerDialog.show()

                if (!begindate.isChecked) {
                    // 设置开始日期为当前日期
                    begindate.isChecked = true
                    begindate.text =
                        java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())

                }

                // 如果结束日期小于开始日期，提示错误
                datePickerDialog.setOnDateSetListener { _, year, month, dayOfMonth ->
                    val date = "$year-${month + 1}-$dayOfMonth"
                    val format = java.text.SimpleDateFormat("yyyy-MM-dd") // 设置日期格式
                    val endDate = format.parse(date) // 将用户选择的日期转换为Date类型

                    val beginDate = format.parse(begindate.text.toString())// 将开始日期转换为Date类型
                    if (endDate != null) {
                        if (endDate.before(beginDate)) {
                            enddate.text = ""
                            datePickerDialog.dismiss() // 关闭日期选择器
                            val toast = Toast.makeText(
                                this,
                                "结束日期不能小于开始日期",
                                Toast.LENGTH_SHORT
                            )
                            toast.show()
                            // 修改switch按钮的状态为关闭
                            enddate.isChecked = false
                        } else {
                            enddate.text = date

                        }
                    }
                }


            } else {
                // 取消修改，没有日期
                enddate.text = ""
            }
        }

        // 设置开始时间按钮
        val begintime = findViewById<SwitchCompat>(R.id.begintime)
        begintime.setOnClickListener {
            if (begintime.isChecked) {
                // 如果用户没有输入开始日期，默认为当前日期
                if (!begindate.isChecked) {
                    // 设置开始日期为当前日期
                    begindate.isChecked = true
                    begindate.text =
                        java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())
                }
                val timePickerDialog = android.app.TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        val time = "$hourOfDay:$minute"
                        begintime.text = time
                    },
                    0,
                    0,
                    true
                )
                timePickerDialog.show()
            } else {
                // 取消修改，没有时间
                begintime.text = ""
            }
        }

        // 设置结束时间按钮
        val endtime = findViewById<SwitchCompat>(R.id.endtime)
        endtime.setOnClickListener {
            if (endtime.isChecked) {
                // 如果用户没有输入结束日期，提示请先输入结束日期
                if (!enddate.isChecked) {
                    // 提示用户先输入结束日期
                    val toast = Toast.makeText(
                        this,
                        "请先输入结束日期",
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                    // 修改switch按钮的状态为关闭
                    endtime.isChecked = false
                    return@setOnClickListener // 结束该方法
                } else {
                    val timePickerDialog = android.app.TimePickerDialog(
                        this,
                        { _, hourOfDay, minute ->
                            val time = "$hourOfDay:$minute"
                            // 如果开始日期和结束日期一样，结束时间不能小于开始时间
                            if (begindate.text.toString() == enddate.text.toString()) {
                                val format = java.text.SimpleDateFormat("HH:mm") // 设置日期格式
                                val beginTime =
                                    format.parse(begintime.text.toString()) // 将用户选择的日期转换为Date类型
                                val endTime = format.parse(time) // 将用户选择的日期转换为Date类型
                                if (endTime != null) {
                                    if (endTime.before(beginTime)) {
                                        val toast = Toast.makeText(
                                            this,
                                            "结束时间不能小于开始时间",
                                            Toast.LENGTH_SHORT
                                        )
                                        toast.show()
                                        // 修改switch按钮的状态为关闭
                                        endtime.isChecked = false
                                        endtime.text = null
                                    } else {
                                        endtime.text = time
                                    }
                                }
                            } else endtime.text = time
                        },
                        0,
                        0,
                        true
                    )
                    timePickerDialog.show()
                }
            } else {
                // 取消修改，没有时间
                endtime.text = ""
            }
        }


        // 设置优先级
        val priority = findViewById<Spinner>(R.id.priority_spinner)
        // 创建一个ArrayAdapter，用于将数组和spinner绑定
        val adapter_spinner = ArrayAdapter.createFromResource(
            this,
            R.array.planets_array,
            android.R.layout.simple_spinner_item
        )
        // 设置下拉列表的风格
        adapter_spinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        priority.adapter = adapter_spinner
        // 设置优先级的默认值
        priority.setSelection(detail?.priority ?: 3)
        // 设置优先级的监听器，用于获取用户选择的优先级
        priority.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, // 选择的父视图
                view: View?, position: Int, id: Long // 选择的位置
            ) {
                parent as Spinner
                detail?.priority = position // 保存优先级
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }


        // 设置添加图像按钮
        val addimage = findViewById<Button>(R.id.addimage)
        addimage.setOnClickListener {
            // Create an AlertDialog.Builder instance
            val builder = AlertDialog.Builder(this)
            // Set the options for the dialog
            builder.setItems(arrayOf("拍照", "选择图片")) { _, which ->
                when (which) {
                    0 -> {
                        // 检查是否有相机权限
                        if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.CAMERA
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // 如果没有相机权限，请求权限
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.CAMERA),
                                1
                            )
                        } else {
                            // 创建File对象，用于存储拍照后的图片
                            val filename = "userimage${System.currentTimeMillis()}.jpg"
                            photoFile = File(externalCacheDir, filename)
                            if (photoFile.exists()) {
                                photoFile.delete()
                            }
                            photoFile.createNewFile()
                            imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                FileProvider.getUriForFile(
                                    this,
                                    "com.example.hw2.fileprovider",
                                    photoFile
                                );
                            } else {
                                Uri.fromFile(photoFile);
                            }
                            // 启动相机程序
                            val intent = Intent("android.media.action.IMAGE_CAPTURE")
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                            startActivityForResult(intent, 1)
                        }
                    }

                    1 -> {
                        val pickPhotoIntent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(pickPhotoIntent, 2)
                    }
                }
            }
            // Create and show the dialog
            builder.create().show()
        }

        // 设置添加文件按钮
        val addfile = findViewById<Button>(R.id.addfile)
        addfile.setOnClickListener {
            val intent = Intent() // 创建一个意图
            intent.action = Intent.ACTION_GET_CONTENT // 设置意图的动作为获取内容
            intent.type = "*/*"// 设置意图的类型为所有文件
            startActivityForResult(intent, 3)// 开启意图
        }

        val userimage = findViewById<ImageView>(R.id.userimage)
        userimage.setOnClickListener {
            // 点击图片放大，显示原图在popupwindow中
            val popupWindow = PopupWindow(this)
            val imageView = ImageView(this)
            // 获取图片文件,通过文件名获取
            val file = detail?.imageUrl?.let { File(filesDir, it) }
            if (file != null && file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                imageView.setImageBitmap(bitmap)
                popupWindow.contentView = imageView
                popupWindow.width = ViewGroup.LayoutParams.MATCH_PARENT
                popupWindow.height = ViewGroup.LayoutParams.MATCH_PARENT
                popupWindow.isFocusable = true
                popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                popupWindow.showAtLocation(userimage, Gravity.CENTER, 0, 0)
                imageView.setOnClickListener {
                    popupWindow.dismiss()
                }
            }

        }


        // 设置保存按钮
        val save = findViewById<Button>(R.id.done_btn)
        val format = java.text.SimpleDateFormat("yyyy-MM-dd") // 设置日期格式
        val format1 = java.text.SimpleDateFormat("HH:mm") // 设置时间格式
        val actions = intent.getStringExtra("actions")
        val originalId = intent.getIntExtra("originalId", 0) // 获取原来的id
        save.setOnClickListener {
            // 获取用户输入的数据
            val titles = detailtitle.text.toString()
            val begindates = if (begindate.text.toString()
                    .isNotEmpty()
            ) format.parse(begindate.text.toString())?.time else null
            // 如果用户没有输入结束日期，默认为空
            // 如果用户输入了结束日期，将其转换为毫秒数
            val enddates = if (enddate.text.toString()
                    .isNotEmpty()
            ) format.parse(enddate.text.toString())?.time else null
            val begintimes = if (begintime.text.toString()
                    .isNotEmpty()
            ) format1.parse(begintime.text.toString())?.time else null
            val endtimes = if (endtime.text.toString()
                    .isNotEmpty()
            ) format1.parse(endtime.text.toString())?.time else null
            val isChecked =
                if (actions == "adapter") intent.getBooleanExtra("isChecked", false) else false
            val descri = binding.reminderBeizhu.text.toString()
            val imageUrl = detail?.imageUrl
            val fileUrl = detail?.fileUrl
            val prioritys = priority.selectedItemPosition
            // 输入的title不能重复
            val DetailList = queryDataFromDatabase(this)
            Log.d("originalId", originalId.toString())
            for (i in DetailList) {
                // 如果除去原来的title，其他title和用户输入的title相同，提示错误
                if (i.title == titles && i.id != originalId) {
                    Log.d("id", i.id.toString())
                    Log.d("originalId", originalId.toString())
                    val toast = Toast.makeText(
                        this,
                        "标题不能重复",
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                    return@setOnClickListener
                }
            }


            val detail = detail(
                titles,
                descri,
                isChecked,
                begindates,
                enddates,
                begintimes,
                endtimes,
                prioritys,
                null,
                imageUrl,
                fileUrl
            )
            // 更新数据库
            if (actions == "main") {

                if (detailtitle.text.toString().isNotEmpty()) {
                    addDataToDatabase(
                        this, detail
                    )
                }
            } else if (actions == "adapter") {
                updateDatabase(this, detail, originalId)
            }
            // 将数据传递给recyclerview的adapter
            val resultintent = Intent()
            resultintent.putExtra("detail", detail)
            setResult(RESULT_OK, intent)
            finish()
        }

    }


    //显示图片
    @SuppressLint("Recycle")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 获取拍照后的图片
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val userimage = findViewById<ImageView>(R.id.userimage)
            // 从文件中读取图片
            userimage.setImageURI(imageUri)
            userimage.visibility = View.VISIBLE

            // 随机生成文件名
            val filename = "userimage${System.currentTimeMillis()}.jpg"
            val inputStream = contentResolver.openInputStream(imageUri) //获取图片的输入流
            val file = File(filesDir, filename) //创建文件
            val outputStream = FileOutputStream(file) //创建输出流
            inputStream?.copyTo(outputStream) //将输入流复制到输出流
            inputStream?.close() //关闭输入流
            outputStream.close() //关闭输出流
            // 将文件名保存
            val detail = intent.getParcelableExtra<detail>("detail")
            if (detail != null) {
                detail.imageUrl = filename
                // 更新数据库
                val id = queryIdFromDatabase(this, detail)
                updateDatabase(this, detail, id)
            }
        }
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            val userimage = findViewById<ImageView>(R.id.userimage)
            userimage.setImageURI(data.data)
            userimage.visibility = View.VISIBLE
            // 随机生成文件名
            val filename = "userimage${System.currentTimeMillis()}.jpg"
            val inputStream = contentResolver.openInputStream(data.data!!) //获取图片的输入流
            val file = File(filesDir, filename) //创建文件
            val outputStream = FileOutputStream(file) //创建输出流
            inputStream?.copyTo(outputStream) //将输入流复制到输出流
            inputStream?.close() //关闭输入流
            outputStream.close() //关闭输出流
            // 将文件名保存
            val detail = intent.getParcelableExtra<detail>("detail")
            if (detail != null) {
                detail.imageUrl = filename
                // 更新数据库
                val id = queryIdFromDatabase(this, detail)
                updateDatabase(this, detail, id)
            }
        }
        if (requestCode == 3 && resultCode == RESULT_OK && data != null) {
            val userfile = findViewById<TextView>(R.id.filename)
            val uri = data.data //获取文件的Uri，uri是指向本地文件的指针
            val cursor = contentResolver.query(uri!!, null, null, null, null) //获取文件的内容解析器
            if (cursor != null && cursor.moveToFirst()) {//判断是否为空
                val columnindex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)//获取文件名
                if (columnindex != -1) {
                    val filename = cursor.getString(columnindex)
                    userfile.text = filename.toString()
                    userfile.textSize = 20F //设置字体大小
                    userfile.paint.isUnderlineText = true //设置下划线
                    // 设置字体颜色为蓝色
                    userfile.setTextColor(resources.getColor(R.color.darkblue))

                    // 获取附件的绝对路径
                    val filename2 = filename.toString() + System.currentTimeMillis()
                    val inputStream = contentResolver.openInputStream(data.data!!) //获取图片的输入流
                    val file = File(filesDir, filename2) //创建文件
                    val outputStream = FileOutputStream(file) //创建输出流
                    inputStream?.copyTo(outputStream) //将输入流复制到输出流
                    inputStream?.close() //关闭输入流
                    outputStream.close() //关闭输出流

                    // 将文件名保存
                    val detail = intent.getParcelableExtra<detail>("detail")
                    if (detail != null) {
                        detail.fileUrl = filename2
                        // 更新数据库
                        val id = queryIdFromDatabase(this, detail)
                        updateDatabase(this, detail, id)
                    }
                    Linkify.addLinks(userfile, Linkify.WEB_URLS)
                    userfile.setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(uri, contentResolver.getType(uri))
                        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        startActivity(intent)
                    }
                }
            }
            userfile.visibility = View.VISIBLE
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                // 如果请求被取消，那么结果数组将为空
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 权限被用户授予，启动相机
                    val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePhotoIntent.resolveActivity(packageManager) != null) {
                        startActivityForResult(takePhotoIntent, 1)
                    }
                } else {
                    // 权限被用户拒绝，显示一个提示消息
                    Toast.makeText(
                        this,
                        "Camera permission is required to record video",
                        Toast.LENGTH_SHORT
                    ).show()

                    // 再次请求权限
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA),
                        1
                    )
                }
                return
            }
            // 其他 'case' 行来检查其他权限请求
            else -> {
                // 忽略所有其他请求
            }
        }
    }


}