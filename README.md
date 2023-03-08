### Versioned Storage Java SDK

Java implementation of Block based versioned data storage. Python implementation is [here](https://github.com/JaneliaSciComp/VersionedStorage/).

----
#### Proposed solution:
To enable version management for nd data, a mix is created of:
- Version block index using [Zarr](https://zarr.readthedocs.io/en/stable/) / [N5](https://github.com/saalfeldlab/n5) + [Git](https://git-scm.com/) 
- A key value store: using [Zarr](https://zarr.readthedocs.io/en/stable/) / [N5](https://github.com/saalfeldlab/n5) for now


![solution](img/solution.png "Proposed solution")

#### How to Use:
Reader:
```
N5Reader reader = new KleioReader<>(new N5FSReader(INDEXES),new N5FSReader(RAW))
```
Writer: 
```
KleioWriter writer = new Kleio<>(new KleioN5FSIndexWriter(INDEXES),new N5Writer(RAW))
# Commit
writer.commit()
# Create new Branch
writer.createNewBranch(BRANCH_NAME)
# Checkout Branch
writer.checkoutBranch(BRANCH_NAME)
# Push
writer.push
```


----
#### for MAC M1 Chip:

1- install c-blosc: brew install c-blosc

2- Look for it: brew list c-blosc -v

3- jvm param: -Djna.library.path=/opt/homebrew/Cellar/c-blosc/1.21.1/lib/
