package com.statsup.domain

/**
 * Percentage of the total distance spent uphill, downhill and on flat terrain.
 *
 * Values are percentages of the total distance (0..100) and always sum to (approximately) 100.
 */
data class GradeBreakdown(
    val uphillPercent: Double,
    val downhillPercent: Double,
    val flatPercent: Double
)

/**
 * Estimates how much of a training's distance was spent uphill, downhill and on flat terrain
 * from its altitude stream.
 *
 * [elevationPoints] is assumed to be evenly spaced along [totalDistanceMeters] — the same
 * assumption already used by [com.statsup.ui.components.ElevationProfileChart] to place
 * altitude samples on the X axis, since intervals.icu's altitude stream doesn't carry its own
 * per-point distance.
 *
 * The raw stream is typically sampled once per second, so on a gradual climb the altitude delta
 * *between two consecutive raw samples* is tiny — much smaller than the GPS/barometric noise
 * itself — even though the climb is very real over a longer stretch. Comparing adjacent raw
 * samples directly against a fixed elevation threshold would therefore misclassify real climbs as
 * flat. To avoid that, the stream is first downsampled to at most [maxSegments] points by
 * averaging each bin (this cancels out sample-to-sample noise while preserving the actual
 * trend), and grade is computed between these averaged points instead of the raw samples.
 *
 * Classification is done on the *grade* (elevation delta divided by the segment's distance),
 * not on the raw elevation delta. Using an absolute elevation threshold would systematically
 * underestimate flat distance on longer trainings: with [maxSegments] capped at 100, a long
 * training is cut into much longer segments, so even a gentle, essentially flat road easily
 * accumulates more than a couple of meters of elevation noise/drift over a single (long)
 * segment and would incorrectly be counted as sloped. Comparing the grade instead of the
 * absolute delta scales correctly with the segment's distance regardless of how coarse the
 * downsampling is.
 *
 * Returns null when there isn't enough data to compute a meaningful breakdown.
 */
fun computeGradeBreakdown(
    elevationPoints: List<Double>,
    totalDistanceMeters: Double,
    maxSegments: Int = 100,
    flatGradeThresholdPercent: Double = 1.0
): GradeBreakdown? {
    if (elevationPoints.size < 2 || totalDistanceMeters <= 0) return null

    val targetPointCount = (maxSegments + 1).coerceAtMost(elevationPoints.size)
    val smoothed = averageIntoBins(elevationPoints, targetPointCount)
    if (smoothed.size < 2) return null

    val segmentCount = smoothed.size - 1
    val segmentDistance = totalDistanceMeters / segmentCount

    var uphillDistance = 0.0
    var downhillDistance = 0.0

    for (i in 0 until segmentCount) {
        val delta = smoothed[i + 1] - smoothed[i]
        val gradePercent = delta / segmentDistance * 100.0
        when {
            gradePercent > flatGradeThresholdPercent -> uphillDistance += segmentDistance
            gradePercent < -flatGradeThresholdPercent -> downhillDistance += segmentDistance
        }
    }

    val flatDistance = (totalDistanceMeters - uphillDistance - downhillDistance).coerceAtLeast(0.0)

    return GradeBreakdown(
        uphillPercent = uphillDistance / totalDistanceMeters * 100.0,
        downhillPercent = downhillDistance / totalDistanceMeters * 100.0,
        flatPercent = flatDistance / totalDistanceMeters * 100.0
    )
}

/**
 * Downsamples [points] to at most [targetPointCount] points by averaging each contiguous bin,
 * preserving the first and last sample position. Returns [points] unchanged if it's already at
 * or below [targetPointCount].
 */
private fun averageIntoBins(points: List<Double>, targetPointCount: Int): List<Double> {
    if (points.size <= targetPointCount || targetPointCount < 2) return points

    return (0 until targetPointCount).map { i ->
        val start = i * points.size / targetPointCount
        val end = ((i + 1) * points.size / targetPointCount).coerceAtLeast(start + 1).coerceAtMost(points.size)
        val slice = points.subList(start, end)
        slice.average()
    }
}
