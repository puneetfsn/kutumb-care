package com.puneetfsn.kutumbcare.data

/**
 * Pure maths for sampled bitmap decoding. Decoding a modern phone photo
 * (40–50 MP) at full size needs ~200 MB and fails outright on many devices,
 * so every decode path first reads the bounds and then decodes a sampled
 * version close to the size actually needed.
 */
object ImageSampling {

    /**
     * Largest power-of-two sample size that still leaves the biggest
     * dimension at or above [maxPx] (or 1 when the image already fits).
     */
    fun sampleSizeFor(width: Int, height: Int, maxPx: Int): Int {
        if (width <= 0 || height <= 0 || maxPx <= 0) return 1
        val largest = maxOf(width, height)
        var sample = 1
        while (largest / (sample * 2) >= maxPx && sample < 64) sample *= 2
        return sample
    }
}
