package org.reverser.enums

/**
 * Types of density/noise functions.
 * is3D = true means function depends on Y coord (else Y is ignored).
 */
enum class NoiseType(val is3D: Boolean) {
    CONTINENTS(false),
    EROSION(false),
    RIDGES(false),
    TEMPERATURE(false),
    VEGETATION(false),

    DEPTH(true),
    CHUNK_SURFACE_LEVEL(true),
    FINAL_DENSITY(true);

    companion object {
        fun fromString(name: String): NoiseType? =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) }

        fun suggestionList(): List<String> = entries.map { it.name.lowercase() }
    }
}