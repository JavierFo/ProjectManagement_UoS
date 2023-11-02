package com.jfo1g21.android.projectmanagement
import java.util.*

data class Task (var id: UUID = UUID.randomUUID(),
                 var title: String = "",
                 var assignedTeamMember: String = "",
                 var taskDeadline: Date = Date(),
                 var taskStatus: String = ""
)