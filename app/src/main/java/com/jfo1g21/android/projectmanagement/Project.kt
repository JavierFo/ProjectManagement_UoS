package com.jfo1g21.android.projectmanagement
import java.util.*

data class Project(
    var id: UUID = UUID.randomUUID(),
    var title: String = "",
    var manager: String = "",
    var projectDeadline: Date = Date(),
    var projectStatus: Boolean = false,
    var teamMembers: Array<String> = arrayOf("", "", ""),
    var projectTasks: Array<String> = arrayOf("", "", ""),
                   )