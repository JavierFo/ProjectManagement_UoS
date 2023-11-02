package com.jfo1g21.android.projectmanagement
import androidx.lifecycle.ViewModel
import java.util.*
import kotlin.collections.ArrayList

class ProjectsListViewModel: ViewModel() {


    val projects = ArrayList<Project>()

    init {
        for (i in 0 until 33) {
            val project = Project()
            project.title = "tempProject"
            project.projectStatus = i % 2 == 0
            val randomDay = (1 until 20).random()
            project.projectDeadline = Date(Date().getTime() + (1000 * 60 * 60 * 24 * (randomDay)));
            projects += project
        }
    }
}