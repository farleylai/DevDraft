System.in.withReader { 
    count = it.readLine().toInteger()
    workbench = (it.readLine() as List)
    swaps = it.readLine().toInteger()
    indexes = it.readLine().split()
} 


// Build indexes for basic compounds: FIZ, BAR, BAZ, ZIF, RAB, or ZAB
BAR = 'BAR' as List
BAZ = 'BAZ' as List
FIZ = 'FIZ' as List
RAB = 'RAB' as List
ZAB = 'ZAB' as List
ZIF = 'ZIF' as List
ALPHABET = 'ABFIRZ' as List
VOCAB = [BAR, BAZ, FIZ, RAB, ZAB, ZIF]
DICT = [B: [BAR, BAZ], F:[FIZ], R: [RAB], Z: [ZAB, ZIF]].withDefault { [] }

def match(workbench) {
    // Mark positions with matched compounds
    def marked = [].withDefault { false }
    workbench[0..-3].eachWithIndex { crystal, idx ->
        if(DICT[crystal].any { it == workbench[idx..idx+2] }) 
            marked[idx..idx+2] = [true] * 3
    }

    // Remove marked compounds from workbench
    marked.eachWithIndex { mark, idx ->
        if(mark) workbench[idx] = false
    }
    workbench.removeAll { !it }
}

def swap(workbench, pos1, pos2) {
    tmp = workbench[pos1]
    workbench[pos1] = workbench[pos2]
    workbench[pos2] = tmp
}

def diff(c1, c2) {
    def count = 0
    def pos = []
    c1.eachWithIndex { c, i -> 
        if(c1[i] == c2[i]) return
        count++
        pos += i
    }
    return [count, pos]
}

def move(workbench) {
    // Search for a legal swap
    // 1. Build indexes for each crystal in the workbench: O(n)
    def indexes = [:].withDefault { [].withDefault{-1} }
    workbench.eachWithIndex { crystal, idx ->
        indexes[crystal] += idx
    }

    // 2. Find candidates with one crystal to swap: O(n)
    def found = false
    def candidates = [].withDefault { false }
    workbench[0..-3].eachWithIndex { crystal, idx ->
        if(found) return
        def candidate = workbench[idx..idx+2]
        def compounds = VOCAB.findAll { diff(candidate, it)[0] == 1 } 
        compounds.each {
            // 3. Swap for any eligible position from workbench: O(c)
            if(found) return
            def (count, pos) = diff(candidate, it)
            def pos1 = idx + pos[0] 
            def pos2 = indexes[it[pos[0]]]
            pos2 = pos2.find { it == pos1 + 1 }
            if(pos2 == pos1 + 1) {
                found = true
                swap(workbench, pos1, pos2)   
            }
        }
    }
}

def pos1 = indexes[0].toInteger()
def pos2 = indexes[1].toInteger()
swap(workbench, pos1, pos2)
match(workbench)
while(--swaps >= 0) {
    move(workbench)
    match(workbench)
}

println workbench.join()
