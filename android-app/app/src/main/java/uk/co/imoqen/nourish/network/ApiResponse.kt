package uk.co.imoqen.nourish.network

// based on the json structure retrieved from the example, https://jsonplaceholder.typicode.com/
data class ApiResponse(
    val userId: Int,
    val id: Int,
    val title: String,
    val completed: Boolean
)