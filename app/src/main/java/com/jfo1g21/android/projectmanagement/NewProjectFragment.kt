package com.jfo1g21.android.projectmanagement

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QuerySnapshot
import java.util.*
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList

private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
public var taskDates: Array<Date> = emptyArray()

class NewProjectFragment : Fragment(), DatePickerFragment.Callbacks {
    private lateinit var project: Project

    private lateinit var newProjectTitleEditText: EditText
    private lateinit var newProjectDeadlineSubmitButton: Button
    private lateinit var newProjectMember1Spinner: Spinner
    private lateinit var newProjectMember2Spinner: Spinner
    private lateinit var newProjectMember3Spinner: Spinner

    private lateinit var newProjectTask1EditText: EditText
    private lateinit var newTask1DeadlineButton: Button
    private lateinit var newTask1MemberSpinner: Spinner

    private lateinit var newProjectTask2EditText: EditText
    private lateinit var newTask2DeadlineButton: Button
    private lateinit var newTask2MemberSpinner: Spinner

    private lateinit var newProjectTask3EditText: EditText
    private lateinit var newTask3DeadlineButton: Button
    private lateinit var newTask3MemberSpinner: Spinner

    private lateinit var newProjectDescriptionEditText: EditText

    private lateinit var newProjectSubmitButton: Button

    var example_users = arrayOf("Paul", "John", "George", "Ringo")
    var task1: String = ""
    var task2: String = ""
    var task3: String = ""

    var member1: String = ""
    var member2: String = ""
    var member3: String = ""

    var task1member: String = ""
    var task2member: String = ""
    var task3member: String = ""

    var taskdeadline1: Date = Date()
    var taskdeadline2: Date = Date()
    var taskdeadline3: Date = Date()

    var projectManagerString: String = ""
    var projectDescriptionString: String = ""
    var timestampProjectDeadline: Date = Date()

    private lateinit var auth: FirebaseAuth

    enum class DeadlineButton {
        PROJECT_DEADLINE, TASK1_DEADLINE, TASK2_DEADLINE, TASK3_DEADLINE, NotYet
    }

