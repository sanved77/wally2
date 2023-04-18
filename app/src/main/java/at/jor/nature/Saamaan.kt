package at.jor.superhero

data class Saamaan (
    val status: Int,
    val appBlob: AppBlob
)

data class AppBlob (
    val name: String,
    val incognito: Boolean,
    val categories: List<Category>
)

data class Category (
    val name: String,
    val cover: String,
    val wallies: List<String>
) {
    public override fun toString(): String {
        return "Name - $name, Cover - $cover, Wallies - ${wallies.toString()}"
    }
}