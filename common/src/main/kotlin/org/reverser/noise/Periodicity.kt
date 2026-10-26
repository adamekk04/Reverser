package org.reverser.noise

/** Period detection independent of Minecraft and Fabric APIs. */
object Periodicity {
    fun findPeriod(values: IntArray, minRepetitions: Int): Int? {
        if (values.isEmpty() || minRepetitions < 2) return null

        val prefix = IntArray(values.size)
        for (i in 1 until values.size) {
            var matched = prefix[i - 1]
            while (matched > 0 && values[i] != values[matched]) {
                matched = prefix[matched - 1]
            }
            if (values[i] == values[matched]) {
                matched++
            }
            prefix[i] = matched
        }

        val period = values.size - prefix.last()
        return period.takeIf {
            it < values.size && it.toLong() * minRepetitions <= values.size.toLong()
        }
    }
}
