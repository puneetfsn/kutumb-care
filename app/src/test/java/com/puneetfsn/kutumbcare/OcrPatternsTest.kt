package com.puneetfsn.kutumbcare

import com.puneetfsn.kutumbcare.data.ImageSampling
import com.puneetfsn.kutumbcare.data.OcrPatterns
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** OCR extraction patterns + sampled-decode maths (pure JVM). */
class OcrPatternsTest {

    @Test
    fun `finds PAN in card text`() {
        val text = "INCOME TAX DEPARTMENT\nRAJESH KUMAR\nABCDE1234F\n"
        assertEquals("ABCDE1234F", OcrPatterns.findId(text))
    }

    @Test
    fun `finds spaced Aadhaar and groups it 4-4-4`() {
        val text = "Government of India\nRajesh Kumar\nDOB: 12/05/1990\n1234 5678 9012\n"
        assertEquals("1234 5678 9012", OcrPatterns.findId(text))
    }

    @Test
    fun `finds Aadhaar split across lines`() {
        val text = "Rajesh Kumar\n1234 5678\n9012\nVID: 1234 5678 9012 3456"
        assertEquals("1234 5678 9012", OcrPatterns.findId(text))
    }

    @Test
    fun `finds Aadhaar read as twelve digits in a row`() {
        assertEquals("1234 5678 9012", OcrPatterns.findId("No. 123456789012 issued"))
    }

    @Test
    fun `does not grab the first twelve digits of the 16-digit VID`() {
        assertNull(OcrPatterns.findId("VID: 1234 5678 9012 3456"))
    }

    @Test
    fun `PAN wins over Aadhaar when both are present`() {
        val text = "ABCDE1234F and 1234 5678 9012"
        assertEquals("ABCDE1234F", OcrPatterns.findId(text))
    }

    @Test
    fun `no id in ordinary text`() {
        assertNull(OcrPatterns.findId("State Bank of India, savings account"))
    }

    @Test
    fun `finds date and normalises dashes to slashes`() {
        assertEquals("05/11/2031", OcrPatterns.findDate("Valid up to 05-11-2031"))
        assertEquals("12/05/1990", OcrPatterns.findDate("DOB: 12/05/1990"))
        assertNull(OcrPatterns.findDate("Year of birth 1990"))
    }

    @Test
    fun `sampling never upscales and steps down by powers of two`() {
        assertEquals(1, ImageSampling.sampleSizeFor(800, 600, 1600))
        assertEquals(1, ImageSampling.sampleSizeFor(1600, 1200, 1600))
        assertEquals(2, ImageSampling.sampleSizeFor(4000, 3000, 1600))
        assertEquals(4, ImageSampling.sampleSizeFor(8160, 6120, 1600))
        assertEquals(1, ImageSampling.sampleSizeFor(0, 0, 1600))
        assertEquals(2, ImageSampling.sampleSizeFor(4400, 3300, 2200))
    }
}
