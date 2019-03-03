package com.github.hiroshi_cl.sa.withSentinel.algorithm

import com.github.hiroshi_cl.sa.withSentinel.SuffixArray

class NaiveStringSortKt: SuffixArray() {
    override fun saInternal(cs: CharArray, sa: IntArray): IntArray {
        val n = cs.size

        return generateSequence(String(cs)) {
            it.substring(1, it.length)
        }.take(n).sorted().map {
            n - it.length
        }.toList().toIntArray()
    }
}
