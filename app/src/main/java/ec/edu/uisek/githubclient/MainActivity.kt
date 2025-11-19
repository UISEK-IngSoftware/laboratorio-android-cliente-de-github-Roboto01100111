package ec.edu.uisek.githubclient

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ec.edu.uisek.githubclient.databinding.ActivityMainBinding
import ec.edu.uisek.githubclient.models.Repo
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

        binding.newRepoFab.setOnClickListener { displayNewRepoForm() }
    }

    override fun onResume() {
        super.onResume()
        setUpRecyclerView()
        fetchRepositories()
    }

    private fun setUpRecyclerView() {
        reposAdapter = ReposAdapter(
            onEditClick = { repo -> displayEditRepoForm(repo) },
            onDeleteClick = { repo -> showDeleteConfirmationDialog(repo) }
        )
        binding.reposRecyclerView.adapter = reposAdapter
    }

    private fun fetchRepositories() {
        val call = RetrofitClient.gitHubApiService.getRepos()
        call.enqueue(object : Callback<List<Repo>> {
            override fun onResponse(call: Call<List<Repo>>, response: Response<List<Repo>>) {
                if (response.isSuccessful) {
                    response.body()?.let { reposAdapter.updateRepositories(it) }
                } else {
                    handleApiError(response.code(), response.message())
                }
            }

            override fun onFailure(call: Call<List<Repo>>, t: Throwable) {
                handleNetworkError(t.message)
            }
        })
    }

    private fun displayNewRepoForm() {
        val intent = Intent(this, RepoForm::class.java)
        startActivity(intent)
    }

    private fun displayEditRepoForm(repo: Repo) {
        val intent = Intent(this, RepoForm::class.java).apply {
            putExtra("repo", repo)
        }
        startActivity(intent)
    }

    private fun showDeleteConfirmationDialog(repo: Repo) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Repositorio")
            .setMessage("¿Estás seguro de que quieres eliminar el repositorio '${repo.name}'?")
            .setPositiveButton("Eliminar") { _, _ -> deleteRepository(repo) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteRepository(repo: Repo) {
        val call = RetrofitClient.gitHubApiService.deleteRepository(repo.owner.login, repo.name)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showMessage("Repositorio eliminado exitosamente")
                    fetchRepositories() // Refresh the list
                } else {
                    handleApiError(response.code(), response.message())
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                handleNetworkError(t.message)
            }
        })
    }

    private fun handleApiError(code: Int, message: String) {
        val errorMsg = when (code) {
            401 -> "Error de autenticación"
            403 -> "Error de autorización"
            404 -> "Recurso no encontrado"
            else -> "Error: $code: $message"
        }
        Log.e("MainActivity", errorMsg)
        showMessage(errorMsg)
    }

    private fun handleNetworkError(message: String?) {
        Log.e("MainActivity", "Error de red: $message")
        showMessage("Error de red: $message")
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}