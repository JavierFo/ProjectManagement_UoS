package com.jfo1g21.android.projectmanagement

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


object DatabaseHandler {
    private val base = FirebaseFirestore.getInstance()

    private lateinit var auth: FirebaseAuth

    private var ProjectCache:QuerySnapshot? = null
    private var UserCache:QuerySnapshot? = null
    public var CurrentProjectListFragment:ProjectListFragment? = null
    var InitCalled = false

    public fun init(context: Context?){
        if (InitCalled){
            return
        }
        FetchUsers()
        FetchProjects(null,null,null)
        Log.d("Test","DatabaseHandler Init Called")
        InitCalled = true
        val test = base.collection("Users").addSnapshotListener(EventListener { value, error->
            if (error !=null||value==null){
                Log.d("Test", "Strange update occured.")
                return@EventListener
            }

            if (value.isEmpty){

            } else{
                Log.d("Test","A USER HAS CHANGED")
                this.UserCache = value
                //Rerender UI here
                for (dc in value!!.documentChanges){
                    when (dc.type){
                        DocumentChange.Type.ADDED -> Log.d("Test", "New User: ${dc.document.data}")
                        DocumentChange.Type.MODIFIED -> Log.d("Test", "Modified User: ${dc.document.data}")
                        DocumentChange.Type.REMOVED -> Log.d("Test", "Removed User: ${dc.document.data}")
                    }
                }
            }

        })

        val test2 = base.collection("Projects").addSnapshotListener(EventListener { value, error->

            if (error !=null||value==null){
                Log.d("Test", "Strange update occured.")
                return@EventListener
            }

            if (value.isEmpty){

            } else{
                Log.d("Test","A PROJECT HAS CHANGED")
                this.ProjectCache = value
                //Rerender UI here
                auth = Firebase.auth
                CurrentProjectListFragment?.updateUI()
                for (dc in value!!.documentChanges){//If project is a new project, notify team members of their participation, if project is modified, check if completed and if so notify manager.
                    when (dc.type){
                        DocumentChange.Type.ADDED -> {
                            Log.d("Test", "New Project: ${dc.document.data}")
                            var AnyTasksAssigned = false

                            var TaskArray = dc.document.get("ProjectTasks") as ArrayList<HashMap<String,Any>>
                            for (taskIndex in TaskArray.indices){
                                if (TaskArray[taskIndex].get("TaskUser").toString()== auth.currentUser?.uid.toString() && value!!.documentChanges.size < 2){//Exclude cases where entire project list is considered new
                                    AnyTasksAssigned = true
                                }
                            }
                            if (AnyTasksAssigned){
                                if (context != null){
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        val name = "ProjectApp"
                                        val descriptionText = "Stuff"
                                        val importance = NotificationManager.IMPORTANCE_DEFAULT
                                        val channel =
                                            NotificationChannel("Test", name, importance).apply {
                                                description = descriptionText
                                            }
                                        // Register the channel with the system
                                        val nm =
                                            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                        nm.createNotificationChannel(channel)
                                        var builder =
                                            NotificationCompat.Builder(context, channel.id)
                                                .setContentTitle("You have had a new task assigned")
                                                .setContentText("${dc.document.get("ProjectName")} has a task assigned to you")
                                                .setAutoCancel(true)
                                                .setSmallIcon(androidx.core.R.drawable.notification_template_icon_low_bg)
                                        with(NotificationManagerCompat.from(context)) {
                                            // notificationId is a unique int for each notification that you must define
                                            notify(2, builder.build())
                                        }


                                    }
                                }
                            }
                        }
                        DocumentChange.Type.MODIFIED -> {
                            Log.d("Test", "Modified Project: ${dc.document.data}")
                            auth = Firebase.auth
                            if (dc.document.get("ProjectManager") == auth.currentUser?.uid.toString() && dc.document.get("ProjectStatus") == "Completed"){
                                Log.d("Test", "One of my projects has been updated to completed.")
                                if (context != null){
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        val name = "ProjectApp"
                                        val descriptionText = "Stuff"
                                        val importance = NotificationManager.IMPORTANCE_DEFAULT
                                        val channel = NotificationChannel("Test", name, importance).apply {
                                            description = descriptionText
                                        }
                                        // Register the channel with the system
                                        val nm =
                                            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                            nm.createNotificationChannel(channel)
                                        var builder = NotificationCompat.Builder(context,channel.id)
                                            .setContentTitle("Project Completed")
                                            .setContentText("${dc.document.get("ProjectName")} is complete")
                                            .setAutoCancel(true).setSmallIcon(androidx.core.R.drawable.notification_template_icon_low_bg)
                                        with(NotificationManagerCompat.from(context)) {
                                            // notificationId is a unique int for each notification that you must define
                                            notify(1, builder.build())
                                    }




                                    }
                                }

                            }
                        }
                        DocumentChange.Type.REMOVED -> Log.d("Test", "Removed Project: ${dc.document.data}")
                    }
                }
            }

        })
    }

    public fun createProfile(Name: String, Email: String, Image: String, Skills: List<String>, Frag: RegisterFragment) {
        Log.d("Test", "Calling Create Profile")
        auth = Firebase.auth
        val user = auth.currentUser
        //Log.d("test 2",user?.uid.toString())
        val doc = base.collection("Users").document(user?.uid.toString())
        val UserData = hashMapOf(
            "name" to Name,
            "email" to Email,
            "image" to Image,
            "skills" to Skills

        )
        doc.set(UserData).addOnSuccessListener {
            Log.d("Test", "FirestoreSuccess")
           Frag.updateUI(user)
        }.addOnFailureListener { exception ->
            Log.d("Test", "FirestoreFailure")
        }

    }


    public fun updateProfile(Name: String, Email: String, Image: String, Skills: List<String>) {
        Log.d("Test", "Calling Update Profile")
        auth = Firebase.auth
        val user = auth.currentUser
        //Log.d("test 2",user?.uid.toString())
        val doc = base.collection("Users").document(user?.uid.toString())
        val UserData = hashMapOf(
            "name" to Name,
            "email" to Email,
            "image" to Image,
            "skills" to Skills

        )
        doc.update(UserData).addOnSuccessListener {
            Log.d("Test", "FirestoreSuccess")
        }.addOnFailureListener { exception ->
            Log.d("Test", "FirestoreFailure")
        }

    }

    //To Create a project use:
    //val DH = DatabaseHandler()
    //DH.CreateProject("TestProjectName","TestProjectDescription",auth.currentUser?.uid.toString(), hashMapOf("User1Id" to false,"User2Id" to false),arrayOf(arrayOf("TaskNameTest","TaskUser")))
    //
    public fun CreateProject(
        ProjectName: String,
        ProjectDescription: String,
        ProjectManager: String,
        ProjectDeadline: Timestamp,
        ProjectUsers: Array<Any>,
        Tasks: Array<Array<Any>>
    ) {

        val doc = base.collection("Projects").document()
        var TaskDatas:MutableList<Map<String,Any>> = ArrayList()
        for (Task in Tasks) {
            val TaskData = hashMapOf(
                "TaskName" to Task[0],
                "TaskUser" to Task[1],
                "TaskDeadline" to Task[2],
                "TaskStatus" to "Assigned",
            )
            TaskDatas.add(TaskData)
        }
        val ProjectData = hashMapOf(
            "ProjectName" to ProjectName,
            "ProjectDescription" to ProjectDescription,
            "ProjectManager" to ProjectManager,
            "ProjectStatus" to "Ongoing",
            "ProjectUsers" to ProjectUsers.asList(),
            "ProjectDeadline" to ProjectDeadline,
            "ProjectTasks" to TaskDatas
        )
        doc.set(ProjectData).addOnSuccessListener {
            Log.d("Test", "FirestoreSuccess")
        }.addOnFailureListener { exception ->
            Log.d("Test", "FirestoreFailure")
        }
    }
    public fun UpdateProjectVerified(ProjectId: String,User:String){
        var doc = base.collection("Projects").document(ProjectId)
        var UpdatedData: MutableMap<String,Any>? = null
        for (Project in ProjectCache!!){
            if (Project.id == ProjectId){
                UpdatedData = Project.data
            }
        }
        var UserList = UpdatedData?.get("ProjectUsers") as MutableMap<Any,Any>?
        //NEED TO SOMEHOW ACTUALLY CHANGE THE VALUE
        if (UserList != null) {
            UserList[User] = true
        }


        doc.update("ProjectUsers",UpdatedData).addOnSuccessListener {
            Log.d("Test", "FirestoreSuccess")
        }.addOnFailureListener { exception ->
            Log.d("Test", "FirestoreFailure")
        }
    }

    public fun UpdateTask(ProjectId:String,TaskIndex:Int, Checked: Boolean){
        for (Project in ProjectCache!!){
            if (Project.id == ProjectId){
                var TaskData:ArrayList<HashMap<String,Any>?> = Project.get("ProjectTasks") as ArrayList<HashMap<String,Any>?>
                if (Checked) {
                    TaskData?.get(TaskIndex)?.set("TaskStatus", "Completed")
                   // var Tasks:ArrayList<Any> = Project.get("ProjectTasks") as ArrayList<Any>
                   // Tasks.set(TaskIndex,"Completed")
                } else{
                    TaskData?.get(TaskIndex)?.set("TaskStatus", "Assigned")
                   // var Tasks:ArrayList<Any> = Project.get("ProjectTasks") as ArrayList<Any>
                  //  Tasks.set(TaskIndex,"Assigned")
                }
                base.collection("Projects").document(ProjectId).update("ProjectTasks",TaskData).addOnSuccessListener {
                    Log.d("Test", "FirestoreSuccess")
                    var AllTasksComplete = true
                    var TaskArray = Project.get("ProjectTasks") as ArrayList<HashMap<String,Any>>
                    for (taskIndex in TaskArray.indices){
                        if (taskIndex == TaskIndex){
                            if (Checked == false){
                                Log.d("NotAllComplete","Fail1")
                                AllTasksComplete = false
                            }
                        }else{
                            if ((TaskArray[taskIndex].get("TaskStatus").toString() != "Completed")){
                                Log.d("NONON",TaskArray[taskIndex].get("TaskStatus").toString() )
                                Log.d("NotAllComplete","Fail2")
                                AllTasksComplete = false
                            }
                        }

                    }
                    if (AllTasksComplete){
                        Log.d("Test", "All tasks complete")
                        base.collection("Projects").document(ProjectId).update("ProjectStatus","Completed").addOnSuccessListener {
                            Log.d("Test", "FirestoreSuccess")
                        }.addOnFailureListener { exception ->
                            Log.d("Test", "FirestoreFailure")
                        }
                    }
                }.addOnFailureListener { exception ->
                    Log.d("Test", "FirestoreFailure")
                }
            }
        }

    }
    public fun FetchProjects(ProjectManager: String?, User: String?,Frag: ProjectListFragment?) {
        //If ProjectManager not null then query just those
        //If user not null then query just those
        base.collection("Projects").get().addOnSuccessListener { result ->
            this.ProjectCache = result
            if (Frag != null) {
                Frag.updateUI()
            }
            for (doc in result) {
                Log.d("DataHandler", "${doc.id} => ${doc.data}")
                //Maybe trigger draw from here?

            }
            Log.d("Data received?", "FirestoreSuccess")
        }.addOnFailureListener { exception ->
            Log.d("Test", "FirestoreFailure")
        }
    }

    public fun FetchUsers(){
        base.collection("Users").get().addOnSuccessListener { result ->
            this.UserCache = result
            for (doc in result) {
                Log.d("DataHandler", "${doc.id} => ${doc.data}")

            }
            Log.d("Data received?", "FirestoreSuccess")
        }.addOnFailureListener { exception ->
            Log.d("Test", "FirestoreFailure")
        }
    }

