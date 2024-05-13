package org.altbeacon.etsiindoor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.altbeacon.apiUsers.ApiClientUsuarios
import org.altbeacon.apiUsers.User
import org.altbeacon.utils.BaseActivity

class UsersActivity : BaseActivity() {
    private lateinit var usersListView: ListView
    private lateinit var usersAdapter: UserAdapter
    private lateinit var emptyTextView: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)
        setupToolbar(R.id.toolbar)

        usersListView = findViewById(R.id.usersListView)
        emptyTextView = findViewById(R.id.emptyTextView)
        progressBar = findViewById(R.id.progressBar)

        getUsers()
    }

    private fun getUsers() {
        progressBar.visibility = View.VISIBLE
        // Disable the ListView until the data is loaded
        usersListView.isEnabled = false
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClientUsuarios.getUsers(this@UsersActivity)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val usuarios = response.body()!!
                        runOnUiThread {
                            if (usuarios.isEmpty()) {
                                usersListView.visibility = View.GONE
                                emptyTextView.visibility = View.VISIBLE
                            } else {
                                usersListView.visibility = View.VISIBLE
                                emptyTextView.visibility = View.GONE
                                usersAdapter = UserAdapter(this@UsersActivity, usuarios)
                                usersListView.adapter = usersAdapter

                                usersListView.isEnabled = true
                            }
                        }
                    } else {
                        Log.d("api",response.toString() )
                        runOnUiThread {
                            Toast.makeText(this@UsersActivity, "Error al obtener los registros", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: java.net.SocketTimeoutException) {
                runOnUiThread {
                    Toast.makeText(this@UsersActivity, "Vuelve a probar dentro de 1 minuto", Toast.LENGTH_SHORT).show()
                }
            }catch(e: Exception){
                runOnUiThread {
                    Log.d("api","Exception: "+ e.toString())
                    Toast.makeText(
                        this@UsersActivity,
                        "Error al obtener los registros. Contacte con el administrador",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    usersListView.setOnItemClickListener { _, _, position, _ ->
                        // Get the User at the clicked position
                        val user = usersAdapter.getItem(position) as User
                        // Show a Toast with the User's username
                        val intent = Intent(this@UsersActivity, EditUserActivity::class.java).apply {
                            putExtra("USER_ID", user.id)
                        }
                        startActivity(intent)                    }
                }
            }
        }
    }
}