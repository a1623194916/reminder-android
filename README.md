计划管理APP
功能
1. list![输入图片说明]![image](https://github.com/a1623194916/reminder-android/assets/43876825/bbe7258d-d92b-4d7f-a518-8e75b48b5506)

2. Pull-up query（数据保存在本地的sqlite数据库）![image](https://github.com/a1623194916/reminder-android/assets/43876825/77808964-9b09-4448-83c7-01bc356124ac)

3. order![image](https://github.com/a1623194916/reminder-android/assets/43876825/22a7e9b6-adb9-41a0-a7bb-5e5efc7241c5)

4. edit（开始时间、结束时间、文字描述、图片、附件、重要程度、完成情况）![image](https://github.com/a1623194916/reminder-android/assets/43876825/b37edf8d-8a4f-49c9-b594-c63fd5a57a85)

5. lefe slide to delete![image](https://github.com/a1623194916/reminder-android/assets/43876825/8098aa7c-3529-4d69-8bdb-065cbd88145b)

6. remind![image](https://github.com/a1623194916/reminder-android/assets/43876825/fc1787b7-47a9-4b1c-a31d-41585eb4e7d7)


7. Show/hide completed![image](https://github.com/a1623194916/reminder-android/assets/43876825/7eca151a-fee8-4655-8495-07c5385b3e26)


List function and database implementation
Database implementation
1. Define data classes
The @Parcelize annotation is used, which means that it can be automatically converted to Parcelable so that it can be passed in the Intent
imageurl and fileurl are used to save the address of images and attachments in the phone's file system
@Parcelize
data class detail(
var title: String, // title
val description: String?  =null, // description
var isChecked: Boolean = false, // Whether it is finished
var begindate: Long?  = null,
var enddate: Long? =null,
var begintime:Long? =null,
var endtime:Long? =null,
var priority:Int? =3, // No priority by default
var id:Int? =null,
var imageUrl:String? =null,
var fileUrl:String? =null,
): Parcelable
2. Create a database
Database to achieve query, update, add, delete and other functions
First, they all need a Context object to create a DatabaseHelper, and then a DatabaseHelper to fetch the SQLiteDatabase object. They all then query, insert, update, or delete the database as needed.
The query function is realized through the cursor, and the addition and update function is realized by adding/overwriting the values of contentvalues

List function
1. Use recyclerview for list display. Define two types first. The first is listitem for normal list display and the second is bottom footview for adding new items. The bottom of the data is defined as footview.
If the view type is TYPE_ITEM, then create a new view from the listitem.xml layout file; If the view type is TYPE_FOOTER, then create a new view from the footview.xml layout file and pass this view to the FooterViewHolder constructor, creating an instance of FooterViewHolder.
override fun getItemViewType(position: Int): Int {
return if (position == DetailList.size) {
TYPE_FOOTER
} else {
TYPE_ITEM
}
}

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
return if (viewType == TYPE_ITEM) {
val view = LayoutInflater.from(parent.context).inflate(R.layout.listitem, parent, false)
ViewHolder(view)
} else {
val view = LayoutInflater.from(parent.context).inflate(R.layout.footview, parent, false)
FooterViewHolder(view)
}
2. Implement the logic in onbindviewholder
3. Bind the mainactivity to recyclerview
a. First obtain all the data of the database and define the list as a linear layout
b. Register a callback that handles the result returned from the Activity. If the result code returned is RESULT_OK, updateRecyclerView by calling the updateRecyclerView() function.
c. Create a new Recycleadapter and pass it the queried data, the resulting processor, and a function reference to set the visibility of the FloatingActionButton.
private fun setupRecyclerView() {
val detailList = queryDataFromDatabase(this)
val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
recyclerView.layoutManager = layoutManager

/ / registerForActivityResult () method of the first parameter is a ActivityResultContract object, the object is responsible for creating the Intent,
// When the Activity ends, the Intent is passed to the onActivityResult() method.
resultLauncher =
registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
if (it.resultCode == RESULT_OK) {
updateRecyclerView()
}
}
// Recycleadapter is a self-written class that binds data to views. adapter is an instance of Recycleadapter
val adapter =
Recycleadapter(detailList, resultLauncher, ::setFloatingActionButtonVisibility)
recyclerView.adapter = adapter

// ItemTouchHelper is a helper class that implements the drag-and-drop and slide-down capabilities of RecyclerView
val itemTouchHelperCallback =
object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
The // onMove() method is used to implement drag and drop, which is not needed here, so it returns false
override fun onMove(
recyclerView: RecyclerView,
viewHolder: RecyclerView.ViewHolder,
target: RecyclerView.ViewHolder
): Boolean {
return false
}

// onSwiped() method is used to implement the sliding delete function
override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
val position = viewHolder.adapterPosition
detailList.removeAt(position)
recyclerView.adapter!! .notifyItemRemoved(position)
// Delete data from the database
deleteDataFromDatabase(this@MainActivity, position)
}
}
// Bind ItemTouchHelper and RecyclerView
val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
itemTouchHelper.attachToRecyclerView(recyclerView)
}
Query function
1. Use NestedScrollView to wrap the layout of the main interface to achieve sliding-up search
2. Set the scrolling listening for NestedScrollView. When the scrollY position is less than or equal to 0, the searchView is visible and the checkmore button is not. When the scroll position is greater than 100, the searchView is not visible and the checkmore button is visible. This changes the visibility of the two controls as you scroll.
3. Set the search text listener for the searchView. When the query text changes, the updateRecyclerView(newText) function is called to updateRecyclerView. This enables the search function to filter the data in RecyclerView based on the text entered by the user in the searchView.
4. updateRecyclerView calls setData(filteredList) to set the filtered data to the adapter, and sets the value of the showFooter property to whether filter is empty or an empty string. The notifyDataSetChanged() method is then called to notify RecyclerView that the data has changed
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
override fun onQueryTextSubmit(query: String?) : Boolean {
