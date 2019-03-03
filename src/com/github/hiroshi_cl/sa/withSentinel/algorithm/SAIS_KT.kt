package com.github.hiroshi_cl.sa.withSentinel.algorithm

import java.util.Arrays

import com.github.hiroshi_cl.sa.withSentinel.SuffixArray

class SAIS_KT : SuffixArray() {

    override fun saInternal(cs: CharArray, sa: IntArray): IntArray {
        val s = cs.map { it.toInt() }.toIntArray()
        return rec(s, sa, cs.size, 1 shl Character.SIZE)
    }

    //isSの初期化とSのカウント
    private fun makeS(input: List<Int>): Pair<Int, List<Boolean>> {
        val temp = generateSequence(Triple(true, 1, input.size - 2)) { (before, nS, i) ->
            val flag = input[i] < input[i + 1] || input[i] == input[i + 1] && before
            Triple(flag, if (flag) nS + 1 else nS, i - 1)
        }.take(input.size).toList().asReversed()

        return Pair(temp.first().second, temp.map { it.first })
    }

    private fun makeLMS(isS: List<Boolean>): Pair<Int, List<Boolean>> {
        val temp = generateSequence(Triple(isS[0], if(isS[0]) 1 else 0, 1)) { (_, count, i) ->
            val flag = isS[i] && !isS[i - 1]
            Triple(flag, if (flag) count+1 else count, i+1)
        }.take(isS.size)

        return Pair(temp.last().second, temp.map { it.first }.toList())
    }

    //isLMSの初期化とカウント
    private fun makeLMS(isS: List<Boolean>, isLMS: BooleanArray): Int {
        var n = 0
        for (i in 0 until isS.size) {
            isLMS[i] = isS[i] && (i == 0 || !isS[i - 1])
            if (isLMS[i]) n++
        }
        return n
    }

    private fun step2(input: IntArray, sa: IntArray, N: Int, isS: List<Boolean>): Int {
        val (n, isLMS) = makeLMS(isS)

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
            for (i in 0 until n) sa[i] = rev[sa[i]]
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
        if (n == Knew) for (i in 0 until n) sa[s[i]] = LMS[i]
        else { // not unique
            rec(s, sa, n, Knew)
            for (i in 0 until n) sa[i] = LMS[sa[i]]
        }

        return n
    }

    private fun rec(input: IntArray, sa: IntArray, N: Int, K: Int): IntArray {
        // determine L or S and count
        val (nS, isS) = makeS(input.toList())

        val n = if (nS > 1) {
            // step 1
            sort(input, sa, K, N, isS)

            // step 2
            step2(input, sa, N, isS)
        } else {
            sa[0] = N - 1
            1
        }

        // step 3
        sort(input, sa, K, n, isS)

        return sa
    }

    private fun sort(input: IntArray, sa: IntArray, K: Int, n: Int, isS: List<Boolean>) {
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
