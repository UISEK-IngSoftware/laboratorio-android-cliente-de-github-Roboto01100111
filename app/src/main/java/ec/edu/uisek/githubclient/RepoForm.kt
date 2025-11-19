package ec.edu.uisek.githubclient

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ec.edu.uisek.githubclient.databinding.ActivityRepoFormBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.models.RepoRequest
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepoForm : AppCompatActivity() {

    private lateinit var repoFormBinding: ActivityRepoFormBinding
    private var repo: Repo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repoFormBinding = ActivityRepoFormBinding.inflate(layoutInflater)
        setContentView(repoFormBinding.root)

        repo = intent.getSerializableExtra("repo") as? Repo

        if (repo != null) {
            repoFormBinding.repoNameInput.setText(repo!!.name)
            repoFormBinding.repoNameInput.isEnabled = false // El nombre no se puede editar
            repoFormBinding.repoDescriptionInput.setText(repo!!.description)
            repoFormBinding.saveButton.setOnClickListener { updateRepo() }
        } else {
            repoFormBinding.saveButton.setOnClickListener { createRepo() }
        }

        repoFormBinding.cancelButton.setOnClickListener { finish() }
    }

    private fun validateForm(isUpdate: Boolean = false): Boolean {
        val repoName = repoFormBinding.repoNameInput.text.toString()

        if (!isUpdate) {
            if (repoName.isBlank()) {
                repoFormBinding.repoNameInput.error = "El nombre del repositorio es requerido"
                return false
            }

            if (repoName.contains(" ")) {
                repoFormBinding.repoNameInput.error = "El nombre del repositorio no puede contener espacios"
                return false
            }
        }

        return true
    }

    private fun createRepo() {
        if (!validateForm()) {
            return
        }

        val repoRequest = RepoRequest(
            name = repoFormBinding.repoNameInput.text.toString(),
            description = repoFormBinding.repoDescriptionInput.text.toString()
        )

        val call = RetrofitClient.gitHubApiService.addRepository(repoRequest)

        call.enqueue(object : Callback<Repo> {
            override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                if (response.isSuccessful) {
                    showMessage("Repositorio creado exitosamente")
                    finish()
                } else {
                    handleApiError(response.code(), response.message())
                }
            }

            override fun onFailure(call: Call<Repo>, t: Throwable) {
                handleNetworkError(t.message)
            }
        })
    }

    private fun updateRepo() {
        if (!validateForm(isUpdate = true)) {
            return
        }

        val repoRequest = RepoRequest(
            name = repo!!.name, // El nombre no cambia
            description = repoFormBinding.repoDescriptionInput.text.toString()
        )

        val call = RetrofitClient.gitHubApiService.updateRepository(repo!!.owner.login, repo!!.name, repoRequest)

        call.enqueue(object : Callback<Repo> {
            override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                if (response.isSuccessful) {
                    showMessage("Repositorio actualizado exitosamente")
                    finish()
                } else {
                    handleApiError(response.code(), response.message())
                }
            }

            override fun onFailure(call: Call<Repo>, t: Throwable) {
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
        Log.e("RepoForm", errorMsg)
        showMessage(errorMsg)
    }

    private fun handleNetworkError(message: String?) {
        Log.e("RepoForm", "Error de red: $message")
        showMessage("Error de red: $message")
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