    var deadlineButtonPressed: DeadlineButton = DeadlineButton.NotYet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = Project()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_new_project, container, false)

        newProjectTitleEditText = view.findViewById(R.id.new_project_title) as EditText
        newProjectDeadlineSubmitButton = view.findViewById(R.id.new_project_deadline) as Button
        newProjectMember1Spinner = view.findViewById(R.id.new_project_member1) as Spinner
        newProjectMember2Spinner = view.findViewById(R.id.new_project_member2) as Spinner
        newProjectMember3Spinner = view.findViewById(R.id.new_project_member3) as Spinner

        newProjectTask1EditText = view.findViewById(R.id.new_project_task1) as EditText
        newTask1DeadlineButton = view.findViewById(R.id.new_project_task1_deadline) as Button
        newTask1MemberSpinner = view.findViewById(R.id.spinner_task1) as Spinner

        newProjectTask2EditText = view.findViewById(R.id.new_project_task2) as EditText
        newTask2DeadlineButton = view.findViewById(R.id.new_project_task2_deadline) as Button
        newTask2MemberSpinner = view.findViewById(R.id.spinner_task2) as Spinner

        newProjectTask3EditText = view.findViewById(R.id.new_project_task3) as EditText
        newTask3DeadlineButton = view.findViewById(R.id.new_project_task3_deadline) as Button
        newTask3MemberSpinner = view.findViewById(R.id.spinner_task3) as Spinner

        newProjectDescriptionEditText =
            view.findViewById(R.id.project_description_editText) as EditText

        newProjectSubmitButton = view.findViewById(R.id.submit_new_project_button) as Button

        val Users: QuerySnapshot? = DatabaseHandler.GetUsers()
        val UserEmails: MutableList<String> = ArrayList()
        if (Users != null) {
            for (User in Users) {
                UserEmails.add(User.data.get("email").toString())
            }
        }
        Log.d("Email Test", UserEmails.toString())

        val arrayAdapter = context?.let {
            ArrayAdapter<String>(
                it,
                android.R.layout.simple_spinner_dropdown_item,
                UserEmails.toTypedArray()
            )
        }
        newProjectMember1Spinner.adapter = arrayAdapter
        newProjectMember2Spinner.adapter = arrayAdapter
        newProjectMember3Spinner.adapter = arrayAdapter

        newTask1MemberSpinner.adapter = arrayAdapter
        newTask2MemberSpinner.adapter = arrayAdapter
        newTask3MemberSpinner.adapter = arrayAdapter

        newProjectMember1Spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    var emailID = DatabaseHandler.getUserByEmail(UserEmails[p2])
                    project.teamMembers[0] = emailID?.id.toString()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }

        newProjectMember2Spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    var emailID = DatabaseHandler.getUserByEmail(UserEmails[p2])
                    project.teamMembers[1] = emailID?.id.toString()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }

        newProjectMember3Spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    var emailID = DatabaseHandler.getUserByEmail(UserEmails[p2])
                    project.teamMembers[2] = emailID?.id.toString()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }

        newTask1MemberSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                var emailID = DatabaseHandler.getUserByEmail(UserEmails[p2])


                task1member = emailID?.id.toString()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        newTask2MemberSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                var emailID = DatabaseHandler.getUserByEmail(UserEmails[p2])
                task2member = emailID?.id.toString()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        newTask3MemberSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                var emailID = DatabaseHandler.getUserByEmail(UserEmails[p2])
                task3member = emailID?.id.toString()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()

        newProjectDeadlineSubmitButton.text = Date().toString()

        newTask1DeadlineButton.text = "New Deadline"
        newTask2DeadlineButton.text = "New Deadline"
        newTask3DeadlineButton.text = "New Deadline"

        newProjectDeadlineSubmitButton.setOnClickListener {
            deadlineButtonPressed = DeadlineButton.PROJECT_DEADLINE
            DatePickerFragment.newInstance(project.projectDeadline).apply {
                setTargetFragment(this@NewProjectFragment, REQUEST_DATE)
                show(this@NewProjectFragment.parentFragmentManager, DIALOG_DATE)
            }
        }

        newTask1DeadlineButton.setOnClickListener {
            deadlineButtonPressed = DeadlineButton.TASK1_DEADLINE
            DatePickerFragment.newInstance(project.projectDeadline).apply {
                setTargetFragment(this@NewProjectFragment, REQUEST_DATE)
                show(this@NewProjectFragment.parentFragmentManager, DIALOG_DATE)
            }
        }

        newTask2DeadlineButton.setOnClickListener {
            deadlineButtonPressed = DeadlineButton.TASK2_DEADLINE
            DatePickerFragment.newInstance(project.projectDeadline).apply {
                setTargetFragment(this@NewProjectFragment, REQUEST_DATE)
                show(this@NewProjectFragment.parentFragmentManager, DIALOG_DATE)
            }
        }

        newTask3DeadlineButton.setOnClickListener {
            deadlineButtonPressed = DeadlineButton.TASK3_DEADLINE
            DatePickerFragment.newInstance(project.projectDeadline).apply {
                setTargetFragment(this@NewProjectFragment, REQUEST_DATE)
                show(this@NewProjectFragment.parentFragmentManager, DIALOG_DATE)
            }
        }

        editTextsWatchers()

        auth = Firebase.auth
        var user = auth.currentUser?.uid.toString()

        newProjectSubmitButton.setOnClickListener {

            val memberArray: ArrayList<String> = ArrayList()



            for (i in project.teamMembers ){
                if (i != ""){
                    memberArray.add(i)
                }
            }
           // Log.d("eeee", memberArray.toList().toString())
            val projectDeadlineTimeStamp = Timestamp(timestampProjectDeadline)
            val DH = DatabaseHandler
            val timestampProjectDeadlineTimestamp = timestampProjectDeadline
            DH.CreateProject(
                project.title, projectDescriptionString, user, projectDeadlineTimeStamp,
               // arrayOf(memberArray),
                arrayOf(project.teamMembers[0], project.teamMembers[1], project.teamMembers[2]),
                arrayOf(
                    arrayOf(task1, task1member, taskdeadline1),
                    arrayOf(task2, task2member, taskdeadline2),
                    arrayOf(task3, task3member, taskdeadline3)
                )
            )
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, ProjectListFragment())
                .commit()
            // DH.CreateProject("TestProjectName","TestProjectDescription",auth.currentUser?.uid.toString(), hashMapOf("User1Id" to false,"User2Id" to false),arrayOf(arrayOf("TaskNameTest","TaskUser")))
        }

    }

    fun editTextsWatchers() {
        val titleEditTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                project.title = sequence.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        }
        newProjectTitleEditText.addTextChangedListener(titleEditTextWatcher)

        ////TASKS EDIT TEXTS

        val task1EditTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                task1 = sequence.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        }
        newProjectTask1EditText.addTextChangedListener(task1EditTextWatcher)

        val task2EditTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                task2 = sequence.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        }
        newProjectTask2EditText.addTextChangedListener(task2EditTextWatcher)

        val task3EditTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                task3 = sequence.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        }
        newProjectTask3EditText.addTextChangedListener(task3EditTextWatcher)

        val projectDescriptionEditTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                projectDescriptionString = sequence.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        }
        newProjectDescriptionEditText.addTextChangedListener(projectDescriptionEditTextWatcher)
    }

    override fun onDateSelected(date: Date) {

        when (deadlineButtonPressed) {
            DeadlineButton.PROJECT_DEADLINE -> {
                project.projectDeadline = date
                timestampProjectDeadline = date
                newProjectDeadlineSubmitButton.text = project.projectDeadline.toString()
            }
            DeadlineButton.TASK1_DEADLINE -> {
                taskdeadline1 = date
                newTask1DeadlineButton.text = date.toString()
            }
            DeadlineButton.TASK2_DEADLINE -> {
                taskdeadline2 = date
                newTask2DeadlineButton.text = date.toString()
            }
            DeadlineButton.TASK3_DEADLINE -> {
                taskdeadline3 = date
                newTask3DeadlineButton.text = date.toString()
            }
        }
    }
}