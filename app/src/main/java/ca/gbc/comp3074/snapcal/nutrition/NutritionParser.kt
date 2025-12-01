package ca.gbc.comp3074.snapcal.nutrition

object NutritionParser {

    data class NutritionData(
        val calories: Int? = null,
        val fat: Int? = null,
        val saturatedFat: Int? = null,
        val cholesterol: Int? = null,
        val sodium: Int? = null,
        val carbs: Int? = null,
        val fiber: Int? = null,
        val sugars: Int? = null,
        val protein: Int? = null
    )

    fun parse(rawText: String): NutritionData {
        val cleaned = rawText.lowercase()
            .replace(":", " ")
            .replace(",", " ")
            .replace(";", " ")
            .replace("(", " ")
            .replace(")", " ")
            .replace(Regex("\\s+"), " ")

        fun extract(regex: Regex): Int? =
            regex.find(cleaned)?.groupValues?.getOrNull(1)?.toIntOrNull()

        return NutritionData(
            calories      = extract(Regex("""calor[a-z]*\s+(\d{1,4})""")),
            fat           = extract(Regex("""total\s+fat\s+(\d{1,3})""")),
            saturatedFat  = extract(Regex("""satur[a-z]*\s+fat\s+(\d{1,3})""")),
            cholesterol   = extract(Regex("""chol[eoa][a-z]*\s+(\d{1,4})""")),
            sodium        = extract(Regex("""sod[i1]um\s+(\d{1,4})""")),
            carbs         = extract(Regex("""carbo[a-z]*\s+(\d{1,3})""")),
            fiber         = extract(Regex("""fib[eoa][a-z]*\s+(\d{1,3})""")),
            sugars        = extract(Regex("""sugar[a-z]*\s+(\d{1,3})""")),
            protein       = extract(Regex("""prot[eioa][a-z]*\s+(\d{1,3})"""))
        )
    }
}
