package ec.edu.uisek.githubclient

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ec.edu.uisek.githubclient.databinding.ActivityLoginBinding
import ec.edu.uisek.githubclient.services.RetrofitClient
import ec.edu.uisek.githubclient.services.SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        binding.loginButton.setOnClickListener { loginUser() }
        setContentView(binding.root)
    }

    private fun loginUser() {
        val user = binding.userInput.text.toString()
        val pass = binding.passwordInput.text.toString()

        if (user.isNotEmpty() && pass.isNotEmpty()) {
            RetrofitClient.createAuthenticatedClient(user, pass)
            val sessionManager = SessionManager(this) // Lo utilizamos para guardar
            sessionManager.saveCredentials(user, pass)
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}