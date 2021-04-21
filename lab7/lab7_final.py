#!/usr/bin/env python
# coding: utf-8

# In[115]:


import numpy as np

L1_4  = [0, 1, 1, 0, 0, 0, 0, 0, 0, 0]
L1  = [0, 1, 1, 0, 1, 0, 0, 0, 0, 0]
L2  = [1, 0, 0, 1, 0, 0, 0, 0, 0, 0]
L3_4  = [0, 1, 0, 0, 0, 0, 0, 0, 0, 0]
L3  = [0, 1, 0, 0, 0, 0, 1, 0, 0, 0]
L4  = [0, 1, 1, 0, 0, 0, 0, 0, 0, 0]
L5  = [0, 0, 0, 0, 0, 1, 1, 0, 0, 0]
L6  = [0, 0, 0, 0, 0, 0, 1, 1, 0, 0]
L7  = [0, 0, 0, 0, 1, 1, 1, 1, 1, 1]
L8  = [0, 0, 0, 0, 0, 0, 1, 0, 1, 0]
L9  = [0, 0, 0, 0, 0, 0, 1, 0, 0, 1]
L10 = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]

L = np.array([L1, L2, L3, L4, L5, L6, L7, L8, L9, L10])
L2 = np.array([L1_4, L2, L3_4, L4, L5, L6, L7, L8, L9, L10])

ITERATIONS = 100

def getM(L):
    M = np.zeros([10, 10], dtype=float)
    # number of outgoing links
    c = np.zeros([10], dtype=int)
    
    ## TODO 1 compute the stochastic matrix M
    for i in range(0, 10):
        c[i] = sum(L[i])
    
    for i in range(0, 10):
        for j in range(0, 10):
            if L[j][i] == 0: 
                M[i][j] = 0
            else:
                M[i][j] = 1.0/c[j]
    return M

def trustrank(M,q,d,tr):
    for i in range(ITERATIONS):
        tr = q*d + (1-q)*(M@tr)
    tr = tr/sum(tr)
    tr = sorted(enumerate(tr,1), key=lambda x: -x[1])
    print(str(tr).replace('[','').replace(']','').replace('(','').replace(')','\n').replace(',',' '))
    
    
print("Matrix L (indices)\n")
print(L)    

M = getM(L)

print("\nMatrix M (stochastic matrix)\n")
print(M)

### TODO 2: compute pagerank with damping factor q = 0.15
### Then, sort and print: (page index (first index = 1 add +1) : pagerank)
### (use regular array + sort method + lambda function)
print("\nPAGERANK\n")

q = 0.15
p = np.zeros([10], dtype=float)
pr = [v for v in p]
for i in range(ITERATIONS):
    pr = q + (1 - q) * (M@pr)
pr=pr/sum(pr)
pr = sorted(enumerate(pr,1), key=lambda x: -x[1])
print(str(pr).replace('[','').replace(']','').replace('(','').replace(')','\n').replace(',',''))


### TODO 3: compute trustrank with damping factor q = 0.15
### Documents that are good = 1, 2 (indexes = 0, 1)
### Then, sort and print: (page index (first index = 1, add +1) : trustrank)
### (use regular array + sort method + lambda function)

print("\nTRUSTRANK (DOCUMENTS 1 AND 2 ARE GOOD)\n")

q = 0.15
d = np.zeros([10], dtype=float)
d[0]=1
d[1]=1
d=d/sum(d) #normalization
tr = [v for v in d] #initial vector
trustrank(M,q,d,tr)
    
### TODO 4: Repeat TODO 3 but remove the connections 3->7 and 1->5 (indexes: 2->6, 0->4) 
### before computing trustrank
M2 = getM(L2)
print("\nTRUSTRANK (removed connections 3->7 and 1->5)\n")
trustrank(M2,q,d,tr)


# In[ ]:




