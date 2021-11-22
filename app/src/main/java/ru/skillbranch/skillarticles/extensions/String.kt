package ru.skillbranch.skillarticles.extensions

fun String?.indexesOf(
    substr: String,
    ignoreCase: Boolean = true
): List<Int> {
    this ?: return listOf()
    if (this.isEmpty() || substr.isEmpty()) return listOf()
    val tempSubstr = if (ignoreCase) substr.toLowerCase() else substr
    val tempText = if (ignoreCase) this.toLowerCase() else this
    return arrayListOf<Int>().apply {
        Regex(tempSubstr).findAll(tempText).forEach {
            add(it.range.first)
        }
    }
}
