System.in.withReader { 
    count = it.readLine().toInteger()
    workbench = (it.readLine() as List)
    swaps = it.readLine().toInteger()
    positions = it.readLine().split()
} 

def match(workbench, pos) {
    if(workbench.size() < 3) return
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

    // Mark positions with matched compounds
    def marked = []
    def start = Math.max(0, pos-2)
    def end = Math.min(pos+1, workbench.size()-3)
    workbench[start..end].eachWithIndex { crystal, idx ->
        idx = start + idx
        if(DICT[crystal].any { it == workbench[idx..idx+2] }) 
            marked += [idx, idx+2]
    }

    // Remove marked compounds from workbench if any
    if(marked.isEmpty()) {
        return false
    } else {
        workbench[marked[0]..marked[-1]] = []
        // In case of cascading removal due to the swap
        match(workbench, marked[0])     
        return true
    }
}

positions.each {
    def pos1 = it.toInteger()
    def pos2 = pos1 + 1
    // Roll back in case of illegal swaps
    workbench[pos1, pos2] = workbench[pos2, pos1]
    if(!match(workbench, pos1))
        workbench[pos1, pos2] = workbench[pos2, pos1]
}

println workbench.join()
