package ec.edu.uisek.githubclient

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ec.edu.uisek.githubclient.databinding.ActivityMainBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.services.GitHubApiService
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var reposAdapter: ReposAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.newRepoFab.setOnClickListener {
            displayNewRepoForm()
        }
    }

    override fun onResume() {
        super.onResume()
        setUpRecyclerView()
        fetchRepositories()
    }

    private fun setUpRecyclerView() {
        reposAdapter = ReposAdapter(
            onEditClick = { repo ->
                displayEditRepoForm(repo)
            },
            onDeleteClick = { repo ->
                showDeleteConfirmationDialog(repo)
            }
        )
        binding.reposRecyclerView.adapter = reposAdapter
    }
    
    private fun displayEditRepoForm(repo: Repo) {
        Intent(this, RepoEditForm::class.java).apply {
            putExtra("REPO_NAME", repo.name)
            putExtra("REPO_DESCRIPTION", repo.description ?: "")
            putExtra("REPO_OWNER", repo.owner.login)
            startActivity(this)
        }
    }
    
    private fun showDeleteConfirmationDialog(repo: Repo) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar repositorio")
            .setMessage("¿Estás seguro de que deseas eliminar el repositorio \"${repo.name}\"?\nEsta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteRepository(repo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun deleteRepository(repo: Repo) {
        val apiService = RetrofitClient.getApiService()
        val call = apiService.deleteRepository(repo.owner.login, repo.name)
        
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("MainActivity", "El repositorio ${repo.name} ha sido eliminado exitosamente")
                    showMessage("El repositorio ${repo.name} ha sido eliminado exitosamente")
                    // Refrescar la lista de repositorios
                    fetchRepositories()
                } else {
                    val errMsg = when (response.code()) {
                        401 -> "Error de autenticación"
                        403 -> "Error de autorización"
                        404 -> "Error de recurso no encontrado"
                        else -> "Error desconocido: ${response.code()}: ${response.message()}"
                    }
                    Log.e("MainActivity", errMsg)
                    showMessage(errMsg)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                val errMsg = "Error de conexión: ${t.message}"
                Log.e("MainActivity", errMsg, t)
                showMessage(errMsg)
            }
        })
    }

    private fun fetchRepositories() {
        val apiService = RetrofitClient.getApiService()
        val call = apiService.getRepos()
        call.enqueue(object : Callback<List<Repo>> {
            override fun onResponse(call: Call<List<Repo>?>, response: Response<List<Repo>?>) {
                if (response.isSuccessful) {
                    val repos = response.body()
                    if (repos != null && repos.isNotEmpty()) {
                        reposAdapter.updateRepositories(repos)
                    }
                } else {
                    val errMsg = when (response.code()) {
                        401 -> "Error de autenticación"
                        403 -> "Error de autorización"
                        404 -> "Error de recurso no encontrado"
                        else -> "Error desconocido: ${response.code()}: ${response.message()}"
                    }
                    Log.e("MainActivity", errMsg)
                    showMessage(errMsg)
                }
            }

            override fun onFailure(call: Call<List<Repo>?>, t: Throwable) {
                val errMsg = "Error de conexión: ${t.message}"
                Log.e("MainActivity", errMsg, t)
                showMessage(errMsg)
            }
        })
    }

    private fun showMessage (msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun displayNewRepoForm() {
        Intent(this, RepoForm::class.java).apply {
            startActivity(this)

        }
    }
}