package com.github.hiroshi_cl.sa.withSentinel.algorithm

import java.util.Arrays

import com.github.hiroshi_cl.sa.withSentinel.SuffixArray

class SAIS_KT : SuffixArray() {

    override fun saInternal(cs: CharArray, sa: IntArray): IntArray {
        val s = cs.map { it.toInt() }.toIntArray()
        return rec(s, sa, cs.size, 1 shl Character.SIZE)
    }

    private fun rec(input: IntArray, sa: IntArray, N: Int, K: Int): IntArray {
        // determine L or S and count
        val isS = BooleanArray(N)
        isS[N - 1] = true
        var nS = 1
        for (i in N - 2 downTo 0) {
            isS[i] = input[i] < input[i + 1] || input[i] == input[i + 1] && isS[i + 1]
            if (isS[i]) nS++
        }

        var n = 0
        if (nS > 1) {
            // step 1
            sort(input, sa, K, N, isS)

            // step 2
            run {
                val isLMS = BooleanArray(N)
                for (i in 0 until N) {
                    isLMS[i] = isS[i] && (i == 0 || !isS[i - 1])
                    if (isLMS[i]) n++
                }

                // renumber
                val LMS = IntArray(n + 1)
                run {
                    val rev = IntArray(N)
                    run {
                        var i = 0
                        var j = 0
                        var k = 0
                        while (i < N) {
                            if (isLMS[i]) {
                                rev[i] = j
                                LMS[j] = i
                                j++
                            }
                            if (isLMS[sa[i]])
                                sa[k++] = sa[i]
                            i++
                        }
                    }
                    for (i in 0 until n)
                        sa[i] = rev[sa[i]]
                }
                LMS[n] = N - 1

                // rename
                val s = IntArray(n)
                var Knew = -1
                run {
                    var i = 0
                    var j = -1
                    var l = -1
                    while (i < n) {
                        val c = sa[i]
                        var f = LMS[c + 1] - LMS[c] == l
                        if (f) {
                            val p = LMS[sa[i]]
                            val q = LMS[sa[j]]
                            var k = 0
                            while (f && k <= l) {
                                f = input[p + k] == input[q + k]
                                k++
                            }
                        }
                        if (f)
                            s[c] = Knew
                        else {
                            l = LMS[c + 1] - LMS[c]
                            j = i
                            s[c] = ++Knew
                        }
                        i++
                    }
                }
                Knew++

                Arrays.fill(sa, 0, N, -1)
                // unique
                if (n == Knew)
                    for (i in 0 until n)
                        sa[s[i]] = LMS[i]
                else {
                    rec(s, sa, n, Knew)
                    for (i in 0 until n)
                        sa[i] = LMS[sa[i]]
                }// not unique
            }
        } else
            sa[n++] = N - 1

        // step 3
        sort(input, sa, K, n, isS)

        return sa
    }

    private fun sort(input: IntArray, sa: IntArray, K: Int, n: Int, isS: BooleanArray) {
        val N = input.size
        // make buckets
        val bkt = IntArray(K)
        input.forEach { bkt[it]++ }
        for (i in 1 until K) bkt[i] += bkt[i - 1]
        val idx = IntArray(K)

        System.arraycopy(bkt, 0, idx, 0, K)
        // arrange S suffixes
        if (n < N)
            for (i in n - 1 downTo 0) {
                val c = sa[i]
                sa[i] = -1
                sa[--idx[input[c]]] = c
            }
        else for (i in 0 until N) if (isS[i]) sa[--idx[input[i]]] = i// sort by first character

        // copy S -> L
        System.arraycopy(bkt, 0, idx, 1, K - 1)
        idx[0] = 0
        sa.forEach {
            val k = it - 1
            if (k >= 0 && !isS[k]) sa[idx[input[k]]++] = k
        }

        // copy L -> S
        System.arraycopy(bkt, 0, idx, 0, K)
        for (i in N - 1 downTo 0) {
            val k = sa[i] - 1
            if (k >= 0 && isS[k]) sa[--idx[input[k]]] = k
        }
    }
}
