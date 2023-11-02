package com.jfo1g21.android.projectmanagement
import android.content.Context
import android.graphics.Color
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "projectListFragment"

class ProjectListFragment: Fragment() {

    interface Callbacks {
        fun onProjectSelected(currentUserId: String, PMUserId: String,projectID: String)
        fun createNewProjectSelected()
        fun signOut()
        fun viewProfile()
    }

    private var callbacks : Callbacks? = null

    private lateinit var projectRecyclerView: RecyclerView
    private var adapter: ProjectAdapter? = null

    private val projectListViewModel: ProjectsListViewModel by lazy {
        ViewModelProviders.of(this).get(ProjectsListViewModel::class.java)
    }

    private lateinit var auth: FirebaseAuth

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_project_list, container, false)
        projectRecyclerView =
            view.findViewById(R.id.project_recycler_view) as RecyclerView
        projectRecyclerView.layoutManager = LinearLayoutManager(context)

        DatabaseHandler.CurrentProjectListFragment = this
        DatabaseHandler.init(context)
        DatabaseHandler.FetchProjects(null,null,this)

        return view
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_project_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_new_project -> {
                callbacks?.createNewProjectSelected()
            }
            R.id.filter_status -> {
                var ProjectList = DatabaseHandler.GetProjects()

                val sortedByStatusProjects =  ProjectList?.documents?.sortedBy {
                    it.get("ProjectStatus") as String
                }
                adapter = ProjectAdapter(sortedByStatusProjects)
                projectRecyclerView.adapter = adapter
            }
            R.id.filter_deadline -> {
                var ProjectList = DatabaseHandler.GetProjects()

                val sortedByDeadlineProjects = ProjectList?.documents?.sortedBy {
                    it.get("ProjectDeadline") as Timestamp
                }
                adapter = ProjectAdapter(sortedByDeadlineProjects)
                projectRecyclerView.adapter = adapter
            }
            R.id.view_profile -> {
            //TODO call viewprofile
                callbacks?.viewProfile()
            }
            R.id.logout_application -> {
                callbacks?.signOut()
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, LoginFragment())
                    .commit()
                //TODO go to login page
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /* TODO: private fun logout*/

    public fun updateUI() {
        var ProjectList = DatabaseHandler.GetProjects()
        adapter = ProjectAdapter(ProjectList?.documents)
        projectRecyclerView.adapter = adapter
    }

    companion object {
        fun newInstance(): ProjectListFragment {
            return ProjectListFragment()
        }
    }

    private inner class ProjectHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var project: DocumentSnapshot

        private val titleTextView: TextView = itemView.findViewById(R.id.project_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.project_date)
        private val completedProjectImageView: ImageView = itemView.findViewById(R.id.project_complete)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(project: DocumentSnapshot) {
            this.project = project

            titleTextView.text = this.project.get("ProjectName").toString()
            val projectDeadline = this.project.get("ProjectDeadline") as Timestamp
            val date = projectDeadline.toDate()
            dateTextView.text = date.toString()
            val projectStatus = this.project.get("ProjectStatus").toString()

            if (projectStatus == "Completed") {
                completedProjectImageView.setImageResource(R.drawable.complete)
                itemView.setBackgroundColor(Color.rgb(200,255,200))
            } else if (projectStatus == "Ongoing") {
                completedProjectImageView.setImageResource(R.drawable.ongoing)
                itemView.setBackgroundColor(Color.rgb(255,255,200))
            }
        }

        override fun onClick(v: View) {
            auth = Firebase.auth
            var user = auth.currentUser?.uid.toString()
            var pmid = this.project.get("ProjectManager").toString()
            var projectID = this.project.id
            callbacks?.onProjectSelected(user, pmid,projectID)
        }
    }

    private inner class ProjectAdapter(var projects: List<DocumentSnapshot>?): RecyclerView.Adapter<ProjectHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectHolder {
            val view = layoutInflater.inflate(R.layout.list_item_project, parent, false)
            return ProjectHolder(view)
        }

        override fun onBindViewHolder(holder: ProjectHolder, position: Int) {
            val project = projects?.get(position)
            if (project != null) {
                holder.bind(project)
            }
        }

        override fun getItemCount(): Int {
        var Size = projects?.size
            if (Size == null){
                Size = 0
            }
            return Size
        }
    }
}