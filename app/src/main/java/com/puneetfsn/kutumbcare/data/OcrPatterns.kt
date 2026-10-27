package com.puneetfsn.kutumbcare.data

/**
 * Finds the numbers a family actually needs inside text read from a document
 * photo: a PAN, an Aadhaar number, and a date. Pure JVM so it is unit-tested.
 *
 * Aadhaar in the wild: "1234 5678 9012", split across lines, hyphenated, or
 * (when OCR merges the groups) twelve digits in a row. The look-arounds stop
 * us grabbing any twelve digits of a longer number such as the 16-digit
 * Virtual ID printed under the Aadhaar number itself: a match may not start
 * right after "digit + space/hyphen", nor end right before
 * "space/hyphen + digit". Newlines deliberately do not count as
 * continuations — a genuine Aadhaar often begins on the line after a date.
 */
object OcrPatterns {

    private val PAN = Regex("[A-Z]{5}[0-9]{4}[A-Z]")
    private val AADHAAR =
        Regex("(?<!\\d)(?<![\\d][ -])(\\d{4})[\\s-]?(\\d{4})[\\s-]?(\\d{4})(?![ -]?\\d)")
    private val DATE = Regex("\\d{2}[/-]\\d{2}[/-]\\d{4}")

    /** PAN wins when both appear; Aadhaar is returned grouped 4-4-4. */
    fun findId(text: String): String? {
        PAN.find(text)?.let { return it.value }
        AADHAAR.find(text)?.let { m ->
            return "${m.groupValues[1]} ${m.groupValues[2]} ${m.groupValues[3]}"
        }
        return null
    }

    /** First DD/MM/YYYY-style date, normalised to slashes. */
    fun findDate(text: String): String? =
        DATE.find(text)?.value?.replace('-', '/')
}
