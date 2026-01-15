package ec.edu.uisek.githubclient

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ec.edu.uisek.githubclient.databinding.ActivityRepoEditFormBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.models.RepoRequest
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepoEditForm : AppCompatActivity() {

    private lateinit var repoEditFormBinding: ActivityRepoEditFormBinding
    private var repoOwner: String = ""
    private var originalRepoName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repoEditFormBinding = ActivityRepoEditFormBinding.inflate(layoutInflater)
        setContentView(repoEditFormBinding.root)
        
        // Obtener datos del repositorio desde el Intent
        originalRepoName = intent.getStringExtra("REPO_NAME") ?: ""
        val repoDescription = intent.getStringExtra("REPO_DESCRIPTION") ?: ""
        repoOwner = intent.getStringExtra("REPO_OWNER") ?: ""
        
        // Prellenar los campos con los datos del repositorio
        repoEditFormBinding.repoNameInput.setText(originalRepoName)
        repoEditFormBinding.repoDescriptionInput.setText(repoDescription)
        
        repoEditFormBinding.cancelButton.setOnClickListener { finish() }
        repoEditFormBinding.editButton.setOnClickListener { editRepo() }
    }

    private fun validateForm(): Boolean {
        val repoName = repoEditFormBinding.repoNameInput.text.toString()

        if  (repoName.isBlank()) {
            repoEditFormBinding.repoNameInput.error = "El nombre del repositorio es requerido"
            return false
        }

        if  (repoName.contains(" ")) {
            repoEditFormBinding.repoNameInput.error = "El nombre del repositorio no puede contener espacios"
            return false
        }

        return true
    }

    private fun editRepo() {

        if (!validateForm()) {
            return
        }

        val repoName = repoEditFormBinding.repoNameInput.text.toString()
        val repoDescription = repoEditFormBinding.repoDescriptionInput.text.toString()

        val repoRequest: RepoRequest = RepoRequest(
            name = repoName,
            description = repoDescription
        )

        val apiService = RetrofitClient.getApiService()
        val call = apiService.updateRepository(repoOwner, originalRepoName, repoRequest)

        call.enqueue(object : Callback<Repo> {
            override fun onResponse(call: Call<Repo?>, response: Response<Repo?>) {
                if (response.isSuccessful) {
                    Log.d("RepoEditForm", "El repositorio ${repoName} ha sido actualizado exitosamente")
                    showMessage("El repositorio ${repoName} ha sido actualizado exitosamente")
                    finish()
                } else {
                    val errMsg = when (response.code()) {
                        401 -> "Error de autenticación"
                        403 -> "Error de autorización"
                        404 -> "Error de recurso no encontrado"
                        else -> "Error desconocido: ${response.code()}: ${response.message()}"
                    }
                    Log.e("RepoEditForm", errMsg)
                    showMessage(errMsg)
                }
            }

            override fun onFailure(call: Call<Repo?>, t: Throwable) {
                Log.e("RepoEditForm", "Error de red: ${t.message}")
                showMessage("Error de red: ${t.message}")
            }
        })

    }

    private fun showMessage (msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}