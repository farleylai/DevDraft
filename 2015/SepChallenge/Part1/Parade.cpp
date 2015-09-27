#include <iostream>
#include <climits>
#include <vector>
#include <stack>
#include <map>
using namespace std;

void debug(int B, int T[], map<int, int> &F) {
    cout << B << endl;
    string s;
    for(int i = 0; i < B; i++)
        s += to_string(T[i]) + ' ';
    cout << s << endl;
    for(map<int, int>::iterator it = F.begin(); it != F.end(); it++)
        cout << it->first << ":" << it->second << " ";
    cout << endl;
}

/**
 * Customized map for tracking carried threats given available forces at a particular block.
 */
class AnswerSheet {
private:
    class CoverageThreatMap {
        private:
            map<int, int> ct;
        public:
            int& operator[] (const int key) {
                // Rank by remaining force coverage to facilitate binary search for the carried threat upper bound
                map<int, int>::iterator it = ct.find(key);
                if(it == ct.end()) {
                    map<int, int>::iterator pos = ct.upper_bound(key);
                    ct[key] = pos == ct.end() ? INT_MAX : pos->second;
                }
                return ct[key];
            }
            map<int, int>::iterator begin() {
                return ct.begin();
            }
            map<int, int>::iterator end() {
                return ct.end();
            }
            int size() {
                return ct.size();
            }
    };

    map<int, CoverageThreatMap> ans;
    
public:
    CoverageThreatMap& operator[] (const int key) {
        return ans[key];
    }
    int size() {
        return ans.size();
    }
    void debug() {
        for(map<int, CoverageThreatMap>::iterator it = ans.begin(); it != ans.end(); it++) {
            cout << it->first << ": [";
            CoverageThreatMap ct = it->second;
            for(map<int, int>::iterator itr = ct.begin(); itr != ct.end(); itr++) {
                cout << itr->first << ':' << itr->second << ", ";
            }
            cout << "]" << endl;
        }
    }
};

/**
 * Iterative branch and bound algorithm to search the optimal solution in the problem space.
 */
int solve(int B, int T[], map<int, int> &F) {
    // Optimization: trim leading and trailing zero threat blocks
    int head = 0;
    int tail = B - 1;
    while(head < B && T[head] == 0) head++;
    while(tail >= 0 && T[tail] == 0) tail--;
    if(head > tail) return 0;
    int* pT = T;
    T = new int[B = tail - head + 1];
    for(int i = 0; i < B; i++)
        T[i] = pT[head+i];
    //debug(B, T, F);

    // Optimization: customized map to fast prune configurations with lower remaining force coverage but higher carried threats
    AnswerSheet ANS;
    stack<int> stk;
    int min = 0;
    int max = 0;
    for(map<int, int>::iterator it = F.begin(); it != F.end(); it++)
       min += it->second; 
    for(map<int, int>::iterator it = F.begin(); it != F.end(); it++)
       max += it->first * it->second;
    stk.push(0);
    stk.push(max);
    stk.push(min);
    for(map<int, int>::reverse_iterator it = F.rbegin(); it != F.rend(); it++)
        stk.push(it->second);
    stk.push(0);
    vector<int> forces;
    while(!stk.empty()) {
        forces.clear();
        int block = stk.top(); stk.pop();
        for(int i = 0; i < F.size(); i++) {
            forces.push_back(stk.top()); 
            stk.pop();
        }
        int coverageMin = stk.top(); stk.pop();
        int coverageMax = stk.top(); stk.pop();
        int threats = stk.top(); stk.pop();
        // Optimization: branch and bound on the best force patrol so far
        if(threats >= ANS[B][0]) continue;

        // Optimization: skip zero segment
        while(block < B && T[block] == 0) block++;

        // Termination: reaching the end or sufficient force coverage for the remaining blocks
        if(block >= B || coverageMax >= B - block) {
            if(threats < ANS[B][0])
                ANS[B][0] = threats;
            continue;
        }

        // Forces are hashed by their min-max coverage range ordering
        int cfg = coverageMin * 10 + coverageMax;
        if(threats < ANS[block][cfg]) {
            ANS[block][cfg] = threats;
            // Use no forces
            stk.push(threats + T[block]);
            stk.push(coverageMax);
            stk.push(coverageMin);
            for(vector<int>::reverse_iterator it = forces.rbegin(); it != forces.rend(); it++)
                stk.push(*it);
            stk.push(block + 1);
            // Use either of the forces
            for(int i = 0; i < forces.size(); i++) {
                int force = forces[i];
                if(force > 0) {
                    stk.push(threats);
                    stk.push(coverageMax - next(F.begin(), i)->first);
                    stk.push(coverageMin - 1);
                    for(int idx = forces.size() - 1; idx > i; idx--)
                        stk.push(forces[idx]);
                    stk.push(force - 1);
                    if(i > 0) {
                        for(int idx = i - 1; idx >= 0; idx--)
                            stk.push(forces[idx]);
                    }
                    stk.push(block + next(F.begin(), i)->first);
                }
            }
        }
    }
    //ANS.debug();
    return ANS[B][0];
}

int main() {
    int scalingB = 1;
    int scalingF = 1;
    int B;
    int P;
    cin >> B;
    cin >> P;
    B *= scalingB;
    int T[B];
    map<int, int> F;
    int prevB = B / scalingB;
    for(int i = 0; i < B; i++) {
        if(i < prevB)
            cin  >> T[i];
        else
            T[i] = T[i % prevB];
    }
    for(int i = 0; i < P; i++) {
        int L, N;
        cin >> L;
        cin >> N;
        F[L] = N * scalingF;
    }
    int threats = solve(B, T, F);
    cout << threats << endl;
}
