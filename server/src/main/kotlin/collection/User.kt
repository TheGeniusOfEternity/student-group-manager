package collection

class User(
    var id: Int?,
    val username: String,
    val passwordHash: String,
) {
    override fun toString(): String {
        return "User(id=$id, username='$username', passwordHash='$passwordHash')"
    }
}