//    public fun fetchTaskByUserId(id: String){
//        var email = auth.currentUser?.email
//
//        for (Project in ProjectCache!!){
//            if (Project.id == ProjectId){
//                var TaskData:Array<Array<Any>>? = Project.get("ProjectTasks") as Array<Array<Any>>?
//
//                TaskData?.get(TaskIndex)?.set(2, "Completed")
//                base.collection("Projects").document(ProjectId).update("ProjectTasks",TaskData).addOnSuccessListener {
//                    Log.d("Test", "FirestoreSuccess")
//                }.addOnFailureListener { exception ->
//                    Log.d("Test", "FirestoreFailure")
//                }
//            }
//        }
//    }

    public fun GetUsers():QuerySnapshot?{
        return this.UserCache
    }

    public fun getIndividualUser(id: String):DocumentSnapshot? {
        for (user in this.UserCache!!) {

            if (user?.id == id) {
              //  Log.d("User String", user.toString())
                return user
            }
        }
        return null
    }

    public fun getUserByEmail(email: String):DocumentSnapshot? {
        for (user in this.UserCache!!) {
            Log.d("User Email", user.get("email").toString())
            if (user.get("email").toString() == email) {
                return user
            }
        }
        return null
    }

    public fun GetProjects():QuerySnapshot?{
        return this.ProjectCache
    }

}

