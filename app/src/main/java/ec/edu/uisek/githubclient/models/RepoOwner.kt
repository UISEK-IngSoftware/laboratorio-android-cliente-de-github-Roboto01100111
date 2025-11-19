package ec.edu.uisek.githubclient.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/*
https://docs.github.com/es/rest/repos/repos?apiVersion=2022-11-28
List organization repositories
*/
@Serializable
data class RepoOwner(
    val id: Long,                           // Identificador
    val login: String,                      // Nombre de usuario
    @SerializedName("avatar_url")   // Para que sepa que avatarUrl es avatar_url
    val avatarUrl: String                   // Imágen del avatar
): java.io.Serializable
