package ec.edu.uisek.githubclient.models

import kotlinx.serialization.Serializable

@Serializable
data class Repo(
    val id: Long,                           // Identificador
    val name: String,                       // Nombre
    val url: String,                        // URL
    val description: String?,               // Descripción con el ? por que puede venir nulo
    val language: String?,                  // Idioma
    val owner: RepoOwner,                   // Dueño del repositorio
): java.io.Serializable

@Serializable
data class RepoRequest(
    val name: String,
    val description: String?
): java.io.Serializable
