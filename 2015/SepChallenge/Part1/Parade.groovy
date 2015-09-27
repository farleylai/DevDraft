int SOLUTION
System.in.withReader {
    def String[] fields = it.readLine().split()
    B = fields[0].toInteger()
    P = fields[1].toInteger()
    T = it.readLine().split().collect { it.toInteger() }[0..<B]
    fields = it.readLine().split()
    F = [:]
    P.times {
        F[fields[2*it].toInteger()] = fields[2*it+1].toInteger()
    }  
    F = F.sort()
    if(System.getProperty('DEBUG'))
        SOLUTION = it.readLine().toInteger() 
}

/**
 * Recursive DP implementation may cause stack overflow on large input size.
 */
def rSolve(blocks, forces) {
    def ANS = [:].withDefault { [:] }
    // Base cases: no blocks or no available forces
    if(blocks <= 0) return 0
    if(forces.isEmpty()) return T[0..<blocks].sum()
    // Check solution bookkeeping: threats at (blocks-1)th block given a set available forces
    if(ANS[blocks-1][forces]) return ANS[blocks-1][forces]
    // Explore possible patrol assignement: use one of the available forces or skip this block
    def ret = [:]
    forces.each {
        def forcesNew = forces.clone()
        if(--forcesNew[it.key] == 0)
            forcesNew.remove(it.key)
        // Use one of the available forces
        ret[it.key] =  rSolve(blocks - it.key, forcesNew)
    }
    // Or skip this block without patrol
    ret[0] = rSolve(blocks - 1, forces) + T[blocks - 1]
    //printf('ANS[%d][ %s ] => %s\n', blocks - 1, forces.toString(), ret.toString());
    return ANS[blocks-1][forces] = ret.values().min()
}

/**
 * Iterative prune and search is efficient on large input size in O(n^2)
 */
import groovy.transform.CompileStatic
@CompileStatic
int iSolve(List<Integer> T, Map<Integer, Integer> F) {
    // Optimization: trim leading and trailing zero threat blocks
    int head = 0
    int tail = T.size() - 1
    while(head < T.size() && T[head] == 0) head++
    while(tail >= 0 && T[tail] == 0) tail--
    if(head > tail) return 0
    T = T[head..tail]
    int blocks = T.size()
    List<Integer> FL = F.keySet() as List<Integer>
    List<Integer> FN = F.values() as List<Integer>

    // Optimization: fast pruning configurations with lower remaining force coverage but higher carried threats
    Map<Integer, Map<Integer, Integer>> ANS = [:].sort().withDefault { 
        def m = [:].sort(); 
        // Rank by remaining force coverage to facilitate binary search for the carried threat upper bound
        m.withDefault { key-> 
            int idx = Collections.binarySearch(m.keySet() as List, key)
            idx = -idx - 1
            return idx == m.size() ? Integer.MAX_VALUE : m[m.keySet()[idx]]
        }
    }
    // Find force coverage
    def cover = { List<Integer> forces->
        int min = 0 
        int max = 0 
        forces.eachWithIndex { int force, int idx-> min += force; max += force * FL[idx] }
        return min * 10 + max
    }
    def coverMin = { List<Integer> forces->
        int min = 0 
        forces.each { int it-> min += it }
        return min
    }
    def coverMax = { List<Integer> forces->
        int max = 0 
        forces.eachWithIndex { int force, int idx-> max += force * FL[idx]}
        return max
    }
    // Forces are hashed by their min-max coverage range ordering. 
    def hash = { int min, int max->
        return min * 10 + max
    }

    // Optimization: use stack to reduce space complexity to O(n)
    Stack<Integer> stack = []
    stack << 0 << coverMax(FN) << coverMin(FN)
    FN[-1..0].each { stack << it }
    stack << 0
    List<Integer> forces = []
    while(!stack.isEmpty()) {
        forces.clear()
        int block = stack.pop()
        F.each { forces << stack.pop() }
        int coverageMin = stack.pop()
        int coverageMax = stack.pop()
        int threats = stack.pop()
        // Optimization: branch and bound on the best force patrol so far
        if(threats >= ANS[blocks][0]) continue;

        // Optimization: skip zero segment
        while(block < blocks && T[block] == 0) block++

        // Termination: reaching the end or sufficient force coverage for the remaining blocks
        if(block >= blocks || coverageMax >= blocks - block) {
            if(threats < ANS[blocks][0])
                ANS[blocks][0] = threats
            continue
        }
        
        // Pruning: if the carried threats is lower than previous results
        int cfg = hash(coverageMin, coverageMax)
        if(threats < ANS[block][cfg]) {
            ANS[block][cfg] = threats
            // Use no forces
            stack << threats + T[block]
            stack << coverageMax
            stack << coverageMin
            forces[-1..0].each { stack << it }
            stack << block + 1
            // Use either of the forces
            forces[0..-1].eachWithIndex { force, idx->
                if(force > 0) {
                    stack << threats
                    stack << coverageMax - FL[idx]
                    stack << coverageMin - 1
                    forces[-1..<idx].each { stack << it }
                    stack << force - 1
                    if(idx > 0)
                        forces[idx-1..0].each { stack << it }
                    stack << block + FL[idx]
                }
            }
        }
    }

    if(System.getProperty('DEBUG')) {
        ANS.each { cfg->
            println cfg.key + ': ' + cfg.value
        }
        println 'States: ' + ANS.values().collect { Map<Integer, Integer> it-> it.size() }.sum()  
    }
    return ANS[blocks][0]
}

B *= 1
T *= 1
F.each { k,v-> F[k] = 1 * v }
//println rSolve(B, F)
int ans = iSolve(T, F)
println ans
if(System.getProperty('DEBUG'))
    assert SOLUTION == ans
