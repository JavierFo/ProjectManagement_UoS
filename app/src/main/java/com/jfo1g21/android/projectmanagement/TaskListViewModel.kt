package com.jfo1g21.android.projectmanagement

import androidx.lifecycle.ViewModel

class TaskListViewModel: ViewModel() {
    val tasks = ArrayList<Task>()
    private val mockMembers = arrayOf("George", "German", "John", "Javier", "Paul", "Pauline", "Ringo", "Richard")
    init {
//        for (i in 0 until mockMembers.size) {
//            val task = Task()
//            task.title = "tempTask"
//            task.taskStatus = i % 2 == 0
//            task.assignedTeamMember = mockMembers[i]
//            tasks += task
//        }
    }
}