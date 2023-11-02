package com.jfo1g21.android.projectmanagement

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.ktx.Firebase
import com.google.firestore.v1.FirestoreGrpc
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.reflect.typeOf

private const val TAG = "taskListFragment"

class TaskListFragment : Fragment() {

    private lateinit var taskRecyclerView: RecyclerView

    private var adapter: TaskListFragment.TaskAdapter? = null

    public var currentProjectID: String? = null

    private lateinit var auth: FirebaseAuth
    private val taskListViewModel: TaskListViewModel by lazy {
        ViewModelProviders.of(this).get(TaskListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        //Log.d(TAG, "Total crimes: ${crimeListViewModel.crimes.size}")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_search_task_list, menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView

        searchView.apply {

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter?.filter?.filter(newText)
                    return false
                }

            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = Firebase.auth
        val view = inflater.inflate(R.layout.fragment_task_list, container, false)
        taskRecyclerView =
            view.findViewById(R.id.task_recycler_view) as RecyclerView
        taskRecyclerView.layoutManager = LinearLayoutManager(context)

        updateUI()

        return view
    }

    private fun updateUI() {
        val taskArray = ArrayList<Task>()
        val tasks = getTasksForUser() as ArrayList<Any?>
        for (i in tasks) {
            i as HashMap<*, *>
            val task = Task()
            val id = i["TaskUser"]

            //TODO: project
            val nameID = DatabaseHandler.getIndividualUser(id.toString())
            Log.d("name", nameID.toString())
            task.title = i["TaskName"].toString()
            task.taskStatus = i["TaskStatus"].toString()
            task.assignedTeamMember = nameID?.get("name").toString()
            taskArray += task
        }


        adapter = TaskAdapter(taskArray)
        taskRecyclerView.adapter = adapter
    }

    private inner class TaskHolder(view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var task: Task
        private val titleTextView: TextView = itemView.findViewById(R.id.task_title)
        private val teamMemberTextView: TextView = itemView.findViewById(R.id.task_member)
        private val solvedTaskImageView: ImageView = itemView.findViewById(R.id.task_solved)

        fun bind(task: Task) {
            this.task = task
            titleTextView.text = this.task.title
            teamMemberTextView.text = this.task.assignedTeamMember
            if (this.task.taskStatus == "Completed") {
                solvedTaskImageView.setImageResource(R.drawable.completed)
                //itemView.setBackgroundColor(Color.WHITE)
            } else {
                solvedTaskImageView.setImageResource(R.drawable.assigned)
                //itemView.setBackgroundColor(Color.YELLOW)
            }
            /*           solvedTaskImageView.visibility = if (task.taskStatus) {
                           View.VISIBLE
                       } else {
                           View.GONE
                       }*/
        }
    }

    private fun getTasksForUser(): Any? {


        val projects = DatabaseHandler.GetProjects()
        var CurrentProject: QueryDocumentSnapshot? = null
        for (Project in projects!!) {
            if (Project.id == currentProjectID)
                CurrentProject = Project
        }
        return CurrentProject?.get("ProjectTasks")
    }

    private inner class TaskAdapter(var tasks: ArrayList<Task>) :
        RecyclerView.Adapter<TaskHolder>(),
        Filterable {

        var taskFilterList = ArrayList<Task>()

        init {
            taskFilterList = tasks
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
            val view = layoutInflater.inflate(R.layout.list_item_task, parent, false)
            return TaskHolder(view)
        }

        override fun onBindViewHolder(holder: TaskHolder, position: Int) {
            val task = taskFilterList[position]
            /*holder.apply {
                titleTextView.text = project.title
                dateTextView.text = project.projectDeadline.toString()
            }*/

            holder.bind(task)
        }

        override fun getItemCount() = taskFilterList.size

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val charSearch = constraint.toString()
                    if (charSearch.isEmpty()) {
                        //countryFilterList = countryList
                        taskFilterList = tasks
                    } else {
                        var resultList = ArrayList<Task>()
                        for (task in tasks) {
                            if (task.assignedTeamMember.lowercase(Locale.ROOT)
                                    .contains(charSearch.lowercase(Locale.ROOT))
                            ) {
                                resultList.add(task)
                            }
                        }
                        taskFilterList = resultList
                    }
                    val filterResults = FilterResults()
                    filterResults.values = taskFilterList
                    return filterResults
                }

                @Suppress("UNCHECKED_CAST")
                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

                    taskFilterList = results?.values as ArrayList<Task>
                    //Log.d(TAG, "Total crimes: ${results?.values}")
                    notifyDataSetChanged()

                    //
                }
            }
        }
    }

    companion object {
        fun newInstance(): TaskListFragment {
            return TaskListFragment()
        }
    }
}