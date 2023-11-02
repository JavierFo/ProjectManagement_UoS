package com.jfo1g21.android.projectmanagement
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ProjectFragment : Fragment() {

    private var tempString: String = "Temporal"

    private lateinit var project: Project
    private lateinit var projectTitleTextView: TextView

    private lateinit var statusTextView: TextView

    private lateinit var managerTextView: TextView

    private lateinit var member1TextView: TextView
    private lateinit var member2TextView: TextView
    private lateinit var member3TextView: TextView

    private lateinit var deadlineTextView: TextView

    private lateinit var task1Checkbox: CheckBox
    private lateinit var task2Checkbox: CheckBox
    private lateinit var task3Checkbox: CheckBox

    private lateinit var taskDeadline1TextView: TextView
    private lateinit var taskDeadline2TextView: TextView
    private lateinit var taskDeadline3TextView: TextView

    private lateinit var auth: FirebaseAuth

    val databaseHandler = DatabaseHandler

    public var currentProjectID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = Project()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_project, container, false)

        projectTitleTextView = view.findViewById(R.id.project_title) as TextView
        statusTextView = view.findViewById(R.id.project_status) as TextView
        managerTextView = view.findViewById(R.id.project_manager) as TextView
        member1TextView = view.findViewById(R.id.project_team_members1) as TextView
        member2TextView = view.findViewById(R.id.project_team_members2) as TextView
        member3TextView = view.findViewById(R.id.project_team_members3) as TextView
        deadlineTextView = view.findViewById(R.id.project_deadline) as TextView
        task1Checkbox = view.findViewById(R.id.project_task1) as CheckBox
        task2Checkbox = view.findViewById(R.id.project_task2) as CheckBox
        task3Checkbox = view.findViewById(R.id.project_task3) as CheckBox
        taskDeadline1TextView = view.findViewById(R.id.task1_deadline_textView) as TextView
        taskDeadline2TextView = view.findViewById(R.id.task2_deadline_textView) as TextView
        taskDeadline3TextView = view.findViewById(R.id.task3_deadline_textView) as TextView

        auth = Firebase.auth
        return view
    }

    override fun onResume() {
        super.onResume()
        val projects = databaseHandler.GetProjects()
        var CurrentProject: QueryDocumentSnapshot? = null
        for (Project in projects!!){
            if (Project.id == currentProjectID)
                CurrentProject = Project
        }
        if (CurrentProject != null) {
            statusTextView.text = CurrentProject.get("ProjectStatus").toString()
        } else {
            statusTextView.text = "STATUS"
        }
    }

    override fun onStart() {
        super.onStart()

//        var o_ : String = ""
        val projects = databaseHandler.GetProjects()
        var CurrentProject: QueryDocumentSnapshot? = null
        for (Project in projects!!){
            if (Project.id == currentProjectID)
                CurrentProject = Project
        }

        val projectsTatus = CurrentProject?.get("ProjectStatus").toString()
        if (projectsTatus == "Completed") {
            task1Checkbox.isEnabled = false
            task2Checkbox.isEnabled = false
            task3Checkbox.isEnabled = false
        }

        Log.d("TAG", CurrentProject.toString())

        if (CurrentProject != null) {
            projectTitleTextView.text = CurrentProject.get("ProjectName").toString()
        } else {
            projectTitleTextView.text = "New Project"
        }
        if (CurrentProject != null) {
            statusTextView.text = CurrentProject.get("ProjectStatus").toString()
        } else {
            statusTextView.text = "STATUS"
        }

        if (CurrentProject != null) {
            var managerID = CurrentProject.get("ProjectManager").toString()
            managerTextView.text =  DatabaseHandler.getIndividualUser(managerID)?.get("name").toString()
        } else {
            managerTextView.text = "jfo1g21@soton.ac.uk"
        }

        val users = CurrentProject?.get("ProjectUsers") as ArrayList<String>
//        val userNameArray = ArrayList<String>()
//        for (user in users){
//            //user as HashMap<*,*>
//            //val e = user["TaskName"].toString()
//            userNameArray.add(user.toString())
//        }

        if (CurrentProject != null) {
            member1TextView.text = DatabaseHandler.getIndividualUser(users[0])?.get("name").toString()
        } else {
            member1TextView.text = "Paul M"
        }

        if (CurrentProject != null) {
            member2TextView.text = DatabaseHandler.getIndividualUser(users[1])?.get("name").toString()
        }else {
            member1TextView.text = "John l"
        }

        if (CurrentProject != null) {
            member3TextView.text = DatabaseHandler.getIndividualUser(users[2])?.get("name").toString()
        }else {
            member1TextView.text = "George H"
        }

        val projectdeadline = CurrentProject?.get("ProjectDeadline") as Timestamp
        val date = projectdeadline.toDate()
        deadlineTextView.text = date.toString()

        val tasks = CurrentProject.get("ProjectTasks") as ArrayList<Any?>
        val taskNameArray = ArrayList<Array<Any?>>()

        for (task in tasks){
            task as HashMap<*,*>
            val e = task["TaskName"].toString()
            val id= task["TaskUser"].toString()
            var complete = false
            if (task["TaskStatus"] == "Completed"){
                complete = true
            }
            taskNameArray.add(arrayOf(e, id, complete))
        }


        //TODO DISBALE

        if (CurrentProject.get("ProjectStatus").toString() != "Completed") {
            task1Checkbox.isEnabled = taskNameArray[0][1] == auth.currentUser?.uid
            task2Checkbox.isEnabled = taskNameArray[1][1] == auth.currentUser?.uid
            task3Checkbox.isEnabled = taskNameArray[2][1] == auth.currentUser?.uid
        }
        task1Checkbox.isChecked = taskNameArray[0][2] == true
        task2Checkbox.isChecked = taskNameArray[1][2] == true
        task3Checkbox.isChecked = taskNameArray[2][2] == true
        task1Checkbox.text = taskNameArray[0][0].toString()
        task2Checkbox.text = taskNameArray[1][0].toString()
        task3Checkbox.text = taskNameArray[2][0].toString()

        task1Checkbox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                Log.d("Checking", isChecked.toString())
                val currentProjectIDString = currentProjectID!!
                val DH = DatabaseHandler
                DH.UpdateTask(currentProjectIDString, 0,isChecked)
                //project.projectStatus = isChecked
                //Toast.makeText(context, "Item pressed!", Toast.LENGTH_SHORT).show()
            }
        }

        task2Checkbox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                Log.d("Checking", isChecked.toString())
                val currentProjectIDString = currentProjectID!!
                val DH = DatabaseHandler
                DH.UpdateTask(currentProjectIDString, 1,isChecked)
                //project.projectStatus = isChecked
                //Toast.makeText(context, "Item pressed!", Toast.LENGTH_SHORT).show()
            }
        }

        task3Checkbox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                Log.d("Checking", isChecked.toString())
                val currentProjectIDString = currentProjectID!!
                val DH = DatabaseHandler
                DH.UpdateTask(currentProjectIDString, 2,isChecked)
                //project.projectStatus = isChecked
                //Toast.makeText(context, "Item pressed!", Toast.LENGTH_SHORT).show()
            }
        }

        val tasksDeadline = CurrentProject.get("ProjectTasks") as ArrayList<Any?>
        val taskDeadlineArray = ArrayList<String>()
        for (task in tasksDeadline){
            task as HashMap<*,*>
            val e = task["TaskDeadline"] as Timestamp
            val date = e.toDate().toString()
            taskDeadlineArray.add(date)
        }

        taskDeadline1TextView.text = taskDeadlineArray[0]
        taskDeadline1TextView.text = taskDeadlineArray[1]
        taskDeadline1TextView.text = taskDeadlineArray[2]
    }
}